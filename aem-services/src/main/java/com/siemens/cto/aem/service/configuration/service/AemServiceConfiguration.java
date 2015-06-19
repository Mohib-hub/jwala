package com.siemens.cto.aem.service.configuration.service;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.configuration.AemCommandExecutorConfig;
import com.siemens.cto.aem.control.configuration.AemSshConfig;
import com.siemens.cto.aem.control.webserver.command.impl.WebServerServiceExistenceFacade;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.configuration.AemDaoConfiguration;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.app.impl.ApplicationServiceImpl;
import com.siemens.cto.aem.service.app.impl.PrivateApplicationServiceImpl;
import com.siemens.cto.aem.service.configuration.jms.AemJmsConfig;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.*;
import com.siemens.cto.aem.service.group.impl.*;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmControlServiceLifecycle;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.JvmStateGateway;
import com.siemens.cto.aem.service.jvm.impl.JvmControlServiceImpl;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.jvm.impl.JvmStateServiceImpl;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.aem.service.state.*;
import com.siemens.cto.aem.service.state.impl.GroupStateServiceImpl;
import com.siemens.cto.aem.service.state.jms.JmsStateNotificationConsumerBuilderImpl;
import com.siemens.cto.aem.service.state.jms.JmsStateNotificationServiceImpl;
import com.siemens.cto.aem.service.webserver.*;
import com.siemens.cto.aem.service.webserver.heartbeat.WebServerServiceFacade;
import com.siemens.cto.aem.service.webserver.heartbeat.WebServerStateServiceFacade;
import com.siemens.cto.aem.service.webserver.impl.*;
import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.toc.files.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AemServiceConfiguration {

    @Autowired
    private AemPersistenceServiceConfiguration persistenceServiceConfiguration;

    @Autowired
    private AemDaoConfiguration aemDaoConfiguration;

    @Autowired
    private AemCommandExecutorConfig aemCommandExecutorConfig;

    @Autowired
    private AemJmsConfig aemJmsConfig;

    @Autowired
    private CommandDispatchGateway commandDispatchGateway;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private StateNotificationGateway stateNotificationGateway;

    @Autowired
    private WebServerStateGateway webServerStateGateway;

    @Autowired
    private JvmStateGateway jvmStateGateway;

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private AemSshConfig aemSshConfig;
    
    @Autowired
    private HarmonyTemplateEngine harmonyTemplateEngine;

    @Autowired
    private WebServerDao webServerDao;

    @Autowired
    @Qualifier("webServerHttpRequestFactory")
    private HttpClientRequestFactory httpClientRequestFactory;

    @Autowired
    @Qualifier("webServerServiceExistence")
    private WebServerServiceExistenceFacade webServerServiceExistenceFacade;

    @Autowired
    @Qualifier("webServerStateServiceFacade")
    private WebServerStateServiceFacade webServerStateServiceFacade;

    /**
     * Make toc.properties available to spring integration configuration
     * System properties are only used if there is no setting in toc.properties.
     */
    @Bean 
    public static PropertySourcesPlaceholderConfigurer configurer() { 
         PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
         ppc.setLocation(new ClassPathResource("META-INF/spring/toc-defaults.properties"));
         ppc.setLocalOverride(true);
         ppc.setProperties(ApplicationProperties.getProperties());
         return ppc;
    } 
    
    @Bean(name="groupStateMachine")
    @Scope((ConfigurableBeanFactory.SCOPE_PROTOTYPE))
    public GroupStateMachine getGroupStateMachine() {
        return new GroupStateManagerTableImpl();
    }

    @Bean(name="groupStateService")
    public GroupStateService.API getGroupStateService() {
        return new GroupStateServiceImpl(
                persistenceServiceConfiguration.getGroupPersistenceService(),
                getStateNotificationService(),
                StateType.GROUP,
                stateNotificationGateway
                );
    }

    @Bean
    public GroupService getGroupService() {
        return new GroupServiceImpl(
                persistenceServiceConfiguration.getGroupPersistenceService(),
                stateNotificationGateway);
    }

    @Bean(name = "jvmService")
    public JvmService getJvmService() {
        return new JvmServiceImpl(persistenceServiceConfiguration.getJvmPersistenceService(),
                                  getGroupService(),
                fileManager,
                                  jvmStateGateway);
    }

    @Bean(name="webServerService")
    public WebServerService getWebServerService() {
        return new WebServerServiceImpl(aemDaoConfiguration.getWebServerDao(), fileManager);
    }

    @Bean
    public ApplicationService getApplicationService() {
        return new ApplicationServiceImpl(aemDaoConfiguration.getApplicationDao(), persistenceServiceConfiguration.getApplicationPersistenceService());
    }

    @Bean
    public PrivateApplicationService getPrivateApplicationService() {
        return new PrivateApplicationServiceImpl(/** Relying on autowire */);
    }

    @Bean(name="jvmControlService")
    public JvmControlService getJvmControlService() {
        return new JvmControlServiceImpl(getJvmService(),
                                         aemCommandExecutorConfig.getJvmCommandExecutor(),
                                         getJvmControlServiceLifecycle());
    }

    @Bean(name="jvmControlServiceLifecycle")
    public JvmControlServiceLifecycle getJvmControlServiceLifecycle() {
        return new JvmControlServiceImpl.LifecycleImpl(
                persistenceServiceConfiguration.getJvmControlPersistenceService(),
                getJvmStateService());
    }

    @Bean(name="groupControlService")
    public GroupControlService getGroupControlService() {
        return new GroupControlServiceImpl(
                getGroupWebServerControlService(),
                getGroupJvmControlService(),
                getGroupStateService());
    }

    @Bean(name="groupJvmControlService")
    public GroupJvmControlService getGroupJvmControlService() {
        return new GroupJvmControlServiceImpl(persistenceServiceConfiguration.getGroupControlPersistenceService(),
                                         getGroupService(),
                                         commandDispatchGateway);
    }

    @Bean(name="groupWebServerControlService")
    public GroupWebServerControlService getGroupWebServerControlService() {
        return new GroupWebServerControlServiceImpl(persistenceServiceConfiguration.getGroupControlPersistenceService(),
                                         getGroupService(),
                                         commandDispatchGateway);
    }

    @Bean(name="webServerControlService")
    public WebServerControlService getWebServerControlService() {
        return new WebServerControlServiceImpl(getWebServerService(),
                                               aemCommandExecutorConfig.getWebServerCommandExecutor(),
                                               webServerStateGateway,
                                               getWebServerControlHistoryService(),
                                               getWebServerStateService());
    }

    @Bean(name="webServerCommandService")
    public WebServerCommandService getWebServerCommandService() {
        final SshConfiguration sshConfig = aemSshConfig.getSshConfiguration();

        final JschBuilder jschBuilder = new JschBuilder().setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                .setKnownHostsFileName(sshConfig.getKnownHostsFile());

        return new WebServerCommandServiceImpl(getWebServerService(),
                                               commandExecutor,
                                               jschBuilder,
                                               sshConfig);
    }

    @Bean
    public WebServerControlHistoryService getWebServerControlHistoryService() {
        return new WebServerControlHistoryServiceImpl(persistenceServiceConfiguration.getWebServerControlPersistenceService());
    }

    @Bean(name = "stateNotificationService")
    public StateNotificationService getStateNotificationService() {
        return new JmsStateNotificationServiceImpl(aemJmsConfig.getJmsTemplate(),
                                                   aemJmsConfig.getStateNotificationDestination(),
                                                   getStateNotificationConsumerBuilder());
    }

    @Bean(name = "jvmStateService")
    public StateService<Jvm, JvmState> getJvmStateService() {
        return new JvmStateServiceImpl(persistenceServiceConfiguration.getJvmStatePersistenceService(),
                                                getStateNotificationService(),
                                                stateNotificationGateway);
    }

    @Bean(name = "webServerStateService")
    public StateService<WebServer, WebServerReachableState> getWebServerStateService() {
        return new WebServerStateServiceImpl(persistenceServiceConfiguration.getWebServerStatePersistenceService(),
                                             getStateNotificationService(),
                                             stateNotificationGateway);
    }

    @Bean
    public StateNotificationConsumerBuilder getStateNotificationConsumerBuilder() {
        return new JmsStateNotificationConsumerBuilderImpl(aemJmsConfig.getJmsPackageBuilder());
    }
    
    @Bean(name = "resourceService")
    public ResourceService getResourceService() {
        return new ResourceServiceImpl(fileManager, harmonyTemplateEngine, persistenceServiceConfiguration.getResourcePersistenceService(), persistenceServiceConfiguration.getGroupPersistenceService());
    }

    @Bean(name = "webServerProvider")
    public WebServerServiceFacade getWebServerProvider() {
        return new WebServerServiceFacade(getWebServerService());
    }

    @Bean(name = "webServerStateServiceFacade")
    public WebServerStateServiceFacade getWebServerStateServiceFacade() {
        return new WebServerStateServiceFacade(getWebServerStateService(), webServerDao);
    }

    @Bean
    public WebServerStateGateway getWebServerStateGateway() {
        return new WebServerStateGatewayImpl(httpClientRequestFactory, webServerServiceExistenceFacade,
                                             commandExecutor, aemSshConfig, webServerStateServiceFacade);
    }

    @Bean
    public WebServerStateRetrievalScheduledTaskHandler getWebServerStatePollerTask() {
        return new WebServerStateRetrievalScheduledTaskHandler(getWebServerProvider(), httpClientRequestFactory,
                                            webServerStateServiceFacade);
    }

}
