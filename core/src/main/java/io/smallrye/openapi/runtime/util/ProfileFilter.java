package io.smallrye.openapi.runtime.util;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.PathItem.HttpMethod;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.model.Extensions;

/**
 * Not intended for use outside of smallrye-open-api. Interface and functionality
 * may not be stable for general use.
 *
 * Removes operations and path items from the model if they are not included
 * based on configuration. Note that path items will be removed if all operations
 * have been removed by the filter and if the path item is not specified in the
 * OpenAPI components section.
 */
public class ProfileFilter implements OASFilter {

    private final Map<String, PathItem> pathItemComponents;
    private final Set<String> included;
    private final Set<String> excluded;

    public ProfileFilter(OpenAPI model, OpenApiConfig config) {
        pathItemComponents = Optional.ofNullable(model.getComponents())
                .map(Components::getPathItems)
                .orElseGet(Collections::emptyMap);

        included = config.getScanProfiles();
        excluded = config.getScanExcludeProfiles();
    }

    @Override
    public PathItem filterPathItem(PathItem pathItem) {
        boolean operationExcluded = false;

        for (HttpMethod method : Set.copyOf(pathItem.getOperations().keySet())) {
            Operation o = pathItem.getOperations().get(method);

            if (!Extensions.includedProfile(o, included, excluded)) {
                operationExcluded = true;
                pathItem.setOperation(method, null);
            }
        }

        if (operationExcluded && pathItem.getOperations().isEmpty() && nonComponent(pathItem)) {
            // Only remove the path item if it is not a component that may be referenced elsewhere.
            return null;
        }

        return pathItem;
    }

    private boolean nonComponent(PathItem pathItem) {
        for (PathItem component : pathItemComponents.values()) {
            if (pathItem == component) {
                // If it's the same object, the given pathItem is in components
                return false;
            }
        }
        return true;
    }
}
