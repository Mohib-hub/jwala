package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.ControlGroupWebServerRequest;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupWebServerControlService;
import com.cerner.jwala.service.webserver.WebServerControlService;

import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupWebServerControlServiceImpl implements GroupWebServerControlService {

    private final GroupService groupService;
    private final WebServerControlService webServerControlService;
    private final ExecutorService executorService;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupWebServerControlServiceImpl.class);

    public GroupWebServerControlServiceImpl(final GroupService theGroupService, WebServerControlService theWebServerControlService) {
        groupService = theGroupService;
        webServerControlService = theWebServerControlService;
        executorService = Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("thread-task-executor.group-control.pool.size", "25")));
    }

    @Transactional
    @Override
    public void controlGroup(final ControlGroupWebServerRequest controlGroupWebServerRequest, final User aUser) {

        controlGroupWebServerRequest.validate();

        Group group = groupService.getGroupWithWebServers(controlGroupWebServerRequest.getGroupId());

        final Set<WebServer> webServers = group.getWebServers();
        if (webServers != null) {
            controlWebServers(controlGroupWebServerRequest, aUser, webServers);
        }
    }

    @Override
    public void controlAllWebSevers(final ControlGroupWebServerRequest controlGroupWebServerRequest, final User user) {
        Set<WebServer> webServers = new HashSet<>();
        for (Group group : groupService.getGroups()) {
            final Set<WebServer> groupWebServers = groupService.getGroupWithWebServers(group.getId()).getWebServers();
            if (groupWebServers != null && !groupWebServers.isEmpty()) {
                webServers.addAll(groupWebServers);
            }
        }
        controlWebServers(controlGroupWebServerRequest, user, webServers);
    }

    private void controlWebServers(final ControlGroupWebServerRequest controlGroupWebServerRequest, final User user, Set<WebServer> webServers) {
        for (final WebServer webServer : webServers) {
            if ("START".equalsIgnoreCase(controlGroupWebServerRequest.getControlOperation().name()) && "STARTED".equalsIgnoreCase(webServer.getState().toStateLabel())) {
                LOGGER.info("webServer {} already in state {}.", webServer.getName(), webServer.getState().toStateLabel());
            } else if ("STOP".equalsIgnoreCase(controlGroupWebServerRequest.getControlOperation().name()) && "STOPPED".equalsIgnoreCase(webServer.getState().toStateLabel())) {
                LOGGER.info("webServer {} already in state {}.", webServer.getName(), webServer.getState().toStateLabel());
            } else {
                executorService.submit(new Callable<CommandOutput>() {
                    @Override
                    public CommandOutput call() throws Exception {
                        final ControlWebServerRequest controlWebServerRequest = new ControlWebServerRequest(webServer.getId(), controlGroupWebServerRequest.getControlOperation());
                        return webServerControlService.controlWebServer(controlWebServerRequest, user);
                    }
                });
            }
        }
    }

}
