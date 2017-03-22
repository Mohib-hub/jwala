package com.cerner.jwala.common.properties;

/**
 * Created by Steven Ger on 12/21/16.
 */
public enum PropertyKeys {

    REMOTE_JAWALA_DATA_DIR("remote.jwala.data.dir"),
    APACHE_HTTPD_FILE_NAME("jwala.apache.httpd.zip.name"),
    REMOTE_PATHS_APACHE_HTTPD("remote.paths.apache.httpd"),
    REMOTE_PATHS_APACHE_HTTPD_CONF("remote.paths.httpd.conf"),
    REMOTE_PATHS_HTTPD_ROOT_DIR_NAME("remote.paths.httpd.root.dir.name"),
    SCRIPTS_PATH("commands.scripts-path"),
    REMOTE_TOMCAT_DIR_NAME("remote.tomcat.dir.name"),
    TOMCAT_BINARY_FILE_NAME("jwala.tomcat.zip.name"),
    REMOTE_PATH_INSTANCES_DIR("remote.paths.instances"),
    REMOTE_PATHS_DEPLOY_DIR("remote.paths.deploy.dir"),
    REMOTE_SCRIPT_DIR("remote.commands.user-scripts"),
    REMOTE_JAVA_HOME("remote.jwala.java.home"),
    REMOTE_JWALA_JAVA_ROOT_DIR_NAME("remote.jwala.java.root.dir.name"),
    JDK_BINARY_FILE_NAME("jwala.default.jdk.zip"),
    REMOTE_PATHS_TOMCAT_ROOT_CORE("remote.paths.tomcat.root.core"),
    REMOTE_PATHS_TOMCAT_CORE("remote.paths.tomcat.core"),
    LOCAL_JWALA_BINARY_DIR("jwala.binary.dir"),
    TOMCAT_MANAGER_XML_SSL_PATH("tomcat.manager.xml.ssl.path"),
    PATHS_WEB_ARCHIVE("paths.web-archive"),
    PATHS_RESOURCE_TEMPLATES("paths.resource-templates"),
    AEM_SSH_USER_NAME("AemSsh.userName"),
    AEM_SSH_PORT("AemSsh.port"),
    AEM_SSH_ENCRYPTED_PASSWORD("AemSsh.encrypted.password"),
    AEM_SSH_PRIVATE_KEY_FILE("AemSsh.privateKeyFile"),
    AEM_SSH_KNOWN_HOST_FILE("AemSsh.knownHostsFile"),
    ACTIVE_DIRECTORY_FQDN("active.directory.fqdn"),
    ACTIVE_DIRECTORY_DOMAIN("active.directory.domain"),
    ACTIVE_DIRECTORY_SERVER_NAME("active.directory.server.name"),
    ACTIVE_DIRECTORY_SERVER_PORT("active.directory.server.port"),
    ACTIVE_DIRECTORY_SERVER_PROTOCOL("active.directory.server.protocol"),
    REMOTE_JVM_ACCOUNT_USERNAME("remote.jvm.account.username"),
    REMOTE_JVM_ACCOUNT_PASSWORD_ENCRYPTED("remote.jvm.account.password.encrypted"),
    REMOTE_JWALA_WEBAPPS_DIR("remote.jwala.webapps.dir"),
    JMAP_DUMP_LIVE_ENABLED("jmap.dump.live.enabled"),
    NET_STOP_SLEEP_TIME_SECONDS("net.stop.sleep.time.seconds"),
    REMOTE_JWALA_EXECUTION_TIMEOUT_SECONDS("remote.jwala.execution.timeout.seconds"),
    PING_JVM_PERIOD_MILLIS("ping.jvm.period.millis"),
    WEBSERVER_THREAD_TASK_EXECUTOR_POOL_SIZE("webserver.thread-task-executor.pool.size"),
    WEBSERVER_THREAD_TASK_EXECUTOR_POOL_MAX_SIZE("webserver.thread-task-executor.pool.max-size"),
    WEBSERVER_THREAD_TASK_EXECUTOR_POOL_QUEUE_CAPACITY("webserver.thread-task-executor.pool.queue-capacity"),
    WEBSERVER_THREAD_TASK_EXECUTOR_POOL_KEEP_ALIVE_SEC("webserver.thread-task-executor.pool.keep-alive-sec"),
    COMMANDS_CONCURRENT_MIN("commands.concurrent.min"),
    COMMANDS_CONCURRENT_MAX("commands.concurrent.max"),
    COMMANDS_CONCURRENT_QUEUE_CAPACITY("commands.concurrent.queue-capacity"),
    COMMANDS_CONCURRENT_NOMINAL("commands.concurrent.nominal"),
    JGROUPS_JAVA_NET_PREFER_IPV4_STACK("jgroups.java.net.preferIPv4Stack"),
    JGROUPS_COORDINATOR_IP_ADDRESS("jgroups.coordinator.ip.address"),
    JGROUPS_COORDINATOR_PORT("jgroups.coordinator.port"),
    JGROUPS_CLUSTER_CONNECT_TIMEOUT("jgroups.cluster.connect.timeout"),
    JGROUPS_CLUSTER_NAME("jgroups.cluster.name"),
    JGROUPS_CONF_XML("jgroups.conf.xml"),
    RESOURCES_ENABLED("resources.enabled"),
    OPERATIONS_GROUP_CHILDREN_VIEW_OPEN("operations.group.children.view.open"),
    OPERATIONS_JVM_MGR_BTN_ENABLED("operations.jvm.mgr.btn.enabled"),
    OPERATIONS_JVM_DIAGNOSE_BTN_ENABLED("operations.jvm.diagnose.btn.enabled"),
    H2_TCP_SERVER_PARAMS("h2.tcp.server.params"),
    H2_WEB_SERVER_PARAMS("h2.web.server.params"),
    DECRYPT_EXPRESSION("decryptExpression"),
    ENCRYPT_EXPRESSION("encryptExpression"),
    JWALA_AGENT_DIR("jwala.agent.dir"),
    SSH_VERBOSE("ssh.verbose"),
    JWALA_TOMCAT_SERVICE_EXE("jwala.tomcat.service.exe"),
    REMOTE_TOMCAT_SERVICE_EXE("remote.tomcat.service.exe"),
    REMOTE_PATHS_TOMCAT_ROOT_DIR("remote.paths.tomcat.root.dir"),
    PATHS_GENERATED_RESOURCE_DIRECTORY("paths.generated.resource.dir"),
    JWALA_AUTHORIZATION("jwala.authorization"),
    JWALA_ADMIN("jwala.role.admin"),
    APACHE_OPENJPA_SQL_DBDICTIONARY("org.apache.openjpa.jdbc.sql.DBDictionary"),
    LOG4J_ROOT_LOGGER("log4j.rootLogger"),
    LOG4J_APPENDER_STDOUT("log4j.appender.stdout"),
    LOG4J_APPENDER_STDOUT_TARGET("log4j.appender.stdout.Target"),
    LOG4J_APPENDER_STDOUT_LAYOUT("log4j.appender.stdout.layout"),
    LOG4J_APPENDER_STDOUT_LAYOUT_CONVERSIONPATTERN("log4j.appender.stdout.layout.ConversionPattern"),
    STRING_PROPERTY("string.property"),
    INTEGER_PROPERTY("integer.property"),
    EXTERNAL_PROPERTY_ONE("external.property.one"),
    EXTERNAL_PROPERTY_TWO("external.property.two"),
    EXTERNAL_PROPERTY_THREE("external.property.three"),
    TEST_RELOAD("test.reload"),
    BOOLEAN_PROPERTY("boolean.property"),
    TEST_JWALA_PROPERTY("test.jwala.property"),
    TEST_PATH_BACKSLASH("test.path.backslash"),
    TEST_PATH_BACKSLASH_ESCAPED("test.path.backslash.escaped"),
    TEST_PATH_BACKSLASH_ESCAPED_ESCAPED("test.path.backslash.escaped.escaped"),
    CO_ORDINATOR_IP("coordinator.ip"),
    JWALA_BASE_URL("jwala.base.url"),
    WEBDRIVER_NAME("webdriver.name"),
    WEBDRIVER_VALUE("webdriver.value"),
    WEBDRIVER_CLASS("webdriver.class"),
    JWALA_USERNAME("jwala.user.name"),
    JWALA_PASSWORD("jwala.user.password"),
    JWALA_RESOURCES_UPLOAD_DIR("jwala.resources.upload.dir"),
    JWALA_PATH_SEPARATOR("jwala.path.separator"),
    JWALA_WAIT_BETWEEN_STEPS("jwala.wait.between.steps"),
    PACKAGE_ACCESS("package.access"),
    PACKAGE_DEFINITION("package.definition"),
    SHARED_LOADER("shared.loader"),
    SERVER_LOADER("server.loader"),
    COMMON_LOADER("common.loader"),
    TOMCAT_DEFAULT_JAR_SCANNER_JARS_TO_SKIP("tomcat.util.scan.DefaultJarScanner.jarsToSkip"),
    CATALINA_STARTUP_CONTEXT_CONFIG_JARS_TO_SKIP("org.apache.catalina.startup.ContextConfig.jarsToSkip"),
    CATALINA_STARTUP_TLD_CONFIG_JARS_TO_SKIP("org.apache.catalina.startup.TldConfig.jarsToSkip"),
    TOMCAT_STRING_CACHE_BYTE_ENABLED("tomcat.util.buf.StringCache.byte.enabled"),
    BUILD_VERSION("buildVersion"),
    TEST_STRING_PROPERTY("test.string.property"),
    TEST_INTEGER_PROPERTY("test.integer.property"),
    TEST_BOOLEAN_PROPERTY("test.boolean.property"),
    HANDLERS("handlers"),
    ROOT_HANDLERS(".handlers"),
    CATALINA_LOGGING_FILE_HANDLER_LEVEL("1catalina.java.util.logging.FileHandler.level"),
    CATALINA_LOGGING_FILE_HANDLER_DIRECTORY("1catalina.java.util.logging.FileHandler.directory"),
    CATALINA_LOGGING_FILE_HANDLER_PREFIX("1catalina.java.util.logging.FileHandler.prefix"),
    CATALINA_LOGGING_FILE_HANDLER_PATTERN("1catalina.java.util.logging.FileHandler.pattern"),
    CATALINA_LOGGING_FILE_HANDLER_LIMIT("1catalina.java.util.logging.FileHandler.limit"),
    CATALINA_LOGGING_FILE_HANDLER_COUNT ("1catalina.java.util.logging.FileHandler.count"),
    CATALINA_LOGGING_FILE_HANDLER_FORMATTER("1catalina.java.util.logging.FileHandler.formatter"),
    LOCAL_HOST_LOGGING_FILE_HANDLER_LEVEL("2localhost.java.util.logging.FileHandler.level"),
    LOCAL_HOST_LOGGING_FILE_HANDLER_DIRECTORY("2localhost.java.util.logging.FileHandler.directory"),
    LOCAL_HOST_LOGGING_FILE_HANDLER_PREFIX("2localhost.java.util.logging.FileHandler.prefix"),
    LOCAL_HOST_LOGGING_FILE_HANDLER_PATTERN("2localhost.java.util.logging.FileHandler.pattern"),
    LOCAL_HOST_LOGGING_FILE_HANDLER_LIMIT("2localhost.java.util.logging.FileHandler.limit"),
    LOCAL_HOST_LOGGING_FILE_HANDLER_COUNT("2localhost.java.util.logging.FileHandler.count"),
    MANAGER_LOGGING_FILE_HANDLER_LEVEL("3manager.java.util.logging.FileHandler.level"),
    MANAGER_LOGGING_FILE_HANDLER_DIRECTORY("3manager.java.util.logging.FileHandler.directory"),
    MANAGER_LOGGING_FILE_HANDLER_PREFIX("3manager.java.util.logging.FileHandler.prefix"),
    MANAGER_LOGGING_FILE_HANDLER_PATTERN("3manager.java.util.logging.FileHandler.pattern"),
    MANAGER_LOGGING_FILE_HANDLER_LIMIT("3manager.java.util.logging.FileHandler.limit"),
    MANAGER_LOGGING_FILE_HANDLER_COUNT("3manager.java.util.logging.FileHandler.count"),
    CATALINA_LOCAL_HOST_LEVEL("org.apache.catalina.core.ContainerBase.[Catalina].[localhost].level"),
    CATALINA_LOCAL_HOST_HANDLERS("org.apache.catalina.core.ContainerBase.[Catalina].[localhost].handlers"),
    CATALINA_LOCAL_HOST_MANAGER_LEVEL("org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/manager].level"),
    CATALINA_LOCAL_HOST_MANAGER_HANDLERS("org.apache.catalina.core.ContainerBase.[Catalina].[localhost].[/manager].handlers"),
    CATALINA_LEVEL("org.apache.catalina.level"),
    LOGGING_SIMPLE_FORMATTER_FORMAT("java.util.logging.SimpleFormatter.format"),
    CATALINA_AUTHENTICATOR_LEVEL("org.apache.catalina.authenticator.level"),
    CATALINA_CLUSTER_LEVEL("org.apache.catalina.cluster.level"),
    CATALINA_HA_LEVEL("org.apache.catalina.ha.level"),
    CATALINA_TRIBES_LEVEL("org.apache.catalina.tribes.level"),
    WEB_CHROME_DRIVER("webdriver.chrome.driver"),
    BASE_URL("base.url"),
    JWALA_USER_PWD("jwala.user.pwd"),
    ELEMENT_SEARCH_RENDER_WAIT_TIME("element.search.render.wait.time"),
    JDBC_DRIVER_CLASSNAME("jdbc.driverClassName"),
    JDBC_URL("jdbc.url"),
    JDBC_USERNAME("jdbc.username"),
    JDBC_PASSWORD("jdbc.password"),
    APACHE_ENTERPRISE_MANAGER_TITLE("apacheEnterpriseManager.title"),
    APACHE_ENTERPRISE_MANAGER_GREETING("apacheEnterpriseManager.greeting");




    private String propertyName;

    PropertyKeys(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
