package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.exec.RuntimeCommand;
import com.siemens.cto.aem.common.request.jvm.*;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.JvmIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author meleje00
 */
@RunWith(MockitoJUnitRunner.class)
public class JvmServiceRestImplTest {

    private static final String name = "jvmName";
    private final static String hostName = "localhost";
    private static final List<Jvm> jvmList = createJvmList();
    private static final Jvm jvm = jvmList.get(0);

    // JVM ports
    private static final String httpPort = "80";
    private static final String httpsPort = "81";
    private static final String redirectPort = "82";
    private static final String shutdownPort = "83";
    private static final String ajpPort = "84";
    private static final Path statusPath = new Path("/statusPath");
    private static final String systemProperties = "EXAMPLE_OPTS=%someEnv%/someVal";

    @Mock
    private JvmServiceImpl impl;
    @Mock
    private JvmControlService controlImpl;
    @Mock
    private StateService<Jvm, JvmState> jvmStateService;
    @Mock
    private AuthenticatedUser authenticatedUser;
    @Mock
    private ResourceService resourceService;

    private Map<String, ReentrantReadWriteLock> writeLockMap;

    private JvmServiceRestImpl jvmServiceRest;

    private static List<Jvm> createJvmList() {
        final Set<LiteGroup> groups = new HashSet<>();
        final Jvm ws = new Jvm(Identifier.id(1L, Jvm.class),
                name,
                hostName,
                groups,
                Integer.valueOf(httpPort),
                Integer.valueOf(httpsPort),
                Integer.valueOf(redirectPort),
                Integer.valueOf(shutdownPort),
                Integer.valueOf(ajpPort),
                statusPath,
                systemProperties);
        final List<Jvm> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        writeLockMap = new HashMap<>();
        jvmServiceRest = new JvmServiceRestImpl(impl, controlImpl, jvmStateService, resourceService, Executors.newFixedThreadPool(12), writeLockMap);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));
    }

    @After
    public void cleanUp() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetJvmList() {
        when(impl.getJvms()).thenReturn(jvmList);

        final Response response = jvmServiceRest.getJvms();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<Jvm> receivedList = (List<Jvm>) content;
        final Jvm received = receivedList.get(0);
        assertEquals(jvm, received);
    }

    @Test
    public void testGetJvm() {
        when(impl.getJvm(any(Identifier.class))).thenReturn(jvm);

        final Response response = jvmServiceRest.getJvm(Identifier.id(1l, Jvm.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testCreateJvm() {
        when(impl.createJvm(any(CreateJvmRequest.class), any(User.class))).thenReturn(jvm);

        final JsonCreateJvm jsonCreateJvm = new JsonCreateJvm(name, hostName, httpPort, httpsPort, redirectPort,
                shutdownPort, ajpPort, statusPath.getUriPath(), systemProperties);
        final Response response = jvmServiceRest.createJvm(jsonCreateJvm, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testCreateJvmWithGroups() {
        when(impl.createAndAssignJvm(any(CreateJvmAndAddToGroupsRequest.class), any(User.class))).thenReturn(jvm);

        final Set<String> groupIds = new HashSet<>();
        groupIds.add("1");
        final JsonCreateJvm jsonCreateJvm = new JsonCreateJvm(name,
                hostName,
                groupIds,
                httpPort,
                httpsPort,
                redirectPort,
                shutdownPort,
                ajpPort,
                statusPath.getUriPath(),
                systemProperties);
        final Response response = jvmServiceRest.createJvm(jsonCreateJvm, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testUpdateJvm() {
        final Set<String> groupIds = new HashSet<>();
        final JsonUpdateJvm jsonUpdateJvm = new JsonUpdateJvm("1", name, hostName, groupIds, "5", "4", "3", "2", "1",
                statusPath.getUriPath(), systemProperties);
        when(impl.updateJvm(any(UpdateJvmRequest.class), any(User.class))).thenReturn(jvm);

        // Check rules for the JVM
        UpdateJvmRequest updateJvmCommand = jsonUpdateJvm.toUpdateJvmCommand();
        updateJvmCommand.validate();
        updateJvmCommand.hashCode();
        updateJvmCommand.equals(jsonUpdateJvm.toUpdateJvmCommand());
        String check = updateJvmCommand.toString();

        final Response response = jvmServiceRest.updateJvm(jsonUpdateJvm, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testRemoveJvm() {
        when(impl.isJvmStarted(any(Jvm.class))).thenReturn(false);
        when(impl.getJvm(jvm.getId())).thenReturn(jvm);

        when(controlImpl.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        when(jvmStateService.getCurrentState(jvm.getId())).thenReturn(new CurrentState<Jvm, JvmState>(jvm.getId(), JvmState.JVM_STOPPED, DateTime.now(), StateType.JVM));
        Response response = jvmServiceRest.removeJvm(jvm.getId(), authenticatedUser);
        verify(impl, atLeastOnce()).removeJvm(jvm.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);

        when(impl.isJvmStarted(any(Jvm.class))).thenReturn(true);
        boolean exceptionThrown = false;
        try {
            response = jvmServiceRest.removeJvm(jvm.getId(), authenticatedUser);
        } catch (Exception e) {
            assertEquals("The target JVM must be stopped before attempting to delete it", e.getMessage());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testControlJvm() {
        when(controlImpl.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        final CommandOutput execData = mock(CommandOutput.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);

        final JsonControlJvm jsonControlJvm = new JsonControlJvm("start");
        final Response response = jvmServiceRest.controlJvm(Identifier.id(1l, Jvm.class), jsonControlJvm, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());


        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof CommandOutput);

        when(execDataReturnCode.wasSuccessful()).thenReturn(false);
        boolean exceptionThrown = false;

        when(controlImpl.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(execDataReturnCode, "", "Jvm Control Failed"));
        try {
            jvmServiceRest.controlJvm(Identifier.id(1l, Jvm.class), jsonControlJvm, authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
            assertEquals("Jvm Control Failed", e.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGetCurrentJvmStates() {
        Set<String> jvmIds = new HashSet<>();
        jvmIds.add(jvm.getId().getId() + "");
        JvmIdsParameterProvider jvmIdProvider = new JvmIdsParameterProvider(jvmIds);
        assertTrue(jvmServiceRest.getCurrentJvmStates(jvmIdProvider).hasEntity());

        when(jvmStateService.getCurrentStates()).thenReturn(new HashSet<CurrentState<Jvm, JvmState>>());
        JvmIdsParameterProvider mockJvmProvider = mock(JvmIdsParameterProvider.class);
        when(mockJvmProvider.valueOf()).thenReturn(new HashSet<Identifier<Jvm>>());
        Response response = jvmServiceRest.getCurrentJvmStates(mockJvmProvider);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testGenerateConfig() {
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        Response responseObj = jvmServiceRest.generateConfig(jvm.getJvmName());
        assertTrue(responseObj.hasEntity());

        when(impl.generateConfigFile(anyString(), anyString())).thenThrow(new TemplateNotFoundException("server.xml", mock(FileNotFoundException.class)));
        boolean exceptionThrown = false;
        try {
            jvmServiceRest.generateConfig(jvm.getJvmName());
        } catch (InternalErrorException e) {
            exceptionThrown = true;
            assertEquals(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessageResponseStatus());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGenerateAndDeployConfigExecutorService() throws CommandFailureException, IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(impl.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(impl.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        boolean failsScp = false;
        Response response = null;
        try {
            response = jvmServiceRest.generateAndDeployConf(jvm.getJvmName(), authenticatedUser);
        } catch (InternalErrorException e) {
            failsScp = true;
        }
        assertFalse(failsScp);
        assertTrue(response != null && response.hasEntity());
        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
    }

    @Test
    public void testGenerateAndDeployConfig() throws CommandFailureException, IOException {
        Collection<ResourceType> mockResourceTypes = new ArrayList<>();
        ResourceType mockResource = mock(ResourceType.class);
        mockResourceTypes.add(mockResource);
        CommandOutput commandOutput = mock(CommandOutput.class);
        RuntimeCommandBuilder runtimeCommandBuilder = mock(RuntimeCommandBuilder.class);
        RuntimeCommand mockRuntimeCommand = mock(RuntimeCommand.class);
        when(commandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(mockResource.getEntityType()).thenReturn("jvm");
        when(mockResource.getTemplateName()).thenReturn("ServerXMLTemplate.tpl");
        when(mockResource.getConfigFileName()).thenReturn("server.xml");
        when(impl.isJvmStarted(jvm)).thenReturn(false);
        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(commandOutput);

        when(controlImpl.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), authenticatedUser.getUser())).thenReturn(commandOutput);
        when(controlImpl.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DEPLOY_CONFIG_TAR), authenticatedUser.getUser())).thenReturn(commandOutput);
        when(controlImpl.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.INVOKE_SERVICE), authenticatedUser.getUser())).thenReturn(commandOutput);

        when(resourceService.getResourceTypes()).thenReturn(mockResourceTypes);
        when(mockRuntimeCommand.execute()).thenReturn(commandOutput);
        when(runtimeCommandBuilder.build()).thenReturn(mockRuntimeCommand);
        Jvm response = jvmServiceRest.generateAndDeployConf(jvm, authenticatedUser, runtimeCommandBuilder);
        assertEquals(response, jvm);

        CommandOutput mockExecDataFail = mock(CommandOutput.class);
        when(mockExecDataFail.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecDataFail.getStandardError()).thenReturn("ERROR");

        when(controlImpl.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.INVOKE_SERVICE), authenticatedUser.getUser())).thenReturn(mockExecDataFail);

        boolean exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployConf(jvm, authenticatedUser, runtimeCommandBuilder);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployConf(jvm, authenticatedUser, runtimeCommandBuilder);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(mockExecDataFail);
        exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployConf(jvm, authenticatedUser, runtimeCommandBuilder);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        ExecCommand execCommand = new ExecCommand("fail command");
        Throwable throwable = new JSchException("Failed scp");
        final CommandFailureException commandFailureException = new CommandFailureException(execCommand, throwable);
        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenThrow(commandFailureException);
        exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployConf(jvm, authenticatedUser, runtimeCommandBuilder);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(impl.isJvmStarted(jvm)).thenReturn(true);
        exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployConf(jvm, authenticatedUser, runtimeCommandBuilder);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName() + "null"));
    }

    @Test
    public void testGenerateAndDeployConfigFailControlService() throws CommandFailureException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(impl.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(impl.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        when(controlImpl.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAIL CONTROL SERVICE"));

        final Response response = jvmServiceRest.generateAndDeployConf(jvm.getJvmName(), authenticatedUser);
        assertEquals("com.siemens.cto.aem.common.exception.InternalErrorException: FAIL CONTROL SERVICE", ((Map) (((ApplicationResponse) response.getEntity()).getApplicationResponseContent())).get("message"));
    }

    @Test
    public void testGenerateAndDeployConfigFailSecureCopyService() throws CommandFailureException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(impl.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(impl.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAIL THE SERVICE SECURE COPY TEST"));

        when(controlImpl.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), authenticatedUser.getUser())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        final Response response = jvmServiceRest.generateAndDeployConf(jvm.getJvmName(), authenticatedUser);
        assertEquals("com.siemens.cto.aem.common.exception.InternalErrorException: Failed running command IOException", ((Map) (((ApplicationResponse) response.getEntity()).getApplicationResponseContent())).get("message"));
    }

    @Test
    public void testGenerateAndDeployConfigFailsRuntimeCommand() throws CommandFailureException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.generateConfigFile(jvm.getJvmName(), "server.xml")).thenReturn("<server>xml-content</server>");
        when(impl.generateConfigFile(jvm.getJvmName(), "context.xml")).thenReturn("<content>xml-content</content>");
        when(impl.generateConfigFile(jvm.getJvmName(), "setenv.bat")).thenReturn("SET TEST=xxtestxx");
        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAIL THE RUNTIME COMMAND TEST"));


        when(controlImpl.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), authenticatedUser.getUser())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        when(resourceService.getResourceTypes()).thenReturn(new ArrayList<ResourceType>());
        final Response response = jvmServiceRest.generateAndDeployConf(jvm.getJvmName(), authenticatedUser);
        assertEquals("com.siemens.cto.aem.common.exception.InternalErrorException: Failed running command IOException", ((Map) (((ApplicationResponse) response.getEntity()).getApplicationResponseContent())).get("message"));
    }

    @Test
    public void testDiagnoseJvm() {
        when(impl.performDiagnosis(jvm.getId())).thenReturn("Good Diagnosis!");
        Response response = jvmServiceRest.diagnoseJvm(jvm.getId());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceNames() {
        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("a resource name");
        when(impl.getResourceTemplateNames(jvm.getJvmName())).thenReturn(resourceNames);
        Response response = jvmServiceRest.getResourceNames(jvm.getJvmName());
        assertTrue(response.hasEntity());
    }

    @Test
    public void testGetResourceTemplate() {
        String resourceTemplateName = "template.tpl";
        when(impl.getResourceTemplate(jvm.getJvmName(), resourceTemplateName, false)).thenReturn("${jvm.jvmName");
        Response response = jvmServiceRest.getResourceTemplate(jvm.getJvmName(), resourceTemplateName, false);
        assertTrue(response.hasEntity());
    }

    @Test
    public void testUploadResourceTemplate() {
        Collection<ResourceType> resourceList = new ArrayList<>();
        final ResourceType mockResourceType = mock(ResourceType.class);
        when(mockResourceType.getConfigFileName()).thenReturn("jvmResource");
        when(mockResourceType.getEntityType()).thenReturn("jvm");
        when(mockResourceType.getTemplateName()).thenReturn("ServerXMLTemplate.tpl");
        resourceList.add(mockResourceType);
        when(resourceService.getResourceTypes()).thenReturn(resourceList);
        jvmServiceRest.uploadAllJvmResourceTemplates(authenticatedUser, jvm);
        verify(impl, times(1)).uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class), any(User.class));
    }

    @Test
    public void testUploadConfigTemplate() {
        MessageContext mockContext = mock(MessageContext.class);
        HttpHeaders mockHttpHeaders = mock(HttpHeaders.class);
        HttpServletRequest mockHttpServletReq = mock(HttpServletRequest.class);
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHttpHeaders.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(mockContext.getHttpHeaders()).thenReturn(mockHttpHeaders);
        when(mockContext.getHttpServletRequest()).thenReturn(mockHttpServletReq);
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        jvmServiceRest.setContext(mockContext);
        try {
            Response response = jvmServiceRest.uploadConfigTemplate(jvm.getJvmName(), authenticatedUser, "server.xml");
            assertNotNull(response.getEntity());
        } catch (Exception e) {
            assertEquals("Error receiving data", e.getMessage());
        }
    }

    @Test
    public void testGenerateAndDeployFile() throws CommandFailureException, IOException {
        Collection<ResourceType> resourceTypes = new ArrayList<>();
        ResourceType mockResourceType = mock(ResourceType.class);
        resourceTypes.add(mockResourceType);
        CommandOutput mockExecData = mock(CommandOutput.class);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(mockResourceType.getTemplateName()).thenReturn("ServerXMLTemplate.tpl");
        when(mockResourceType.getEntityType()).thenReturn("jvm");
        when(mockResourceType.getConfigFileName()).thenReturn("server.xml");
        when(impl.getJvm(jvm.getJvmName())).thenReturn(jvm);
        when(impl.isJvmStarted(jvm)).thenReturn(false);
        when(resourceService.getResourceTypes()).thenReturn(resourceTypes);
        when(impl.generateConfigFile(anyString(),anyString())).thenReturn("<server>xml</server>");
        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(mockExecData);
        Response response = jvmServiceRest.generateAndDeployFile(jvm.getJvmName(), "server.xml", authenticatedUser);
        assertNotNull(response.hasEntity());

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecData.getStandardError()).thenReturn("ERROR");
        when(controlImpl.secureCopyFile(any(ControlJvmRequest.class), anyString(), anyString())).thenReturn(mockExecData);
        boolean exceptionThrown = false;
        try {
            jvmServiceRest.generateAndDeployFile(jvm.getJvmName(), "server.xml", authenticatedUser);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        FileUtils.deleteDirectory(new File("./" + jvm.getJvmName()));
    }

    @Test
    public void testUpdateResourceTemplate() {
        final String updateValue = "<server>update</server>";
        when(impl.updateResourceTemplate(anyString(), anyString(), anyString())).thenReturn(updateValue);
        Response response = jvmServiceRest.updateResourceTemplate(jvm.getJvmName(), "ServerXMLTemplate.tpl", updateValue);
        assertNotNull(response.getEntity());

        when(impl.updateResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException(jvm.getJvmName(), "server.xml"));
        response = jvmServiceRest.updateResourceTemplate(jvm.getJvmName(), "ServerXMLTemplate.tpl", updateValue);
        assertNotNull(response.getEntity());
    }

    @Test
    public void testPreviewResourceTemplate() {
        when(impl.previewResourceTemplate(anyString(), anyString(), anyString())).thenReturn("<server>preview</server>");
        Response response = jvmServiceRest.previewResourceTemplate(jvm.getJvmName(), "group1", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());

        when(impl.previewResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Test failed preview"));
        response = jvmServiceRest.previewResourceTemplate(jvm.getJvmName(), "group1", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());
    }
}
