package io.smallrye.openapi.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

/**
 * @author eric.wittmann@gmail.com
 *
 */
public class FilterUtil {

    private final Map<Object, Object> stack = new IdentityHashMap<>();

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
        return new FilterUtil().filter(filter, model);
    }

    private OpenAPI filter(OASFilter filter, OpenAPI model) {
        filterComponents(filter, model.getComponents());

        if (model.getPaths() != null) {
            filter(filter,
                    model.getPaths().getPathItems(),
                    this::filterPathItem,
                    filter::filterPathItem,
                    model.getPaths()::removePathItem);
        }

        filter(filter, model.getServers(), null, filter::filterServer, model::removeServer);
        filter(filter, model.getTags(), null, filter::filterTag, model::removeTag);

        filter.filterOpenAPI(model);

        return model;
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterComponents(OASFilter filter, Components model) {
        if (model != null) {
            filter(filter, model.getCallbacks(), this::filterCallback, filter::filterCallback, model::removeCallback);
            filter(filter, model.getHeaders(), this::filterHeader, filter::filterHeader, model::removeHeader);
            filter(filter, model.getLinks(), this::filterLink, filter::filterLink, model::removeLink);
            filter(filter, model.getParameters(), this::filterParameter, filter::filterParameter, model::removeParameter);
            filter(filter, model.getRequestBodies(), this::filterRequestBody, filter::filterRequestBody,
                    model::removeRequestBody);
            filter(filter, model.getResponses(), this::filterAPIResponse, filter::filterAPIResponse,
                    model::removeResponse);
            filter(filter, model.getSchemas(), this::filterSchema, filter::filterSchema, model::removeSchema);
            filter(filter, model.getSecuritySchemes(), null, filter::filterSecurityScheme, model::removeSecurityScheme);
        }
    }

    boolean push(Object model) {
        boolean cyclicReference = stack.containsKey(model);

        if (cyclicReference) {
            UtilLogging.logger.cylicReferenceDetected();
        } else {
            stack.put(model, model);
        }

        return !cyclicReference;
    }

    /**
     * Filters the given models.
     *
     * @param filter OASFilter
     * @param models map of models to be filtered
     * @param contentFilter a filter method to be applied over the properties of each model
     * @param modelFilter a filter method - reference to method of OASFilter
     * @param remover
     *        reference to the containing model's method for removing models
     */
    private <K, V> void filter(OASFilter filter,
            Map<K, V> models,
            BiConsumer<OASFilter, V> contentFilter,
            UnaryOperator<V> modelFilter,
            Consumer<K> remover) {

        if (models != null) {
            // The collection must be copied since the original may be modified via the remover
            for (Map.Entry<K, V> entry : new LinkedHashSet<>(models.entrySet())) {
                V model = entry.getValue();

                if (!push(model)) {
                    continue;
                }

                if (contentFilter != null) {
                    contentFilter.accept(filter, model);
                }

                if (modelFilter.apply(model) == null) {
                    remover.accept(entry.getKey());
                }

                stack.remove(model);
            }
        }
    }

    /**
     * Filters the given models.
     *
     * @param filter OASFilter
     * @param models list of models to be filtered
     * @param contentFilter a filter method to be applied over the properties of each model
     * @param modelFilter a filter method - reference to method of OASFilter
     * @param remover
     *        reference to the containing model's method for removing models
     */
    private <T> void filter(OASFilter filter,
            List<T> models,
            BiConsumer<OASFilter, T> contentFilter,
            UnaryOperator<T> modelFilter,
            Consumer<T> remover) {

        if (models != null) {
            // The collection must be copied since the original may be modified via the remover
            for (T model : new ArrayList<>(models)) {
                if (!push(model)) {
                    continue;
                }

                if (contentFilter != null) {
                    contentFilter.accept(filter, model);
                }

                if (modelFilter.apply(model) == null) {
                    remover.accept(model);
                }

                stack.remove(model);
            }
        }
    }

    /**
     * Filters a given model
     *
     * @param filter OASFilter
     * @param models model to be filtered
     * @param contentFilter a filter method to be applied over the properties the model
     * @param modelFilter a filter method - reference to method of OASFilter
     * @param mutator
     *        reference to the containing model's method for updating the model
     */
    private <T> void filter(OASFilter filter,
            T model,
            BiConsumer<OASFilter, T> contentFilter,
            UnaryOperator<T> modelFilter,
            Consumer<T> mutator) {

        if (model != null) {
            if (!push(model)) {
                return;
            }

            if (contentFilter != null) {
                contentFilter.accept(filter, model);
            }

            mutator.accept(modelFilter.apply(model));
            stack.remove(model);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterCallback(OASFilter filter, Callback model) {
        Optional.ofNullable(model)
                .map(Callback::getPathItems)
                .map(Map::keySet)
                .map(ArrayList::new)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .forEach(key -> {
                    PathItem childModel = model.getPathItem(key);
                    filterPathItem(filter, childModel);

                    if (filter.filterPathItem(childModel) == null) {
                        model.removePathItem(key);
                    }
                });
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterPathItem(OASFilter filter, PathItem model) {
        if (model != null) {
            filter(filter, model.getParameters(), this::filterParameter, filter::filterParameter, model::removeParameter);

            filterOperation(filter, model.getDELETE(), model::setDELETE);
            filterOperation(filter, model.getGET(), model::setGET);
            filterOperation(filter, model.getHEAD(), model::setHEAD);
            filterOperation(filter, model.getOPTIONS(), model::setOPTIONS);
            filterOperation(filter, model.getPATCH(), model::setPATCH);
            filterOperation(filter, model.getPOST(), model::setPOST);
            filterOperation(filter, model.getPUT(), model::setPUT);
            filterOperation(filter, model.getTRACE(), model::setTRACE);

            filter(filter, model.getServers(), null, filter::filterServer, model::removeServer);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterOperation(OASFilter filter, Operation model, Consumer<Operation> mutator) {
        if (model != null) {
            filter(filter, model.getCallbacks(), this::filterCallback, filter::filterCallback, model::removeCallback);
            filter(filter, model.getParameters(), this::filterParameter, filter::filterParameter, model::removeParameter);
            filter(filter, model.getRequestBody(), this::filterRequestBody, filter::filterRequestBody,
                    model::setRequestBody);

            if (model.getResponses() != null) {
                APIResponses responses = model.getResponses();
                filter(filter, responses.getAPIResponses(), this::filterAPIResponse, filter::filterAPIResponse,
                        responses::removeAPIResponse);
            }

            filter(filter, model.getServers(), null, filter::filterServer, model::removeServer);

            mutator.accept(filter.filterOperation(model));
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterHeader(OASFilter filter, Header model) {
        if (model != null) {
            filterContent(filter, model.getContent());
            filter(filter, model.getSchema(), this::filterSchema, filter::filterSchema, model::setSchema);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterContent(OASFilter filter, Content model) {
        if (model != null && model.getMediaTypes() != null) {
            Collection<String> keys = new ArrayList<>(model.getMediaTypes().keySet());
            for (String key : keys) {
                MediaType childModel = model.getMediaType(key);
                filterMediaType(filter, childModel);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterMediaType(OASFilter filter, MediaType model) {
        if (model != null) {
            filterEncoding(filter, model.getEncoding());
            filter(filter, model.getSchema(), this::filterSchema, filter::filterSchema, model::setSchema);
        }
    }

    /**
     * Filters the given models.
     *
     * @param filter
     * @param models
     */
    private void filterEncoding(OASFilter filter, Map<String, Encoding> models) {
        if (models != null) {
            Collection<String> keys = new ArrayList<>(models.keySet());
            for (String key : keys) {
                Encoding model = models.get(key);
                filterEncoding(filter, model);
            }
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterEncoding(OASFilter filter, Encoding model) {
        if (model != null) {
            filter(filter, model.getHeaders(), this::filterHeader, filter::filterHeader, model::removeHeader);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterLink(OASFilter filter, Link model) {
        if (model != null && model.getServer() != null) {
            model.setServer(filter.filterServer(model.getServer()));
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterParameter(OASFilter filter, Parameter model) {
        if (model != null) {
            filterContent(filter, model.getContent());
            filter(filter, model.getSchema(), this::filterSchema, filter::filterSchema, model::setSchema);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterRequestBody(OASFilter filter, RequestBody model) {
        if (model != null) {
            filterContent(filter, model.getContent());
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterAPIResponse(OASFilter filter, APIResponse model) {
        if (model != null) {
            filterContent(filter, model.getContent());
            filter(filter, model.getHeaders(), this::filterHeader, filter::filterHeader, model::removeHeader);
            filter(filter, model.getLinks(), this::filterLink, filter::filterLink, model::removeLink);
        }
    }

    /**
     * Filters the given model.
     *
     * @param filter
     * @param model
     */
    private void filterSchema(OASFilter filter, Schema model) {
        if (model != null) {
            filter(filter, model.getAdditionalPropertiesSchema(), this::filterSchema, filter::filterSchema,
                    model::setAdditionalPropertiesSchema);
            filter(filter, model.getAllOf(), this::filterSchema, filter::filterSchema, model::removeAllOf);
            filter(filter, model.getAnyOf(), this::filterSchema, filter::filterSchema, model::removeAnyOf);
            filter(filter, model.getOneOf(), this::filterSchema, filter::filterSchema, model::removeOneOf);
            filter(filter, model.getItems(), this::filterSchema, filter::filterSchema, model::setItems);
            filter(filter, model.getNot(), this::filterSchema, filter::filterSchema, model::setNot);
            filter(filter, model.getProperties(), this::filterSchema, filter::filterSchema, model::removeProperty);
        }
    }
}
