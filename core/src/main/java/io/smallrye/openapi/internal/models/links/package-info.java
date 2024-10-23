@OASModelType(name = "Link", constructible = org.eclipse.microprofile.openapi.models.links.Link.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "operationRef", type = String.class),
        @OASModelProperty(name = "operationId", type = String.class),
        @OASModelProperty(name = "parameters", type = Map.class, valueType = Object.class),
        @OASModelProperty(name = "requestBody", type = Object.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "server", type = org.eclipse.microprofile.openapi.models.servers.Server.class),
})
package io.smallrye.openapi.internal.models.links;

import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
