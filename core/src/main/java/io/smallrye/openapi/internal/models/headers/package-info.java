@OASModelType(name = "Header", constructible = org.eclipse.microprofile.openapi.models.headers.Header.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "required", type = Boolean.class),
        @OASModelProperty(name = "deprecated", type = Boolean.class),
        @OASModelProperty(name = "allowEmptyValue", type = Boolean.class),
        @OASModelProperty(name = "style", type = org.eclipse.microprofile.openapi.models.headers.Header.Style.class),
        @OASModelProperty(name = "explode", type = Boolean.class),
        @OASModelProperty(name = "schema", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "example", type = Object.class),
        @OASModelProperty(name = "examples", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.examples.Example.class),
        @OASModelProperty(name = "content", type = org.eclipse.microprofile.openapi.models.media.Content.class),
})
package io.smallrye.openapi.internal.models.headers;

import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
