@OASModelType(name = "Callback", constructible = org.eclipse.microprofile.openapi.models.callbacks.Callback.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "PathItems", unwrapped = true, type = Map.class, valueType = org.eclipse.microprofile.openapi.models.PathItem.class),
})
package io.smallrye.openapi.internal.models.callbacks;

import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
