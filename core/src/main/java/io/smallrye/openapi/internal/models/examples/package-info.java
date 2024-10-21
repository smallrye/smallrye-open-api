@OASModelType(name = "Example", constructible = org.eclipse.microprofile.openapi.models.examples.Example.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "summary", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "value", type = Object.class),
        @OASModelProperty(name = "externalValue", type = String.class)
})
package io.smallrye.openapi.internal.models.examples;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
