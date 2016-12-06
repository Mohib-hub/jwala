package com.cerner.jwala.persistence.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;

import java.util.Collections;

public class CommonJvmPersistenceServiceBehavior {

    private final JvmPersistenceService jvmPersistenceService;

    public CommonJvmPersistenceServiceBehavior(final JvmPersistenceService theJvmPersistenceService) {
        jvmPersistenceService = theJvmPersistenceService;
    }

    public Jvm createJvm(final String aJvmName,
                         final String aHostName,
                         final Integer aHttpPort,
                         final Integer aHttpsPort,
                         final Integer aRedirectPort,
                         final Integer aShutdownPort,
                         final Integer aAjpPort,
                         final String aUserId,
                         final Path aStatusPath,
                         final String aSystemProperties,
                         final String aUserName,
                         final String anEncryptedPassword,
                         final String jdkVersion,
                         final String tomcatVersion) {

        final CreateJvmRequest createJvmRequest = createCreateJvmRequest(aJvmName,
                aHostName,
                aHttpPort,
                aHttpsPort,
                aRedirectPort,
                aShutdownPort,
                aAjpPort,
                aUserId,
                aStatusPath,
                aSystemProperties,
                aUserName,
                anEncryptedPassword,
                jdkVersion,
                tomcatVersion);

        return jvmPersistenceService.createJvm(createJvmRequest);
    }

    public Jvm updateJvm(final Identifier<Jvm> aJvmId,
                         final String aNewJvmName,
                         final String aNewHostName,
                         final Integer aNewHttpPort,
                         final Integer aNewHttpsPort,
                         final Integer aNewRedirectPort,
                         final Integer aNewShutdownPort,
                         final Integer aNewAjpPort,
                         final String aUserId,
                         final Path aStatusPath,
                         final String aSystemProperties,
                         final String aUserName,
                         final String anEncryptedPassword,
                         final String aJdkVersion,
                         final String aTomcatVersion) {

        final UpdateJvmRequest updateJvmRequest = createUpdateJvmRequest(aJvmId,
                aNewJvmName,
                aNewHostName,
                aNewHttpPort,
                aNewHttpsPort,
                aNewRedirectPort,
                aNewShutdownPort,
                aNewAjpPort,
                aUserId,
                aStatusPath,
                aSystemProperties,
                aUserName,
                anEncryptedPassword,
                aJdkVersion,
                aTomcatVersion);

        return jvmPersistenceService.updateJvm(updateJvmRequest);
    }

    protected CreateJvmRequest createCreateJvmRequest(final String aJvmName,
                                                      final String aJvmHostName,
                                                      final Integer httpPort,
                                                      final Integer httpsPort,
                                                      final Integer redirectPort,
                                                      final Integer shutdownPort,
                                                      final Integer ajpPort,
                                                      final String aUserId,
                                                      final Path aStatusPath,
                                                      final String aSystemProperties,
                                                      final String aUserName,
                                                      final String anEncryptedPassword,
                                                      final String jdkVersion,
                                                      final String tomcatVersion) {

        return new CreateJvmRequest(aJvmName,
                aJvmHostName,
                httpPort,
                httpsPort,
                redirectPort,
                shutdownPort,
                ajpPort,
                aStatusPath,
                aSystemProperties,
                aUserName,
                anEncryptedPassword,
                jdkVersion,
                tomcatVersion);
    }

    protected UpdateJvmRequest createUpdateJvmRequest(final Identifier<Jvm> aJvmId,
                                                      final String aNewJvmName,
                                                      final String aNewHostName,
                                                      final Integer aNewHttpPort,
                                                      final Integer aNewHttpsPort,
                                                      final Integer aNewRedirectPort,
                                                      final Integer aNewShutdownPort,
                                                      final Integer aNewAjpPort,
                                                      final String aUserId,
                                                      final Path aStatusPath,
                                                      final String systemProperties,  
                                                      final String aUserName,
                                                      final String anEncryptedPassword,
                                                      final String aJdkVersion,
                                                      final String aTomcatVersion) {

        return new UpdateJvmRequest(aJvmId,
                aNewJvmName,
                aNewHostName,
                Collections.<Identifier<Group>>emptySet(),
                aNewHttpPort,
                aNewHttpsPort,
                aNewRedirectPort,
                aNewShutdownPort,
                aNewAjpPort,
                aStatusPath,
                systemProperties, 
                aUserName,
                anEncryptedPassword,
                aJdkVersion,
                aTomcatVersion);
    }
}