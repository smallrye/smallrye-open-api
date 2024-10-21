package io.smallrye.openapi.runtime.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.Reference;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.tags.Tag;

import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.model.ReferenceType;
import io.smallrye.openapi.runtime.io.media.ContentIO;

/**
 * Class with some convenience methods useful for working with the OAI data model.
 *
 * @author eric.wittmann@gmail.com
 */
public class ModelUtil {

    /**
     * Constructor.
     */
    private ModelUtil() {
    }

    /**
     * Adds a {@link Tag} to the {@link OpenAPI} model. If a tag having the same
     * name already exists in the model, the tags' attributes are merged, with the
     * new tag's attributes overriding the value of any attributes specified on
     * both.
     *
     * @param openApi the OpenAPI model
     * @param tag a new {@link Tag} to add
     */
    public static void addTag(OpenAPI openApi, Tag tag) {
        List<Tag> tags = openApi.getTags();

        if (tags == null || tags.isEmpty()) {
            openApi.addTag(tag);
            return;
        }

        Tag current = tags.stream().filter(t -> t.getName().equals(tag.getName())).findFirst().orElse(null);
        int currentIndex = tags.indexOf(current);

        if (current != null) {
            Tag replacement = MergeUtil.mergeObjects(current, tag);
            tags = new ArrayList<>(tags);
            tags.set(currentIndex, replacement);
            openApi.setTags(tags);
        } else {
            openApi.addTag(tag);
        }
    }

    /**
     * Gets the {@link Components} from the OAI model. If it doesn't exist, creates it.
     *
     * @param openApi OpenAPI
     * @return Components
     */
    public static Components components(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(OASFactory.createComponents());
        }
        return openApi.getComponents();
    }

    public static <T extends Reference<T>> T dereference(OpenAPI openApi, T model) {
        if (model.getRef() == null) {
            return model;
        }
        T component = getComponent(openApi, model.getRef());
        return component != null ? component : model;
    }

    /**
     * Gets the component type specified by the given `ref` from the OpenAPI model.
     *
     * @param <T> the type of the component map's entry values
     * @param openApi containing OpenAPI model
     * @param ref reference path to retrieve
     * @return the component referenced by ref if present, otherwise null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getComponent(OpenAPI openApi, String ref) {
        final Components components = openApi.getComponents();
        Map<String, T> types = null;
        T value = null;

        if (components != null && ref.startsWith("#")) {
            String[] split = ref.split("/");

            if (split.length > 1) {
                String name = split[split.length - 1];
                ReferenceType type = ReferenceType.fromComponentPath(split[split.length - 2]);

                if (type != null) {
                    switch (type) {
                        case CALLBACK:
                            types = (Map<String, T>) components.getCallbacks();
                            break;
                        case EXAMPLE:
                            types = (Map<String, T>) components.getExamples();
                            break;
                        case HEADER:
                            types = (Map<String, T>) components.getHeaders();
                            break;
                        case LINK:
                            types = (Map<String, T>) components.getLinks();
                            break;
                        case PARAMETER:
                            types = (Map<String, T>) components.getParameters();
                            break;
                        case REQUEST_BODY:
                            types = (Map<String, T>) components.getRequestBodies();
                            break;
                        case RESPONSE:
                            types = (Map<String, T>) components.getResponses();
                            break;
                        case SCHEMA:
                            types = (Map<String, T>) components.getSchemas();
                            break;
                        case SECURITY_SCHEME:
                            types = (Map<String, T>) components.getSecuritySchemes();
                            break;
                        default:
                            break;
                    }
                }

                value = types != null ? types.get(name) : null;
            }
        }

        return value;
    }

    /**
     * Gets the {@link Paths} from the OAI model. If it doesn't exist, creates it.
     *
     * @param openApi OpenAPI
     * @return Paths
     */
    public static Paths paths(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            openApi.setPaths(OASFactory.createPaths());
        }
        return openApi.getPaths();
    }

    /**
     * Gets the {@link APIResponses} child model from the given operation. If it's null
     * then it will be created and returned.
     *
     * @param operation Operation
     * @return APIResponses
     */
    public static APIResponses responses(Operation operation) {
        if (operation.getResponses() == null) {
            operation.setResponses(OASFactory.createAPIResponses());
        }
        return operation.getResponses();
    }

    /**
     * Gets the {@link Schema} associated with the named property of the given schema.
     * When the property is not found or when no properties exist on the schema, returns
     * null.
     *
     * @param schema the schema from which to retrieve the property
     * @param name name of the property
     * @return schema for the named property if set, otherwise null
     */
    public static Schema getPropertySchema(Schema schema, String name) {
        if (schema.getProperties() != null) {
            return schema.getProperties().get(name);
        }

        return null;
    }

    /**
     * Returns true only if the given {@link Parameter} has a schema defined
     * for it. A schema can be defined either via the parameter's "schema"
     * property, or any "content.*.schema" property.
     *
     * @param parameter Parameter
     * @return Whether the parameter has a schema
     */
    public static boolean parameterHasSchema(Parameter parameter) {
        if (parameter.getSchema() != null) {
            return true;
        }
        Map<String, MediaType> mediaTypes = getMediaTypesOrEmpty(parameter.getContent());
        if (!mediaTypes.isEmpty()) {
            for (MediaType mediaType : mediaTypes.values()) {
                if (mediaType.getSchema() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the list of {@link Schema}s defined for the given {@link Parameter}.
     * A schema can be defined either via the parameter's "schema" property, or any
     * "content.*.schema" property.
     *
     * @param parameter Parameter
     * @return list of schemas, never null
     */
    public static List<Schema> getParameterSchemas(Parameter parameter) {
        if (parameter.getSchema() != null) {
            return Arrays.asList(parameter.getSchema());
        }
        Map<String, MediaType> mediaTypes = getMediaTypesOrEmpty(parameter.getContent());
        if (!mediaTypes.isEmpty()) {
            List<Schema> schemas = new ArrayList<>(mediaTypes.size());

            for (MediaType mediaType : mediaTypes.values()) {
                if (mediaType.getSchema() != null) {
                    schemas.add(mediaType.getSchema());
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Sets the given {@link Schema} on the given {@link Parameter}. This is tricky
     * because the paramater may EITHER have a schema property or it may have a
     * {@link Content} child which itself has zero or more {@link MediaType} children
     * which will contain the {@link Schema}.
     *
     * The OpenAPI specification requires that a parameter have *either* a schema
     * or a content, but not both.
     *
     * @param parameter Parameter
     * @param schema Schema
     */
    public static void setParameterSchema(Parameter parameter, Schema schema) {
        if (schema == null) {
            return;
        }
        if (parameter.getContent() == null) {
            parameter.schema(schema);
            return;
        }
        Content content = parameter.getContent();
        Map<String, MediaType> mediaTypes = getMediaTypesOrEmpty(content);
        if (mediaTypes.isEmpty()) {
            for (String mediaTypeName : ContentIO.defaultMediaTypes()) {
                MediaType mediaType = OASFactory.createMediaType();
                mediaType.setSchema(schema);
                content.addMediaType(mediaTypeName, mediaType);
            }
            return;
        }
        for (String mediaTypeName : mediaTypes.keySet()) {
            MediaType mediaType = content.getMediaType(mediaTypeName);
            mediaType.setSchema(schema);
        }
    }

    /**
     * Returns true only if the given {@link RequestBody} has a schema defined
     * for it. A schema would be found within the request body's Content/MediaType
     * children.
     *
     * @param requestBody RequestBody
     * @return Whether RequestBody has a schema
     */
    public static boolean requestBodyHasSchema(RequestBody requestBody) {
        Map<String, MediaType> mediaTypes = getMediaTypesOrEmpty(requestBody.getContent());
        if (!mediaTypes.isEmpty()) {
            for (MediaType mediaType : mediaTypes.values()) {
                if (mediaType.getSchema() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets the given {@link Schema} on the given {@link RequestBody}.
     *
     * @param requestBody RequestBody
     * @param schema Schema
     * @param mediaTypes String array
     */
    public static void setRequestBodySchema(RequestBody requestBody, Schema schema, String[] mediaTypes) {
        Content content = requestBody.getContent();
        if (content == null) {
            content = OASFactory.createContent();
            requestBody.setContent(content);
        }
        Map<String, MediaType> contentMediaTypes = getMediaTypesOrEmpty(content);
        if (contentMediaTypes.isEmpty()) {
            String[] requestBodyTypes;
            if (mediaTypes != null && mediaTypes.length > 0) {
                requestBodyTypes = mediaTypes;
            } else {
                requestBodyTypes = ContentIO.defaultMediaTypes();
            }
            for (String mediaTypeName : requestBodyTypes) {
                MediaType mediaType = OASFactory.createMediaType();
                mediaType.setSchema(schema);
                content.addMediaType(mediaTypeName, mediaType);
            }
            return;
        }
        for (String mediaTypeName : contentMediaTypes.keySet()) {
            MediaType mediaType = content.getMediaType(mediaTypeName);
            mediaType.setSchema(schema);
        }
    }

    static Map<String, MediaType> getMediaTypesOrEmpty(Content content) {
        if (content != null && content.getMediaTypes() != null) {
            return content.getMediaTypes();
        }
        return Collections.emptyMap();
    }

    /**
     * Returns the name component of the ref.
     *
     * @param ref String
     * @return Name
     */
    public static String nameFromRef(String ref) {
        String[] split = ref.split("/");
        return split[split.length - 1];
    }

    public static <V> Map<String, V> unmodifiableMap(Map<String, V> map) {
        return map != null ? Collections.unmodifiableMap(map) : null;
    }

    public static <V> Map<String, V> replace(Map<String, V> modified, UnaryOperator<Map<String, V>> factory) {
        final Map<String, V> replacement;

        if (modified == null) {
            replacement = null;
        } else {
            replacement = factory.apply(modified);
        }

        return replacement;
    }

    public static <V> Map<String, V> add(String key, V value, Map<String, V> map, Supplier<Map<String, V>> factory) {
        if (value != null) {
            if (map == null) {
                map = factory.get();
            }
            map.put(key, value);
        }
        return map;
    }

    public static <V> void remove(Map<String, V> map, String key) {
        if (map != null) {
            map.remove(key);
        }
    }

    public static <V> List<V> unmodifiableList(List<V> list) {
        return list != null ? Collections.unmodifiableList(list) : null;
    }

    public static <V> List<V> replace(List<V> modified, UnaryOperator<List<V>> factory) {
        final List<V> replacement;

        if (modified == null) {
            replacement = null;
        } else {
            replacement = factory.apply(modified);
        }

        return replacement;
    }

    public static <V> List<V> add(V value, List<V> list) {
        return add(value, list, ArrayList<V>::new);
    }

    public static <V> List<V> add(V value, List<V> list, Supplier<List<V>> factory) {
        if (value != null) {
            if (list == null) {
                list = factory.get();
            }
            list.add(value);
        }
        return list;
    }

    public static <V> void remove(List<V> list, V value) {
        if (list != null) {
            list.remove(value);
        }
    }

    public static <T> T supply(Supplier<T> source) {
        return source.get();
    }
}
