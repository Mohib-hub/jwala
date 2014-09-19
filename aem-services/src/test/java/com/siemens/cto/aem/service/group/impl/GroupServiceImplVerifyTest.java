package com.siemens.cto.aem.service.group.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GroupServiceImplVerifyTest extends VerificationBehaviorSupport {

    private GroupServiceImpl impl;
    private GroupPersistenceService groupPersistenceService;
    private User user;
    private PaginationParameter pagination;

    @Before
    public void setUp() {

        groupPersistenceService = mock(GroupPersistenceService.class);
        impl = new GroupServiceImpl(groupPersistenceService);
        user = new User("unused");
        pagination = new PaginationParameter();
    }

    @Test
    public void testCreateGroup() {

        final CreateGroupCommand command = mock(CreateGroupCommand.class);

        impl.createGroup(command,
                         user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).createGroup(matchCommandInEvent(command));
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        impl.getGroup(id);

        verify(groupPersistenceService, times(1)).getGroup(eq(id));
    }

    @Test
    public void testGetGroups() {

        impl.getGroups(pagination);

        verify(groupPersistenceService, times(1)).getGroups(eq(pagination));
    }

    @Test
    public void testFindGroups() {

        final String fragment = "unused";

        impl.findGroups(fragment,
                        pagination);

        verify(groupPersistenceService, times(1)).findGroups(eq(fragment),
                                                             eq(pagination));
    }

    @Test(expected = BadRequestException.class)
    public void testFindGroupsWithBadName() {

        final String badFragment = "";

        impl.findGroups(badFragment,
                        pagination);
    }

    @Test
    public void testUpdateGroup() {

        final UpdateGroupCommand command = mock(UpdateGroupCommand.class);

        impl.updateGroup(command,
                         user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).updateGroup(matchCommandInEvent(command));
    }

    @Test
    public void testRemoveGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        impl.removeGroup(id);

        verify(groupPersistenceService, times(1)).removeGroup(eq(id));
    }

    @Test
    public void testAddJvmToGroup() {

        final AddJvmToGroupCommand command = mock(AddJvmToGroupCommand.class);

        impl.addJvmToGroup(command,
                           user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).addJvmToGroup(matchCommandInEvent(command));
    }

    @Test
    public void testAddJvmsToGroup() {

        final AddJvmsToGroupCommand command = mock(AddJvmsToGroupCommand.class);

        final Set<AddJvmToGroupCommand> addCommands = createMockedAddCommands(5);
        when(command.toCommands()).thenReturn(addCommands);

        impl.addJvmsToGroup(command,
                            user);

        verify(command, times(1)).validateCommand();
        for (final AddJvmToGroupCommand addCommand : addCommands) {
            verify(addCommand, times(1)).validateCommand();
            verify(groupPersistenceService, times(1)).addJvmToGroup(matchCommandInEvent(addCommand));
        }
    }

    @Test
    public void testRemoveJvmFromGroup() {

        final RemoveJvmFromGroupCommand command = mock(RemoveJvmFromGroupCommand.class);

        impl.removeJvmFromGroup(command,
                                user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).removeJvmFromGroup(matchCommandInEvent(command));
    }

    @Test
    public void testGetOtherGroupingDetailsOfJvms() {
        final Set<LiteGroup> groupSet = new HashSet<>();
        groupSet.add(new LiteGroup(new Identifier<Group>("1"), "Group1"));
        groupSet.add(new LiteGroup(new Identifier<Group>("2"), "Group2"));
        groupSet.add(new LiteGroup(new Identifier<Group>("3"), "Group3"));

        final Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(new Jvm(new Identifier<Jvm>("1"), "Jvm1", null, groupSet, null, null, null, null, null, null));

        final Group group = new Group(new Identifier<Group>("1"), "Group1" , jvmSet);

        when(groupPersistenceService.getGroup(any(Identifier.class), eq(false))).thenReturn(group);

        final List<String> otherGroupingDetailsOfJvm = impl.getOtherGroupingDetailsOfJvms(new Identifier<Group>("1"));
        assertTrue(otherGroupingDetailsOfJvm.size() == 1);
        assertTrue("Jvm1 is a member of Group2,Group3".equals(otherGroupingDetailsOfJvm.get(0)) ||
                   "Jvm1 is a member of Group3,Group2".equals(otherGroupingDetailsOfJvm.get(0)));
    }

    @Test
    public void testGetOtherGroupingDetailsOfWebServers() {
        final List<Group> groupSet = new ArrayList<>();
        groupSet.add(new Group(new Identifier<Group>("2"), "Group2"));
        groupSet.add(new Group(new Identifier<Group>("3"), "Group3"));

        final Set<WebServer> webServerSet = new HashSet<>();
        webServerSet.add(new WebServer(new Identifier<WebServer>("1"),
                                       groupSet,
                                       "WebServer1",
                                       null,
                                       null,
                                       null,
                                       null,
                                       null));

        groupSet.add(new Group(new Identifier<Group>("1"), "Group1", new HashSet<Jvm>(), webServerSet, null));

        when(groupPersistenceService.getGroup(any(Identifier.class), eq(true))).thenReturn(groupSet.get(2));

        final List<String> otherGroupingDetailsOfWebServer =
                impl.getOtherGroupingDetailsOfWebServers(new Identifier<Group>("1"));
        assertTrue(otherGroupingDetailsOfWebServer.size() == 1);
        assertTrue("WebServer1 is a member of Group2,Group3".equals(otherGroupingDetailsOfWebServer.get(0)) ||
                   "WebServer1 is a member of Group3,Group2".equals(otherGroupingDetailsOfWebServer.get(0)));
    }

}
