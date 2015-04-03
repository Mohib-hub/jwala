package com.siemens.cto.aem.service.resource;

import java.util.Collection;
import java.util.List;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.domain.model.resource.command.ResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;


public interface ResourceService {

    Collection<ResourceType> getResourceTypes();

    ResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId);

    List<ResourceInstance> getResourceInstancesByGroupName(final String groupName);

    ResourceInstance getResourceInstanceByGroupNameAndName(final String groupName, final String name);

    List<ResourceInstance> getResourceInstancesByGroupNameAndResourceTypeName(final String groupName, final String resourceTypeName);

    ResourceInstance createResourceInstance(final ResourceInstanceCommand createResourceInstanceCommand, final User creatingUser);

    ResourceInstance updateResourceInstance(final String groupName, final String name, final ResourceInstanceCommand updateResourceInstanceAttributesCommand, final User updatingUser);

    void deleteResourceInstance(final String name, final String groupName);
    
    String  encryptUsingPlatformBean(String cleartext);
}
