package io.smallrye.openapi.api.util;

import java.util.IdentityHashMap;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.model.BaseModel;

/**
 * @author eric.wittmann@gmail.com
 * @deprecated
 */
@Deprecated(since = "4.0", forRemoval = true)
public class FilterUtil {

    private FilterUtil() {
    }

    /**
     * Apply the given filter to the given model.
     *
     * @param filter
     *        OASFilter
     * @param model
     *        OpenAPI model
     * @return Filtered OpenAPI model
     */
    public static final OpenAPI applyFilter(OASFilter filter, OpenAPI model) {
        ((BaseModel<?>) model).filter(filter, new IdentityHashMap<>());
        return model;
    }

}
