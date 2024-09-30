package io.smallrye.openapi.runtime.io;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collector;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.Extensible;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Reference;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.internal.models.SmallRyeOASModels;
import io.smallrye.openapi.model.BaseExtensibleModel;
import io.smallrye.openapi.model.BaseModel;
import io.smallrye.openapi.model.DataType;
import io.smallrye.openapi.runtime.io.IOContext.OpenApiVersion;
import io.smallrye.openapi.runtime.io.callbacks.CallbackIO;
import io.smallrye.openapi.runtime.io.callbacks.CallbackOperationIO;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.headers.HeaderIO;
import io.smallrye.openapi.runtime.io.info.ContactIO;
import io.smallrye.openapi.runtime.io.info.InfoIO;
import io.smallrye.openapi.runtime.io.info.LicenseIO;
import io.smallrye.openapi.runtime.io.links.LinkIO;
import io.smallrye.openapi.runtime.io.links.LinkParameterIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.media.DiscriminatorIO;
import io.smallrye.openapi.runtime.io.media.EncodingIO;
import io.smallrye.openapi.runtime.io.media.ExampleObjectIO;
import io.smallrye.openapi.runtime.io.media.MediaTypeIO;
import io.smallrye.openapi.runtime.io.media.SchemaIO;
import io.smallrye.openapi.runtime.io.parameters.ParameterIO;
import io.smallrye.openapi.runtime.io.parameters.RequestBodyIO;
import io.smallrye.openapi.runtime.io.responses.APIResponseIO;
import io.smallrye.openapi.runtime.io.responses.APIResponsesIO;
import io.smallrye.openapi.runtime.io.security.OAuthFlowIO;
import io.smallrye.openapi.runtime.io.security.OAuthFlowsIO;
import io.smallrye.openapi.runtime.io.security.OAuthScopeIO;
import io.smallrye.openapi.runtime.io.security.SecurityIO;
import io.smallrye.openapi.runtime.io.security.SecurityRequirementIO;
import io.smallrye.openapi.runtime.io.security.SecurityRequirementsSetIO;
import io.smallrye.openapi.runtime.io.security.SecuritySchemeIO;
import io.smallrye.openapi.runtime.io.servers.ServerIO;
import io.smallrye.openapi.runtime.io.servers.ServerVariableIO;
import io.smallrye.openapi.runtime.io.tags.TagIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

public abstract class ModelIO<T, V, A extends V, O extends V, AB, OB> implements JsonIO.PropertyMapper<V, OB> {

    private final IOContext<V, A, O, AB, OB> context;
    protected final DotName annotationName;
    protected final DotName modelName;

    protected ModelIO(IOContext<V, A, O, AB, OB> context, DotName annotationName, DotName modelName) {
        this.context = context;
        this.annotationName = annotationName;
        this.modelName = modelName;
    }

    public IOContext<V, A, O, AB, OB> ioContext() {
        return context;
    }

    public JsonIO<V, A, O, AB, OB> jsonIO() {
        return context.jsonIO();
    }

    public AnnotationScannerContext scannerContext() {
        return context.scannerContext();
    }

    protected void setIfPresent(OB object, String key, Optional<? extends V> valueSource) {
        valueSource.ifPresent(value -> jsonIO().set(object, key, value));
    }

    protected void setAllIfPresent(OB object, Optional<? extends O> valueSource) {
        valueSource.ifPresent(value -> jsonIO().setAll(object, value));
    }

    protected Optional<OB> optionalJsonObject(Object source) {
        if (source == null) {
            return Optional.empty();
        }
        return Optional.of(jsonIO().createObject());
    }

    protected Optional<AB> optionalJsonArray(Object source) {
        if (source == null) {
            return Optional.empty();
        }
        return Optional.of(jsonIO().createArray());
    }

    protected static <T> Map.Entry<String, T> entry(String key, T value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    protected <P> P value(AnnotationInstance annotation, String propertyName) {
        return scannerContext().annotations().value(annotation, propertyName);
    }

    protected <P> P value(AnnotationInstance annotation, String propertyName, P defaultValue) {
        return scannerContext().annotations().value(annotation, propertyName, defaultValue);
    }

    protected <P extends Enum<P>> P enumValue(AnnotationInstance annotation, String propertyName, Class<P> type) {
        return scannerContext().annotations().enumValue(annotation, propertyName, type);
    }

    protected <P extends Enum<P>> P enumValue(V value, Class<P> type) {
        String strValue = jsonIO().asString(value);

        if (strValue != null) {
            try {
                return Enum.valueOf(type, strValue.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                // Ignore exceptions from `valueOf` for illegal arguments
            }
        }

        return null;
    }

    protected Optional<String> getName(AnnotationInstance annotation, String nameProperty) {
        String name = value(annotation, nameProperty);

        if (name == null && JandexUtil.isRef(annotation)) {
            name = JandexUtil.nameFromRef(annotation);
        }

        return Optional.ofNullable(name);
    }

    protected Optional<String> getName(AnnotationInstance annotation) {
        return getName(annotation, "name");
    }

    @SuppressWarnings("unchecked")
    protected static <T> Predicate<T> not(Predicate<? super T> predicate) {
        return (Predicate<T>) predicate.negate();
    }

    /**
     * Creates a Collector for a stream of Map.Entry where the entry keys are Strings.
     *
     * Null map entry values are allowed, but duplicate keys will result in an
     * IllegalStateException being thrown.
     *
     * This method is the equivalent of
     * {@link java.util.stream.Collectors#toMap(java.util.function.Function, java.util.function.Function)}
     * where the given key and value mapping functions are simply {@code Map.Entry.getKey()} and
     * {@code Map.Entry.getValue()}, and where null values are tolerated.
     *
     * @param <T> the type of the map entry values
     * @return a collector allowing null values but forbidding duplicate keys.
     */
    protected static <T> Collector<Map.Entry<String, T>, ?, Map<String, T>> toLinkedMap() {
        BiConsumer<Map<String, T>, Map.Entry<String, T>> accumulator = (map, entry) -> {
            String k = entry.getKey();
            T v = entry.getValue();

            if (map.containsKey(k)) {
                throw new IllegalStateException(String.format(
                        "Duplicate key %s (attempted merging values %s and %s)",
                        k, map.get(k), v));
            }

            map.put(k, v);
        };

        BinaryOperator<Map<String, T>> combiner = (m1, m2) -> {
            m2.entrySet().forEach(entry -> accumulator.accept(m1, entry));
            return m1;
        };

        return Collector.of(LinkedHashMap::new, accumulator, combiner);
    }

    public AnnotationInstance getAnnotation(AnnotationTarget target) {
        return scannerContext().annotations().getAnnotation(target, annotationName);
    }

    public List<AnnotationInstance> getRepeatableAnnotations(AnnotationTarget target) {
        return scannerContext().annotations()
                .getRepeatableAnnotation(target, annotationName, Names.containerOf(annotationName));
    }

    public boolean hasRepeatableAnnotation(AnnotationTarget target) {
        return scannerContext().annotations().hasAnnotation(target, annotationName, Names.containerOf(annotationName));
    }

    public T read(AnnotationTarget target) {
        return Optional.ofNullable(getAnnotation(target))
                .map(this::read)
                .orElse(null);
    }

    public T read(AnnotationValue annotation) {
        return Optional.ofNullable(annotation)
                .map(AnnotationValue::asNested)
                .map(this::read)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <C extends Constructible> T read(Class<C> type, AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation(annotation.name().toString());
        BaseModel<C> model = (BaseModel<C>) OASFactory.createObject(type);

        for (AnnotationValue annotationValue : annotation.values()) {
            Object value = scannerContext().annotations().value(annotation, annotationValue);

            if (value != null && !setProperty((T) model, annotationValue)) {
                String name = annotationValue.name();

                if ("ref".equals(name) && Reference.class.isAssignableFrom(type)) {
                    model.setRef(annotationValue.asString());
                } else if ("extensions".equals(name) && Extensible.class.isAssignableFrom(type)) {
                    ((BaseExtensibleModel<?>) model).setExtensions(extensionIO().readExtensible(annotation));
                } else {
                    model.setProperty(name, value);
                }
            }
        }

        return (T) model;
    }

    protected boolean setProperty(T model, AnnotationValue value) {
        return false;
    }

    public abstract T read(AnnotationInstance annotation);

    private SmallRyeOASModels modelTypes = new SmallRyeOASModels();

    @SuppressWarnings("unchecked")
    public <C extends Constructible> T readObject(Class<C> type, O node) {
        var jsonIO = jsonIO();
        BaseModel<C> model = (BaseModel<C>) OASFactory.createObject(type);
        var modelType = modelTypes.getModel(type);

        for (Map.Entry<String, V> property : jsonIO.properties(node)) {
            String name = property.getKey();
            V value = property.getValue();

            if (value != null && !setProperty((T) model, name, value)) {
                if (ReferenceIO.REF.equals(name) && Reference.class.isAssignableFrom(type)) {
                    model.setRef(jsonIO.asString(value));
                } else if (ExtensionIO.isExtension(name) && Extensible.class.isAssignableFrom(type)) {
                    ((BaseExtensibleModel<?>) model).addExtension(name, jsonIO.fromJson(value));
                } else {
                    model.setProperty(name, readJson(value, modelType.getPropertyType(name)));
                }
            }
        }

        return (T) model;
    }

    protected Object readJson(V node, DataType desiredType) {
        //        if (jsonIO().isObject(node) && desiredType.type == DataType.Type.MAP) {
        //            Map<String, Object> result = new HashMap<>();
        //            O object = jsonIO().asObject(node);
        //            for (Entry<String, V> entry : jsonIO().properties(object)) {
        //                result.put(entry.getKey(), readJson(entry.getValue(), desiredType.content));
        //            }
        //            return result;
        //        } else if (jsonIO().isArray(node) && desiredType.type == DataType.Type.LIST) {
        //            List<Object> result = new ArrayList<>();
        //            A array = jsonIO().asArray(node);
        //            for (V element : jsonIO().entries(array)) {
        //                result.add(readJson(element, desiredType.content));
        //            }
        //            return result;
        //        } else if (desiredType.type == DataType.Type.OBJECT) {
        //            if (desiredType.clazz == Object.class) {
        //                return jsonIO().fromJson(node);
        //            } else {
        //                return readValue(node, desiredType.clazz);
        //            }
        //        } else {
        //            return jsonIO().fromJson(node);
        //        }
        if (jsonIO().isObject(node)) {
            if (desiredType.type == DataType.Type.MAP) {
                Map<String, Object> result = new HashMap<>();
                O object = jsonIO().asObject(node);
                for (Entry<String, V> entry : jsonIO().properties(object)) {
                    result.put(entry.getKey(), readJson(entry.getValue(), desiredType.content));
                }
                return result;
            } else if (desiredType.type == DataType.Type.OBJECT) {
                if (desiredType.clazz == Object.class) {
                    return jsonIO().fromJson(node);
                } else {
                    return readValue(node, desiredType.clazz);
                }
            } else {
                // Log node dropped?
                return null;
            }
        } else if (jsonIO().isArray(node)) {
            if (desiredType.type == DataType.Type.LIST) {
                List<Object> result = new ArrayList<>();
                A array = jsonIO().asArray(node);
                for (V element : jsonIO().entries(array)) {
                    result.add(readJson(element, desiredType.content));
                }
                return result;
            } else if (desiredType.clazz == Object.class) {
                return jsonIO().fromJson(node);
            } else {
                // Log node dropped?
                return null;
            }
        } else if (desiredType.type == DataType.Type.OBJECT) {
            if (desiredType.clazz == Object.class) {
                return jsonIO().fromJson(node);
            } else {
                return readValue(node, desiredType.clazz);
            }
        } else {
            return jsonIO().fromJson(node);
        }
    }

    /**
     * Convert JSON value node to an object when we have a desired type
     * <p>
     * The JSON value will be converted to the desired type if possible or returned as its native type if not.
     *
     * @param node the JSON node
     * @param desiredType the type that we want to be returned
     * @return an object which represents the JSON node, which may or may not be of the desired type
     */
    @SuppressWarnings("unchecked")
    protected Object readValue(V node, Class<?> desiredType) {
        if (desiredType == Schema.class) {
            return schemaIO().readValue(node);
        }

        // Handles string, number and boolean types
        Object result = jsonIO().fromJson(node, desiredType);
        if (result != null) {
            return result;
        }

        if (Enum.class.isAssignableFrom(desiredType)) {
            result = enumValue(node, desiredType.asSubclass(Enum.class));
            if (result != null) {
                return result;
            }
        }

        if (Constructible.class.isAssignableFrom(desiredType)) {
            return readObject((Class<? extends Constructible>) desiredType, (O) node);
        }

        return jsonIO().fromJson(node);
    }

    protected boolean setProperty(T model, String name, V value) {
        return false;
    }

    public T readValue(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO()::isObject)
                .map(jsonIO()::asObject)
                .map(this::readObject)
                .orElse(null);
    }

    public T readObject(O node) {
        throw new UnsupportedOperationException(getClass() + "#readObject(O)");
    }

    public Optional<? extends V> write(T model) {
        throw new UnsupportedOperationException(getClass() + "#write(T)");
    }

    Set<String> REF_PROPERTIES = Set.of(ReferenceIO.REF, "summary", "description");

    @Override
    @SuppressWarnings("unchecked")
    public Optional<V> mapObject(Object object) {
        if (object instanceof Schema) {
            return (Optional<V>) schemaIO().write((Schema) object);
        }

        return Optional.empty();
    }

    @Override
    public Optional<V> mapProperty(Object object, String propertyName, Object propertyValue) {
        if (object instanceof Reference) {
            if (object instanceof PathItem) {
                // PathItems may have elements in addition to $ref
                return Optional.empty();
            }

            String ref = ((Reference<?>) object).getRef();

            if (ref != null && !ref.isBlank() && !REF_PROPERTIES.contains(propertyName)) {
                // Do not write additional properties to the output
                return Optional.of(jsonIO().nullValue());
            }
        }

        return Optional.empty();
    }

    @Override
    public void mapObject(Object object, OB nodeBuilder) {
        if (object instanceof RequestBody && ((Reference<?>) object).getRef() == null) {
            Boolean required = ((RequestBody) object).getRequired();
            setIfPresent(nodeBuilder, "required", jsonIO().toJson(required));
        }
    }

    public OpenApiVersion openApiVersion() {
        return context.openApiVersion();
    }

    public void setOpenApiVersion(OpenApiVersion version) {
        context.setOpenApiVersion(version);
    }

    public ComponentsIO<V, A, O, AB, OB> componentsIO() {
        return context.componentsIO();
    }

    public ExternalDocumentationIO<V, A, O, AB, OB> extDocIO() {
        return context.extDocIO();
    }

    public OpenAPIDefinitionIO<V, A, O, AB, OB> openApiDefinitionIO() {
        return context.openApiDefinitionIO();
    }

    public OperationIO<V, A, O, AB, OB> operationIO() {
        return context.operationIO();
    }

    public PathItemOperationIO<V, A, O, AB, OB> pathItemOperationIO() {
        return context.pathItemOperationIO();
    }

    public PathItemIO<V, A, O, AB, OB> pathItemIO() {
        return context.pathItemIO();
    }

    public PathsIO<V, A, O, AB, OB> pathsIO() {
        return context.pathsIO();
    }

    public CallbackIO<V, A, O, AB, OB> callbackIO() {
        return context.callbackIO();
    }

    public CallbackOperationIO<V, A, O, AB, OB> callbackOperationIO() {
        return context.callbackOperationIO();
    }

    public ExtensionIO<V, A, O, AB, OB> extensionIO() {
        return context.extensionIO();
    }

    public HeaderIO<V, A, O, AB, OB> headerIO() {
        return context.headerIO();
    }

    public ContactIO<V, A, O, AB, OB> contactIO() {
        return context.contactIO();
    }

    public InfoIO<V, A, O, AB, OB> infoIO() {
        return context.infoIO();
    }

    public LicenseIO<V, A, O, AB, OB> licenseIO() {
        return context.licenseIO();
    }

    public LinkIO<V, A, O, AB, OB> linkIO() {
        return context.linkIO();
    }

    public LinkParameterIO<V, A, O, AB, OB> linkParameterIO() {
        return context.linkParameterIO();
    }

    public ContentIO<V, A, O, AB, OB> contentIO() {
        return context.contentIO();
    }

    public DiscriminatorIO<V, A, O, AB, OB> discriminatorIO() {
        return context.discriminatorIO();
    }

    public EncodingIO<V, A, O, AB, OB> encodingIO() {
        return context.encodingIO();
    }

    public ExampleObjectIO<V, A, O, AB, OB> exampleObjectIO() {
        return context.exampleObjectIO();
    }

    public MediaTypeIO<V, A, O, AB, OB> mediaTypeIO() {
        return context.mediaTypeIO();
    }

    public SchemaIO<V, A, O, AB, OB> schemaIO() {
        return context.schemaIO();
    }

    public ParameterIO<V, A, O, AB, OB> parameterIO() {
        return context.parameterIO();
    }

    public RequestBodyIO<V, A, O, AB, OB> requestBodyIO() {
        return context.requestBodyIO();
    }

    public APIResponseIO<V, A, O, AB, OB> apiResponseIO() {
        return context.apiResponseIO();
    }

    public APIResponsesIO<V, A, O, AB, OB> apiResponsesIO() {
        return context.apiResponsesIO();
    }

    public OAuthFlowIO<V, A, O, AB, OB> oauthFlowIO() {
        return context.oauthFlowIO();
    }

    public OAuthFlowsIO<V, A, O, AB, OB> oauthFlowsIO() {
        return context.oauthFlowsIO();
    }

    public OAuthScopeIO<V, A, O, AB, OB> oauthScopeIO() {
        return context.oauthScopeIO();
    }

    public SecurityIO<V, A, O, AB, OB> securityIO() {
        return context.securityIO();
    }

    public SecurityRequirementIO<V, A, O, AB, OB> securityRequirementIO() {
        return context.securityRequirementIO();
    }

    public SecurityRequirementsSetIO<V, A, O, AB, OB> securityRequirementsSetIO() {
        return context.securityRequirementsSetIO();
    }

    public SecuritySchemeIO<V, A, O, AB, OB> securitySchemeIO() {
        return context.securitySchemeIO();
    }

    public ServerIO<V, A, O, AB, OB> serverIO() {
        return context.serverIO();
    }

    public ServerVariableIO<V, A, O, AB, OB> serverVariableIO() {
        return context.serverVariableIO();
    }

    public TagIO<V, A, O, AB, OB> tagIO() {
        return context.tagIO();
    }

}
