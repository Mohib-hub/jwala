package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.rule.HostNameRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;
import com.siemens.cto.aem.domain.model.rule.ShutdownPortNumberRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;
import com.siemens.cto.aem.domain.model.rule.StatusPathRule;

public class CreateJvmCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final String jvmName;
    private final String hostName;

    // JVM ports
    private final Integer httpPort;
    private final Integer httpsPort;
    private final Integer redirectPort;
    private final Integer shutdownPort;
    private final Integer ajpPort;

    private final Path statusPath;

    public CreateJvmCommand(final String theName,
                            final String theHostName,
                            final Integer theHttpPort,
                            final Integer theHttpsPort,
                            final Integer theRedirectPort,
                            final Integer theShutdownPort,
                            final Integer theAjpPort,
                            final Path theStatusPath) {
        jvmName = theName;
        hostName = theHostName;
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
        statusPath = theStatusPath;
    }

    public String getJvmName() {
        return jvmName;
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public Integer getRedirectPort() {
        return redirectPort;
    }

    public Integer getShutdownPort() {
        return shutdownPort;
    }

    public Integer getAjpPort() {
        return ajpPort;
    }

    public Path getStatusPath() {
        return statusPath;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new JvmNameRule(jvmName),
                          new HostNameRule(hostName),
                          new StatusPathRule(statusPath),
                          new PortNumberRule(httpPort, AemFaultType.INVALID_JVM_HTTP_PORT),
                          new PortNumberRule(httpsPort, AemFaultType.INVALID_JVM_HTTPS_PORT, true),
                          new PortNumberRule(redirectPort, AemFaultType.INVALID_JVM_REDIRECT_PORT),
                          new ShutdownPortNumberRule(shutdownPort, AemFaultType.INVALID_JVM_SHUTDOWN_PORT),
                          new PortNumberRule(ajpPort, AemFaultType.INVALID_JVM_AJP_PORT)).validate();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CreateJvmCommand rhs = (CreateJvmCommand) obj;
        return new EqualsBuilder()
                .append(this.jvmName, rhs.jvmName)
                .append(this.hostName, rhs.hostName)
                .append(this.statusPath, rhs.statusPath)
                .append(this.httpPort, rhs.httpPort)
                .append(this.httpsPort, rhs.httpsPort)
                .append(this.redirectPort, rhs.redirectPort)
                .append(this.shutdownPort, rhs.shutdownPort)
                .append(this.ajpPort, rhs.ajpPort)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(jvmName)
                .append(hostName)
                .append(statusPath)
                .append(httpPort)
                .append(httpsPort)
                .append(redirectPort)
                .append(shutdownPort)
                .append(ajpPort)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jvmName", jvmName)
                .append("hostName", hostName)
                .append("statusPath", statusPath)
                .append("httpPort", httpPort)
                .append("httpsPort", httpsPort)
                .append("redirectPort", redirectPort)
                .append("shutdownPort", shutdownPort)
                .append("ajpPort", ajpPort)
                .toString();
    }
}
