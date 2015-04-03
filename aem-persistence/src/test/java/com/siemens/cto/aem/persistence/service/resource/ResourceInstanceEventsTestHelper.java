package com.siemens.cto.aem.persistence.service.resource;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.resource.command.ResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

import java.util.Map;

/**
 * Created by z003e5zv on 3/20/2015.
 */
public class ResourceInstanceEventsTestHelper {

    public static Event<ResourceInstanceCommand> createEventWithResourceInstanceCommand(final String resourceTypeName, String name, final String groupName, final Map<String, String> attributes, final String userName) {
       return new Event<>(new ResourceInstanceCommand(resourceTypeName, name, groupName, attributes), AuditEvent.now(new User(userName)));
    }

}
