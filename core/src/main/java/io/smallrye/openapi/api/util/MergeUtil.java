package io.smallrye.openapi.api.util;

import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.model.BaseModel;

/**
 * Used to merge OAI data models into a single one. The MP+OAI 1.0 spec
 * requires that any or all of the various mechanisms for producing an OAI document
 * can be used. When more than one mechanism is used, each mechanism produces an
 * OpenAPI document. These multiple documents must then be sensibly merged into
 * a final result.
 *
 * @author eric.wittmann@gmail.com
 */
public class MergeUtil {

    private MergeUtil() {
    }

    /**
     * Merges documents and returns the result.
     *
     * @param document1 OpenAPIImpl instance
     * @param document2 OpenAPIImpl instance
     * @return Merged OpenAPIImpl instance
     */
    public static final OpenAPI merge(OpenAPI document1, OpenAPI document2) {
        return mergeObjects(document1, document2);
    }

    @SuppressWarnings("unchecked")
    public static <C extends Constructible> C mergeObjects(C object1, C object2) {
        BaseModel<C> model1 = (BaseModel<C>) object1;
        BaseModel<C> model2 = (BaseModel<C>) object2;
        return (C) mergeObjects(model1, model2);
    }

    /**
     * Generic merge of two objects of the same type.
     *
     * @param object1 First object
     * @param object2 Second object
     * @param <T> Type parameter
     * @return Merged object
     */
    public static <C extends Constructible, T extends BaseModel<C>> T mergeObjects(T object1, T object2) {
        return BaseModel.merge(object1, object2);
    }
}
