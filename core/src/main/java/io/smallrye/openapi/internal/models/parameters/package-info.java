@OASModelType(name = "Parameter", constructible = org.eclipse.microprofile.openapi.models.parameters.Parameter.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "name", type = String.class),
        @OASModelProperty(name = "in", type = org.eclipse.microprofile.openapi.models.parameters.Parameter.In.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "required", type = Boolean.class),
        @OASModelProperty(name = "schema", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "allowEmptyValue", type = Boolean.class),
        @OASModelProperty(name = "deprecated", type = Boolean.class),
        @OASModelProperty(name = "style", type = org.eclipse.microprofile.openapi.models.parameters.Parameter.Style.class),
        @OASModelProperty(name = "explode", type = Boolean.class),
        @OASModelProperty(name = "allowReserved", type = Boolean.class),
        @OASModelProperty(name = "example", type = Object.class),
        @OASModelProperty(name = "examples", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.examples.Example.class),
        @OASModelProperty(name = "content", type = org.eclipse.microprofile.openapi.models.media.Content.class),
})
@OASModelType(name = "AbstractRequestBody", incomplete = true, constructible = org.eclipse.microprofile.openapi.models.parameters.RequestBody.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "content", type = org.eclipse.microprofile.openapi.models.media.Content.class),
        @OASModelProperty(name = "required", type = Boolean.class),
})
package io.smallrye.openapi.internal.models.parameters;

import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
