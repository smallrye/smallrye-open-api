@OASModelType(name = "Server", constructible = org.eclipse.microprofile.openapi.models.servers.Server.class, properties = {
        @OASModelProperty(name = "url", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "variables", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.servers.ServerVariable.class),
})
@OASModelType(name = "ServerVariable", constructible = org.eclipse.microprofile.openapi.models.servers.ServerVariable.class, properties = {
        @OASModelProperty(name = "enum", methodNameOverride = "Enumeration", singularName = "Enumeration", type = List.class, valueType = String.class),
        @OASModelProperty(name = "default", methodNameOverride = "DefaultValue", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
})
package io.smallrye.openapi.internal.models.servers;

import java.util.List;
import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
