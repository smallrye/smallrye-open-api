@OASModelType(name = "APIResponse", constructible = org.eclipse.microprofile.openapi.models.responses.APIResponse.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "headers", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.headers.Header.class),
        @OASModelProperty(name = "content", type = org.eclipse.microprofile.openapi.models.media.Content.class),
        @OASModelProperty(name = "links", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.links.Link.class),
})
@OASModelType(name = "APIResponses", constructible = org.eclipse.microprofile.openapi.models.responses.APIResponses.class, properties = {
        @OASModelProperty(name = "APIResponses", unwrapped = true, type = Map.class, valueType = org.eclipse.microprofile.openapi.models.responses.APIResponse.class),
        @OASModelProperty(name = "default", methodNameOverride = "DefaultValue", type = org.eclipse.microprofile.openapi.models.responses.APIResponse.class),
})
package io.smallrye.openapi.internal.models.responses;

import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
