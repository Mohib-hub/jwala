package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaGroupAppConfigTemplate;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.exception.GroupLevelAppResourceHandlerException;
import com.cerner.jwala.service.exception.ResourceServiceException;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Handler for a group level application resource identified by a "resource identifier" {@link ResourceIdentifier}
 * <p>
 * Created by Jedd Cuison on 7/21/2016
 */
public class GroupLevelAppResourceHandler extends ResourceHandler {

    private static final String WAR_FILE_EXTENSION = ".war";
    private static final String MSG_CAN_ONLY_HAVE_ONE_WAR = "A web application can only have 1 war file. To change it, delete the war file first before uploading a new one.";

    private final static Logger LOGGER = LoggerFactory.getLogger(GroupLevelAppResourceHandler.class);

    private final GroupPersistenceService groupPersistenceService;
    private final JvmPersistenceService jvmPersistenceService;
    private final ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    ResourceContentGeneratorService resourceContentGeneratorService;

    public GroupLevelAppResourceHandler(final ResourceDao resourceDao,
                                        final GroupPersistenceService groupPersistenceService,
                                        final JvmPersistenceService jvmPersistenceService,
                                        final ApplicationPersistenceService applicationPersistenceService,
                                        final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.groupPersistenceService = groupPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getGroupLevelAppResource(resourceIdentifier.resourceName, resourceIdentifier.webAppName,
                    resourceIdentifier.groupName);
        } else if (successor != null) {
            configTemplate = successor.fetchResource(resourceIdentifier);
        }
        return configTemplate;
    }

    @Override
    public CreateResourceResponseWrapper
    createResource(final ResourceIdentifier resourceIdentifier,
                   final ResourceTemplateMetaData metaData,
                   final String templateContent) {
        ResourceTemplateMetaData metaDataCopy = metaData;
        CreateResourceResponseWrapper createResourceResponseWrapper = null;
        if (canHandle(resourceIdentifier)) {
            final String groupName = resourceIdentifier.groupName;
            final Group group = groupPersistenceService.getGroup(groupName);
            final ConfigTemplate createdConfigTemplate;

            if (metaDataCopy.getContentType().equals(MediaType.APPLICATION_ZIP) &&
                    templateContent.toLowerCase(Locale.US).endsWith(WAR_FILE_EXTENSION)) {
                final Application app = applicationPersistenceService.getApplication(resourceIdentifier.webAppName);
                if (StringUtils.isEmpty(app.getWarName())) {
                    applicationPersistenceService.updateWarInfo(resourceIdentifier.webAppName, metaDataCopy.getDeployFileName(), templateContent, getTokenizedDeployPath(metaDataCopy, app));
                    metaDataCopy = updateApplicationWarMetaData(resourceIdentifier, metaDataCopy, app);
                } else {
                    throw new ResourceServiceException(MSG_CAN_ONLY_HAVE_ONE_WAR);
                }
            }

            createdConfigTemplate = groupPersistenceService.populateGroupAppTemplate(groupName, resourceIdentifier.webAppName,
                    metaDataCopy.getDeployFileName(), metaDataCopy.getJsonData(), templateContent);

            createJvmTemplateFromAppResource(resourceIdentifier, templateContent, metaDataCopy, group);

            createResourceResponseWrapper = new CreateResourceResponseWrapper(createdConfigTemplate);
        } else if (successor != null) {
            createResourceResponseWrapper = successor.createResource(resourceIdentifier, metaDataCopy, templateContent);
        }
        return createResourceResponseWrapper;
    }

    private String getTokenizedDeployPath(ResourceTemplateMetaData metaData, Application app) {
        return resourceContentGeneratorService.generateContent(metaData.getDeployFileName(), metaData.getDeployPath(), null, app, ResourceGeneratorType.METADATA);
    }

    private ResourceTemplateMetaData updateApplicationWarMetaData(ResourceIdentifier resourceIdentifier, ResourceTemplateMetaData metaDataCopy, Application app) {
        boolean isUnpackWar = app.isUnpackWar();
        metaDataCopy = new ResourceTemplateMetaData(
                metaDataCopy.getTemplateName(),
                metaDataCopy.getContentType(),
                metaDataCopy.getDeployFileName(),
                metaDataCopy.getDeployPath(),
                metaDataCopy.getEntity(),
                isUnpackWar,
                metaDataCopy.isOverwrite(),
                metaDataCopy.isHotDeploy());

        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metaDataCopy);
            metaDataCopy.setJsonData(jsonData);
        } catch (IOException e) {
            final String errMsg = MessageFormat.format("Failed to update the war meta data for app {0} on {1} resource creation", resourceIdentifier.webAppName, metaDataCopy.getDeployFileName(), e);
            LOGGER.error(errMsg);
            throw new GroupLevelAppResourceHandlerException(errMsg);
        }

        return metaDataCopy;
    }

    private void createJvmTemplateFromAppResource(ResourceIdentifier resourceIdentifier, String templateContent, ResourceTemplateMetaData metaDataCopy, Group group) {
        // Can't we just get the application using the group name and target app name instead of getting all the applications
        // then iterating it to compare with the target app name ???
        // If we can do that then TODO: Refactor this to return only one application and remove the iteration!
        final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(group.getName());

        for (final Application application : applications) {
            if (metaDataCopy.getEntity().getDeployToJvms() && application.getName().equals(resourceIdentifier.webAppName)) {
                for (final Jvm jvm : group.getJvms()) {
                    UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaDataCopy.getTemplateName(),
                            metaDataCopy.getDeployFileName(), jvm.getJvmName(), metaDataCopy.getJsonData(), templateContent
                    );
                    JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);
                    applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
                }
            }
        }
    }

    @Override
    public void deleteResource(final ResourceIdentifier resourceIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean canHandle(final ResourceIdentifier resourceIdentifier) {
        return StringUtils.isNotEmpty(resourceIdentifier.resourceName) &&
                StringUtils.isNotEmpty(resourceIdentifier.webAppName) &&
                StringUtils.isNotEmpty(resourceIdentifier.groupName) &&
                StringUtils.isEmpty(resourceIdentifier.webServerName) &&
                StringUtils.isEmpty(resourceIdentifier.jvmName);
    }

    @Override
    public String updateResourceMetaData(final ResourceIdentifier resourceIdentifier, final String resourceName, final String metaData) {
        if (canHandle(resourceIdentifier)) {
            final String previousMetaData = groupPersistenceService.getGroupAppResourceTemplateMetaData(resourceIdentifier.groupName, resourceName, resourceIdentifier.webAppName);
            final String updatedMetaData = groupPersistenceService.updateGroupAppResourceMetaData(resourceIdentifier.groupName, resourceIdentifier.webAppName, resourceName, metaData);
            updateApplicationUnpackWar(resourceIdentifier.webAppName, resourceName, metaData);
            updateMetaDataForChildJVMResources(resourceIdentifier, resourceName, metaData);
            updateAppTemplatesWhenDeployToJvmsChanged(resourceIdentifier, resourceName, previousMetaData, updatedMetaData);
            return updatedMetaData;
        } else {
            return successor.updateResourceMetaData(resourceIdentifier, resourceName, metaData);
        }
    }

    private void updateAppTemplatesWhenDeployToJvmsChanged(final ResourceIdentifier resourceIdentifier, final String resourceName, final String previousMetaData, final String updatedMetaData) {
        try {
            ResourceTemplateMetaData oldMetaData = new ObjectMapper().readValue(previousMetaData, ResourceTemplateMetaData.class);
            ResourceTemplateMetaData newMetaData = new ObjectMapper().readValue(updatedMetaData, ResourceTemplateMetaData.class);
            boolean previousDeployToJvms = oldMetaData.getEntity().getDeployToJvms();
            boolean newDeployToJvms = newMetaData.getEntity().getDeployToJvms();
            if (previousDeployToJvms != newDeployToJvms) {
                Group group = groupPersistenceService.getGroup(resourceIdentifier.groupName);
                if (newDeployToJvms) {
                    // deployToJvms was changed to true - need to create the JVM templates
                    JpaGroupAppConfigTemplate appTemplate = resourceDao.getGroupLevelAppResource(resourceName, resourceIdentifier.webAppName, resourceIdentifier.groupName);
                    newMetaData.setJsonData(updatedMetaData);
                    createJvmTemplateFromAppResource(resourceIdentifier, appTemplate.getTemplateContent(), newMetaData, group);
                } else {
                    // deployToJvms was to false - need to delete the JVM templates
                    for (Jvm jvm : group.getJvms()) {
                        resourceDao.deleteAppResource(resourceName, resourceIdentifier.webAppName, jvm.getJvmName());
                    }
                }
            }
        } catch (IOException ioe) {
            final String errorMsg = MessageFormat.format("Failed to parse meta data for war {0} in application {1} during an update of the meta data", resourceName, resourceIdentifier.webAppName);
            LOGGER.error(errorMsg, ioe);
            throw new GroupLevelAppResourceHandlerException(errorMsg);
        }
    }

    private void updateApplicationUnpackWar(final String webAppName, final String resourceName, final String jsonMetaData) {
        Application application = applicationPersistenceService.getApplication(webAppName);
        ResourceTemplateMetaData warMetaData = null;
        final String appName = application.getName();
        try {
            warMetaData = new ObjectMapper().readValue(jsonMetaData, ResourceTemplateMetaData.class);
            applicationPersistenceService.updateApplication(new UpdateApplicationRequest(
                    application.getId(),
                    application.getGroup().getId(),
                    application.getWebAppContext(),
                    appName,
                    application.isSecure(),
                    application.isLoadBalanceAcrossServers(),
                    warMetaData.isUnpack()
            ));

            // update the war info for the application
            final String warName = application.getWarName();
            if (StringUtils.isNotEmpty(warName) && warName.equals(resourceName)) {
                final String tokenizedDeployPath = getTokenizedDeployPath(warMetaData, application);
                applicationPersistenceService.updateWarInfo(appName, resourceName, application.getWarPath(), tokenizedDeployPath);
            }
        } catch (IOException e) {
            final String errorMsg = MessageFormat.format("Failed to parse meta data for war {0} in application {1} during an update of the meta data", resourceName, appName);
            LOGGER.error(errorMsg, e);
            throw new GroupLevelAppResourceHandlerException(errorMsg);
        }
    }

    private void updateMetaDataForChildJVMResources(final ResourceIdentifier resourceIdentifier, final String resourceName, final String metaData) {
        Set<Jvm> jvmSet = groupPersistenceService.getGroup(resourceIdentifier.groupName).getJvms();
        for (Jvm jvm : jvmSet) {
            List<String> resourceNames = applicationPersistenceService.getResourceTemplateNames(resourceIdentifier.webAppName, jvm.getJvmName());
            if (resourceNames.contains(resourceName)) {
                applicationPersistenceService.updateResourceMetaData(resourceIdentifier.webAppName, resourceName, metaData, jvm.getJvmName(), resourceIdentifier.groupName);
            }
        }
    }

    @Override
    public Object getSelectedValue(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)) {
            return applicationPersistenceService.getApplication(resourceIdentifier.webAppName);
        } else {
            return successor.getSelectedValue(resourceIdentifier);
        }
    }

    @Override
    public List<String> getResourceNames(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)) {
            return resourceDao.getGroupLevelAppResourceNames(resourceIdentifier.groupName, resourceIdentifier.webAppName);
        } else {
            return successor.getResourceNames(resourceIdentifier);
        }
    }
}
