package io.smallrye.openapi.runtime.io;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collector;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

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

public abstract class ModelIO<T, V, A extends V, O extends V, AB, OB> {

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

    public abstract T read(AnnotationInstance annotation);

    public T readValue(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO()::isObject)
                .map(jsonIO()::asObject)
                .map(this::readObject)
                .orElse(null);
    }

    public abstract T readObject(O node);

    public abstract Optional<? extends V> write(T model);

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
