@OASModelType(name = "AbstractTag", incomplete = true, constructible = org.eclipse.microprofile.openapi.models.tags.Tag.class, properties = {
        @OASModelProperty(name = "name", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "externalDocs", type = ExternalDocumentation.class)
})
package io.smallrye.openapi.internal.models.tags;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
