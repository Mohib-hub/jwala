package com.siemens.cto.aem.persistence.dao.webserver;

import com.siemens.cto.aem.request.group.CreateGroupRequest;
import com.siemens.cto.aem.request.webserver.*;
import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.request.webserver.UpdateWebServerRequest;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

import static com.siemens.cto.aem.persistence.dao.group.GroupEventsTestHelper.createCreateGroupEvent;
import static com.siemens.cto.aem.persistence.dao.webserver.WebServerEventsTestHelper.createCreateWebServerEvent;
import static com.siemens.cto.aem.persistence.dao.webserver.WebServerEventsTestHelper.createUpdateWebServerEvent;
import static org.junit.Assert.*;

@Transactional
public abstract class AbstractWebServerDaoIntegrationTest {

    @Autowired
    private WebServerDao webServerDao;

    @Autowired
    private GroupDao groupDao;

    private WebServer preCreatedWebServer;
    private Group preCreatedGroup;

    private Collection<Identifier<Group>> preCreatedGroupIds;

    private String userName;
    private static final String TEST_WS_NAME = "Tomcat Operations Center TEST";
    private static final Integer TEST_WS_PORT = 8080;
    private static final Integer TEST_WS_HTTPS_PORT = 8009;
    private static final String TEST_WS_HOST = "localhost";
    private static final String TEST_WS_GROUP_NAME = "test group";
    private static final String TEST_USER_NAME = "Auto-constructed User ";
    private static final String UNCHECKED_WS_NAME = "noname";
    private static final Integer UNCHECKED_WS_PORT = 1023;
    private static final Integer UNCHECKED_WS_HTTPS_PORT = 1024;
    private static final String UNCHECKED_WS_HOST = "nohost";
    private static final Long NONEXISTANT_WS_ID = -123456L;
    private static final Long NONEXISTANT_GROUP_ID = -123456L;
    private static final Identifier<Group> NONEXISTANT_GROUP = new Identifier<>(NONEXISTANT_GROUP_ID);
    private static final Collection<Identifier<Group>> NONEXISTANT_GROUP_IDS = new ArrayList<>(1);
    private static final String UNIQUE_NEW_WS_NAME = "Web Server Name to turn into a duplicate";
    private static final String SECOND_WS_GROUP_NAME = "test group 2";
    private static final String SECOND_TEST_WS_NAME = "TOC Test 2";
    private static final Path STATUS_PATH = new Path("/statusPath");
    private static final FileSystemPath HTTP_CONFIG_FILE = new FileSystemPath("d:/some-dir/httpd.conf");
    private static final Path SVR_ROOT = new Path("./");
    private static final Path DOC_ROOT = new Path("htdocs");

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Before
    public void setUp() throws Exception {

        preCreatedGroup = groupDao.createGroup(createCreateGroupEvent(
                TEST_WS_GROUP_NAME, TEST_USER_NAME));

        preCreatedGroupIds = new ArrayList<>(1);
        preCreatedGroupIds.add(preCreatedGroup.getId());

        preCreatedWebServer = webServerDao
                .createWebServer(createCreateWebServerEvent(
                        preCreatedGroupIds, TEST_WS_NAME, TEST_WS_HOST,
                        TEST_WS_PORT, TEST_WS_HTTPS_PORT, userName,
                        STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));
    }

    static {
        NONEXISTANT_GROUP_IDS.add(NONEXISTANT_GROUP);
    }

    @Test
    public void testCreateWebServer() {
        assertEquals(TEST_WS_NAME, preCreatedWebServer.getName());
        assertNotNull(preCreatedWebServer.getId());
    }

    @Test(expected = BadRequestException.class)
    public void testCreateDuplicateWebServer() {

        final Event<CreateWebServerRequest> createWebServer = createCreateWebServerEvent(
                preCreatedGroupIds, TEST_WS_NAME, UNCHECKED_WS_HOST,
                UNCHECKED_WS_PORT, TEST_WS_HTTPS_PORT, userName,
                STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT);

        webServerDao.createWebServer(createWebServer);
    }

    @Test
    public void testUpdateWebServer() {

        final Event<UpdateWebServerRequest> updateWebServer = createUpdateWebServerEvent(
                preCreatedWebServer.getId(), preCreatedGroupIds,
                "My New Name", "My New Host", 1, 2, userName,
                STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT);

        final WebServer actualWebServer = webServerDao
                .updateWebServer(updateWebServer);

        assertEquals(updateWebServer.getRequest().getNewName(),
                actualWebServer.getName());
        assertEquals(updateWebServer.getRequest().getNewHost(),
                actualWebServer.getHost());
        assertEquals(updateWebServer.getRequest().getNewPort(),
                actualWebServer.getPort());
        assertEquals(updateWebServer.getRequest().getNewHttpsPort(),
                actualWebServer.getHttpsPort());
        assertEquals(updateWebServer.getRequest().getId(),
                actualWebServer.getId());
        assertEquals(updateWebServer.getRequest().getNewHttpConfigFile(), actualWebServer.getHttpConfigFile());

    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistent() {

        final Identifier<WebServer> nonExistentWebServerId = new Identifier<>(
                NONEXISTANT_WS_ID);

        webServerDao.updateWebServer(createUpdateWebServerEvent(
                nonExistentWebServerId, preCreatedGroupIds,
                UNCHECKED_WS_NAME, UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, UNCHECKED_WS_HTTPS_PORT,
                userName,
                STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateDuplicateWebServer() {

        final WebServer newWebServer = webServerDao
                .createWebServer(createCreateWebServerEvent(
                        preCreatedGroupIds, UNIQUE_NEW_WS_NAME,
                        UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, UNCHECKED_WS_HTTPS_PORT, userName,
                        STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));

        webServerDao.updateWebServer(createUpdateWebServerEvent(
                newWebServer.getId(), preCreatedGroupIds,
                preCreatedWebServer.getName(), UNCHECKED_WS_HOST, UNCHECKED_WS_HTTPS_PORT,
                UNCHECKED_WS_PORT, userName,
                STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));
    }

    @Test
    public void testGetWebServer() {

        final Identifier<WebServer> expectedWebServerIdentifier = preCreatedWebServer
                .getId();

        final WebServer webServer = webServerDao
                .getWebServer(expectedWebServerIdentifier);

        assertTrue(webServer.getGroupIds().containsAll(preCreatedGroupIds));
        assertEquals(preCreatedGroupIds.size(), webServer.getGroups().size());

        assertEquals(preCreatedWebServer.getName(), webServer.getName());
        assertEquals(preCreatedWebServer.getHost(), webServer.getHost());
        assertEquals(preCreatedWebServer.getPort(), webServer.getPort());
        assertEquals(preCreatedWebServer.getStatusPath(), webServer.getStatusPath());
        assertEquals(preCreatedWebServer.getHttpConfigFile(), webServer.getHttpConfigFile());
        assertEquals(expectedWebServerIdentifier, webServer.getId());
        assertEquals(preCreatedWebServer, webServer);
        assertEquals(preCreatedWebServer.hashCode(), webServer.hashCode());
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistentWebServer() {

        webServerDao.getWebServer(new Identifier<WebServer>(NONEXISTANT_WS_ID));
    }

    @Test
    public void testGetWebServers() {

        for (int i = 0; i <= 5; i++) {
            webServerDao.createWebServer(createCreateWebServerEvent(
                    preCreatedGroupIds, TEST_WS_NAME + (i + 1),
                    UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, UNCHECKED_WS_HTTPS_PORT, TEST_USER_NAME + (i + 1),
                    STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));
        }

        final List<WebServer> actualWebServers = webServerDao.getWebServers();

        assertTrue(actualWebServers.size() > 0);
    }

    @Test
    public void testFindWebServers() {

        final String expectedContains = preCreatedWebServer.getName()
                .substring(3, 5);

        final List<WebServer> actualWebServers = webServerDao.findWebServers(expectedContains);

        for (final WebServer WebServer : actualWebServers) {
            assertTrue(WebServer.getName().contains(expectedContains));
        }
    }

    @Test
    public void testRemoveWebServer() {

        final Identifier<WebServer> webServerId = preCreatedWebServer.getId();

        webServerDao.removeWebServer(webServerId);

        try {
            webServerDao.getWebServer(webServerId);
        } catch (final NotFoundException nfe) {
            // Success
            return;
        }
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistent() {

        final Identifier<WebServer> nonExistentWebServerId = new Identifier<>(
                NONEXISTANT_WS_ID);

        webServerDao.removeWebServer(nonExistentWebServerId);
    }

    @Test(expected = NotFoundException.class)
    public void testWebServerWithNotFoundGroup() {
        WebServer webServer = webServerDao
                .createWebServer(createCreateWebServerEvent(NONEXISTANT_GROUP_IDS,
                        TEST_WS_NAME, TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT,
                        TEST_USER_NAME,
                        STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));

        assertNotNull(webServer.getId());
        assertNotNull(webServer.getId().getId());
    }

    @Test
    public void testWebServerWithNullGroup() {
        WebServer webServer = webServerDao
                .createWebServer(createCreateWebServerEvent(null, SECOND_TEST_WS_NAME,
                        TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT, TEST_USER_NAME,
                        STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));

        assertNotNull(webServer.getId());
        assertNotNull(webServer.getId().getId());
    }

    @Test
    public void testWebServerWithEmptyGroup() {
        WebServer webServer = webServerDao
                .createWebServer(createCreateWebServerEvent(new ArrayList<Identifier<Group>>(0), SECOND_TEST_WS_NAME,
                        TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT, TEST_USER_NAME,
                        STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));

        assertNotNull(webServer.getId());
        assertNotNull(webServer.getId().getId());
    }

    @Test
    public void testUpdateWebServerGroup() {
        Group newGroup = groupDao.createGroup(createCreateGroupEvent(
                SECOND_WS_GROUP_NAME, TEST_USER_NAME));

        ArrayList<Identifier<Group>> newGroupIds = new ArrayList<>(1);
        newGroupIds.add(newGroup.getId());

        WebServer webServer = webServerDao
                .updateWebServer(createUpdateWebServerEvent(
                        preCreatedWebServer.getId(), newGroupIds,
                        TEST_WS_NAME, TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT,
                        TEST_USER_NAME,
                        STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));

        assertTrue(webServer.getGroupIds().containsAll(newGroupIds));

        assertEquals(newGroupIds.size(), webServer.getGroups().size());

        assertEquals(newGroup.getName(), webServer.getGroups().iterator().next().getName());
    }

    class GeneralizedDao {
        <R> R update(Event<?> updateCommand) {
            return this.updateInternal(updateCommand.getRequest(),
                    updateCommand);
        }

        @SuppressWarnings("unchecked")
        private <R> R updateInternal(Object updateCommand,
                                     @SuppressWarnings("rawtypes") Event eventObj) {
            if (updateCommand instanceof UpdateGroupRequest) {
                return (R) groupDao.updateGroup(eventObj);
            } else if (updateCommand instanceof UpdateWebServerRequest) {
                return (R) webServerDao.updateWebServer(eventObj);
            }
            return null;
        }

        <R> R create(Event<?> createCommand) {
            return this.createInternal(createCommand.getRequest(),
                    createCommand);
        }

        @SuppressWarnings({"unchecked"})
        private <R> R createInternal(Object createCommand,
                                     @SuppressWarnings("rawtypes") Event eventObj) {
            if (createCommand instanceof CreateGroupRequest) {
                return (R) groupDao.createGroup(eventObj);
            } else if (createCommand instanceof CreateWebServerRequest) {
                return (R) webServerDao.createWebServer(eventObj);
            }
            return null;
        }
    }

    @Test
    public void testGeneralDao() {
        GeneralizedDao generalizedDao = new GeneralizedDao();
        Group newGroup = generalizedDao.create(createCreateGroupEvent(
                SECOND_WS_GROUP_NAME, TEST_USER_NAME));

        ArrayList<Identifier<Group>> newGroupIds = new ArrayList<>(1);
        newGroupIds.add(newGroup.getId());

        WebServer webServer = generalizedDao.update(createUpdateWebServerEvent(
                preCreatedWebServer.getId(), newGroupIds, TEST_WS_NAME,
                TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT, TEST_USER_NAME,
                STATUS_PATH, HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT));

        assertTrue(webServer.getGroupIds().containsAll(newGroupIds));
        assertEquals(newGroupIds.size(), webServer.getGroups().size());

        assertEquals(newGroup.getName(), webServer.getGroups().iterator().next().getName());
    }

    @Test
    public void testFindApplicationsBelongingToWebServer() {
        final Integer testMethodHash = "testFindApplicationsBelongingToWebServer".hashCode();
        final String GROUP_NAME_PREFIX = "group" + testMethodHash;
        final String GROUP_A_NAME = GROUP_NAME_PREFIX + "A";
        final String GROUP_B_NAME = GROUP_NAME_PREFIX + "B";
        final String GROUP_C_NAME = GROUP_NAME_PREFIX + "C";

        final String WEB_SERVER_NAME = "webServer" + testMethodHash;

        final String APP_PREFIX = "app" + testMethodHash;
        final String APP_1_NAME = APP_PREFIX + "1";
        final String APP_2_NAME = APP_PREFIX + "2";
        final String APP_3_NAME = APP_PREFIX + "3";
        final String APP_4_NAME = APP_PREFIX + "4";
        final String APP_5_NAME = APP_PREFIX + "5";

        // Create groups A, B and C
        final JpaGroup jpaGroupA = new JpaGroup();
        jpaGroupA.setName(GROUP_A_NAME);
        entityManager.persist(jpaGroupA);

        final JpaGroup jpaGroupB = new JpaGroup();
        jpaGroupB.setName(GROUP_B_NAME);
        entityManager.persist(jpaGroupB);

        final JpaGroup jpaGroupC = new JpaGroup();
        jpaGroupC.setName(GROUP_C_NAME);
        entityManager.persist(jpaGroupC);

        final List<JpaGroup> groups = new ArrayList<>();
        groups.add(jpaGroupA);
        groups.add(jpaGroupB);

        // Create the web server
        final JpaWebServer jpaWebServer = new JpaWebServer();
        jpaWebServer.setName(WEB_SERVER_NAME);
        jpaWebServer.setGroups(groups);
        jpaWebServer.setHost("the-host-name");
        jpaWebServer.setPort(80);
        jpaWebServer.setStatusPath("/jk/status");
        jpaWebServer.setHttpConfigFile("d:/some-dir/httpd.conf");
        jpaWebServer.setDocRoot(DOC_ROOT.getPath());
        jpaWebServer.setSvrRoot(SVR_ROOT.getPath());
        entityManager.persist(jpaWebServer);

        // Create the applications 1, 2, 3, 4 and 5
        final JpaApplication jpaApp1 = new JpaApplication();
        jpaApp1.setName(APP_1_NAME);
        jpaApp1.setGroup(jpaGroupA);
        jpaApp1.setWebAppContext("/app1");
        entityManager.persist(jpaApp1);

        final JpaApplication jpaApp2 = new JpaApplication();
        jpaApp2.setName(APP_2_NAME);
        jpaApp2.setGroup(jpaGroupA);
        jpaApp2.setWebAppContext("/app2");
        entityManager.persist(jpaApp2);

        final JpaApplication jpaApp3 = new JpaApplication();
        jpaApp3.setName(APP_3_NAME);
        jpaApp3.setGroup(jpaGroupB);
        jpaApp3.setWebAppContext("/app3");
        entityManager.persist(jpaApp3);

        final JpaApplication jpaApp4 = new JpaApplication();
        jpaApp4.setName(APP_4_NAME);
        jpaApp4.setGroup(jpaGroupC);
        jpaApp4.setWebAppContext("/app4");
        entityManager.persist(jpaApp4);

        final JpaApplication jpaApp5 = new JpaApplication();
        jpaApp5.setName(APP_5_NAME);
        jpaApp5.setGroup(jpaGroupB);
        jpaApp5.setWebAppContext("/app5");
        entityManager.persist(jpaApp5);

        entityManager.flush();

        final List<Application> applications =
                webServerDao.findApplications(jpaWebServer.getName());

        assertEquals(4, applications.size());

        final List<String> contextList = Arrays.asList("/app1", "/app2", "/app3", "/app5");
        final List<String> generatedContextList = new ArrayList<>();

        for (final Application app : applications) {
            generatedContextList.add(app.getWebAppContext());
        }

        Collections.sort(generatedContextList);
        assertEquals(contextList.toString(), generatedContextList.toString());
    }

    @Test
    public void testFindWebServerByName() {
        final int methodHash = "testFindWebServerByName".hashCode();
        final String WS_NAME = "webserver" + methodHash;

        final JpaWebServer jpaWebServer = new JpaWebServer();
        jpaWebServer.setName(WS_NAME);
        jpaWebServer.setHost("the-host-name");
        jpaWebServer.setPort(80);
        jpaWebServer.setStatusPath("/jk/status");
        jpaWebServer.setHttpConfigFile("d:/some-dir/httpd.conf");
        jpaWebServer.setDocRoot(DOC_ROOT.getPath());
        jpaWebServer.setSvrRoot(SVR_ROOT.getPath());
        entityManager.persist(jpaWebServer);
        entityManager.flush();

        final WebServer ws = webServerDao.findWebServerByName(WS_NAME);
        assertEquals(WS_NAME, ws.getName());
    }

    /**
     * Add JVM to group
     *
     * @param jpaJvm   JVM to add
     * @param jpaGroup group to add to
     */
    private void addJvmToGroup(JpaJvm jpaJvm, JpaGroup jpaGroup) {
        Query q = entityManager.createNativeQuery("INSERT INTO GRP_JVM (GROUP_ID, JVM_ID) VALUES (?, ?)");
        q.setParameter(1, jpaGroup.getId());
        q.setParameter(2, jpaJvm.getId());
        q.executeUpdate();
    }

    @Test
    public void testFindJvms() {
        final Integer testMethodHash = "testFindJvms".hashCode();
        final String GROUP_NAME_PREFIX = "group" + testMethodHash;
        final String GROUP_A_NAME = GROUP_NAME_PREFIX + "A";
        final String GROUP_B_NAME = GROUP_NAME_PREFIX + "B";
        final String GROUP_C_NAME = GROUP_NAME_PREFIX + "C";

        final String WEB_SERVER_NAME = "webServer" + testMethodHash;

        final String JVM_PREFIX = "jvm" + testMethodHash;
        final String JVM_1_NAME = JVM_PREFIX + "1";
        final String JVM_2_NAME = JVM_PREFIX + "2";
        final String JVM_3_NAME = JVM_PREFIX + "3";
        final String JVM_4_NAME = JVM_PREFIX + "4";
        final String JVM_5_NAME = JVM_PREFIX + "5";

        final String APP_PREFIX = "app" + testMethodHash;
        final String APP_1_NAME = APP_PREFIX + "1";
        final String APP_2_NAME = APP_PREFIX + "2";
        final String APP_3_NAME = APP_PREFIX + "3";
        final String APP_4_NAME = APP_PREFIX + "4";
        final String APP_5_NAME = APP_PREFIX + "5";

        // Create groups A, B and C
        final JpaGroup jpaGroupA = new JpaGroup();
        jpaGroupA.setName(GROUP_A_NAME);
        entityManager.persist(jpaGroupA);

        final JpaGroup jpaGroupB = new JpaGroup();
        jpaGroupB.setName(GROUP_B_NAME);
        entityManager.persist(jpaGroupB);

        final JpaGroup jpaGroupC = new JpaGroup();
        jpaGroupC.setName(GROUP_C_NAME);
        entityManager.persist(jpaGroupC);

        final List<JpaGroup> groups = new ArrayList<>();
        groups.add(jpaGroupA);
        groups.add(jpaGroupB);

        // Create the web server
        final JpaWebServer jpaWebServer = new JpaWebServer();
        jpaWebServer.setName(WEB_SERVER_NAME);
        jpaWebServer.setHost("the-host-name");
        jpaWebServer.setPort(80);
        jpaWebServer.setGroups(groups);
        jpaWebServer.setStatusPath("/jk/status");
        jpaWebServer.setHttpConfigFile("d:/some-dir/httpd.conf");
        jpaWebServer.setDocRoot(DOC_ROOT.getPath());
        jpaWebServer.setSvrRoot(SVR_ROOT.getPath());
        entityManager.persist(jpaWebServer);

        // Create jvm 1, 2, 3, 4 and 5
        final JpaJvm jpaJvm1 = new JpaJvm();
        jpaJvm1.setName(JVM_1_NAME);
        jpaJvm1.setHttpPort(8080);
        jpaJvm1.setAjpPort(8009);
        jpaJvm1.setRedirectPort(443);
        jpaJvm1.setShutdownPort(8005);
        jpaJvm1.setStatusPath("/abc");
        entityManager.persist(jpaJvm1);

        addJvmToGroup(jpaJvm1, jpaGroupA);
        addJvmToGroup(jpaJvm1, jpaGroupB);

        final JpaJvm jpaJvm2 = new JpaJvm();
        jpaJvm2.setName(JVM_2_NAME);
        jpaJvm2.setHttpPort(8080);
        jpaJvm2.setAjpPort(8009);
        jpaJvm2.setRedirectPort(443);
        jpaJvm2.setShutdownPort(8005);
        jpaJvm2.setStatusPath("/abc");
        entityManager.persist(jpaJvm2);

        addJvmToGroup(jpaJvm2, jpaGroupC);

        final JpaJvm jpaJvm3 = new JpaJvm();
        jpaJvm3.setName(JVM_3_NAME);
        jpaJvm3.setHttpPort(8080);
        jpaJvm3.setAjpPort(8009);
        jpaJvm3.setRedirectPort(443);
        jpaJvm3.setShutdownPort(8005);
        jpaJvm3.setStatusPath("/abc");
        entityManager.persist(jpaJvm3);

        addJvmToGroup(jpaJvm3, jpaGroupB);
        addJvmToGroup(jpaJvm3, jpaGroupC);

        final JpaJvm jpaJvm4 = new JpaJvm();
        jpaJvm4.setName(JVM_4_NAME);
        jpaJvm4.setHttpPort(8080);
        jpaJvm4.setAjpPort(8009);
        jpaJvm4.setRedirectPort(443);
        jpaJvm4.setShutdownPort(8005);
        jpaJvm4.setStatusPath("/abc");
        entityManager.persist(jpaJvm4);

        addJvmToGroup(jpaJvm4, jpaGroupA);
        addJvmToGroup(jpaJvm4, jpaGroupB);

        final JpaJvm jpaJvm5 = new JpaJvm();
        jpaJvm5.setName(JVM_5_NAME);
        jpaJvm5.setHttpPort(8080);
        jpaJvm5.setAjpPort(8009);
        jpaJvm5.setRedirectPort(443);
        jpaJvm5.setShutdownPort(8005);
        jpaJvm5.setStatusPath("/abc");
        entityManager.persist(jpaJvm5);

        addJvmToGroup(jpaJvm5, jpaGroupC);

        // Create the applications 1, 2, 3, 4 and 5
        final JpaApplication jpaApp1 = new JpaApplication();
        jpaApp1.setName(APP_1_NAME);
        jpaApp1.setGroup(jpaGroupA);
        jpaApp1.setWebAppContext("/app1");
        entityManager.persist(jpaApp1);

        final JpaApplication jpaApp2 = new JpaApplication();
        jpaApp2.setName(APP_2_NAME);
        jpaApp2.setGroup(jpaGroupA);
        jpaApp2.setWebAppContext("/app2");
        entityManager.persist(jpaApp2);

        final JpaApplication jpaApp3 = new JpaApplication();
        jpaApp3.setName(APP_3_NAME);
        jpaApp3.setGroup(jpaGroupB);
        jpaApp3.setWebAppContext("/app3");
        entityManager.persist(jpaApp3);

        final JpaApplication jpaApp4 = new JpaApplication();
        jpaApp4.setName(APP_4_NAME);
        jpaApp4.setGroup(jpaGroupC);
        jpaApp4.setWebAppContext("/app4");
        entityManager.persist(jpaApp4);

        final JpaApplication jpaApp5 = new JpaApplication();
        jpaApp5.setName(APP_5_NAME);
        jpaApp5.setGroup(jpaGroupB);
        jpaApp5.setWebAppContext("/app5");
        entityManager.persist(jpaApp5);

        entityManager.flush();

        final List<Jvm> jvms =
                webServerDao.findJvms(WEB_SERVER_NAME);

        assertEquals(3, jvms.size());

        final List<String> jvmNameList = Arrays.asList(JVM_1_NAME, JVM_3_NAME, JVM_4_NAME);
        final List<String> generatedJvmNameList = new ArrayList<>();

        for (final Jvm jvm : jvms) {
            generatedJvmNameList.add(jvm.getJvmName());
        }

        Collections.sort(generatedJvmNameList);
        assertEquals(jvmNameList.toString(), generatedJvmNameList.toString());
    }

    @Test
    @Ignore
    // TODO: Fix this test! Double check if data us correct. Can't find any problem with the code (JPA).
    public void testFindWebServersBelongingToGroup() {
        final List<WebServer> webServers =
                webServerDao.findWebServersBelongingTo(preCreatedGroup.getId());
        assertTrue(webServers.size() > 0);
    }

    @Test
    public void testGetResourceTemplateNames() {
        List<String> resultList = webServerDao.getResourceTemplateNames(TEST_WS_NAME);
        assertTrue("Template was not added so should be empty", resultList.isEmpty());
    }

    @Test
    public void testGetResourceTemplate() {
        String templateText = "";
        boolean exceptionThrown = false;
        try {
            templateText = webServerDao.getResourceTemplate(TEST_WS_NAME, "httpd.conf");
        }
        catch (NonRetrievableResourceTemplateContentException e) {
            exceptionThrown = true;
        }
        assertTrue("Template will return empty since it was not uploaded", templateText.isEmpty());
        assertTrue("Template not uploaded to database will throw exception on retrieval", exceptionThrown);
    }

    @Test
    public void testPopulateWebserverConfig() {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        UploadWebServerTemplateCommandBuilder builder = new UploadWebServerTemplateCommandBuilder();
        ArrayList<UploadWebServerTemplateRequest> uploadWSTemplateCommands = new ArrayList<>();
        uploadWSTemplateCommands.add(builder.buildHttpdConfCommand(preCreatedWebServer));
        final boolean overwriteExisting = false;
        webServerDao.populateWebServerConfig(uploadWSTemplateCommands, new User(TEST_USER_NAME), overwriteExisting);

        String templateText = webServerDao.getResourceTemplate(TEST_WS_NAME, "httpd.conf");
        assertTrue("Template text was returned after uploading the file to the database", !templateText.isEmpty());
        System.clearProperty(AemConstants.PROPERTIES_ROOT_PATH);
    }
}
