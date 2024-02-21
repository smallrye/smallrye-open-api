package io.smallrye.openapi.runtime.io.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.runtime.io.ContentDirection;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.media.ExampleObjectIO;
import io.smallrye.openapi.runtime.io.media.SchemaIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

public class ParameterIO extends MapModelIO<Parameter> implements ReferenceIO {

    private static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
    private static final String PROP_ALLOW_RESERVED = "allowReserved";
    private static final String PROP_CONTENT = "content";
    private static final String PROP_DEPRECATED = "deprecated";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_EXAMPLE = "example";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_EXPLODE = "explode";
    private static final String PROP_HIDDEN = "hidden";
    private static final String PROP_IN = "in";
    private static final String PROP_NAME = "name";
    private static final String PROP_REQUIRED = "required";
    private static final String PROP_SCHEMA = "schema";
    private static final String PROP_STYLE = "style";

    private final SchemaIO schemaIO;
    private final ContentIO contentIO;
    private final ExampleObjectIO exampleObjectIO;
    private final ExtensionIO extensionIO;

    public ParameterIO(AnnotationScannerContext context, ContentIO contentIO) {
        super(context, Names.PARAMETER, Names.create(Parameter.class));
        this.contentIO = contentIO;
        exampleObjectIO = new ExampleObjectIO(context);
        schemaIO = new SchemaIO(context);
        extensionIO = new ExtensionIO(context);
    }

    public List<Parameter> readList(AnnotationValue annotations) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(this::readList)
                .orElse(null);
    }

    public List<Parameter> readList(AnnotationInstance[] annotations) {
        return readList(Arrays.asList(annotations));
    }

    public List<Parameter> readList(Collection<AnnotationInstance> annotations) {
        return annotations.stream()
                .filter(not(this::isReference))
                .map(this::read)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Parameter read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Parameter");
        ParameterImpl parameter = new ParameterImpl();
        parameter.setName(context.annotations().value(annotation, PROP_NAME));
        parameter.setIn(context.annotations().enumValue(annotation, PROP_IN, Parameter.In.class));

        // Params can be hidden. Skip if that's the case.
        Boolean isHidden = context.annotations().value(annotation, PROP_HIDDEN);
        if (Boolean.TRUE.equals(isHidden)) {
            ParameterImpl.setHidden(parameter, true);
            return parameter;
        }

        parameter.setDescription(context.annotations().value(annotation, PROP_DESCRIPTION));
        parameter.setRequired(context.annotations().value(annotation, PROP_REQUIRED));
        parameter.setDeprecated(context.annotations().value(annotation, PROP_DEPRECATED));
        parameter.setAllowEmptyValue(context.annotations().value(annotation, PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(context.annotations().enumValue(annotation, PROP_STYLE, Parameter.Style.class));
        parameter.setExplode(readExplode(context, annotation));
        parameter.setAllowReserved(context.annotations().value(annotation, PROP_ALLOW_RESERVED));
        parameter.setSchema(schemaIO.read(annotation.value(PROP_SCHEMA)));
        parameter.setContent(contentIO.read(annotation.value(PROP_CONTENT), ContentDirection.PARAMETER));
        parameter.setExamples(exampleObjectIO.readMap(annotation.value(PROP_EXAMPLES)));
        parameter.setExample(exampleObjectIO.parseValue(value(annotation, PROP_EXAMPLE)));
        parameter.setRef(ReferenceType.PARAMETER.refValue(annotation));

        if (annotation.target() != null) {
            switch (annotation.target().kind()) {
                case FIELD:
                case METHOD_PARAMETER:
                    /*
                     * Limit to field and parameter. Extensions on methods are ambiguous and pertain
                     * instead to the operation.
                     *
                     */
                    parameter.setExtensions(extensionIO.readExtensible(annotation));
                    break;
                default:
                    break;
            }

            parameter.setParamRef(JandexUtil.createUniqueAnnotationTargetRef(annotation.target()));
        }

        return parameter;
    }

    public List<Parameter> readList(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isArray)
                .map(ArrayNode.class::cast)
                .map(ArrayNode::elements)
                .map(elements -> Spliterators.spliteratorUnknownSize(elements, Spliterator.ORDERED))
                .map(elements -> StreamSupport.stream(elements, false))
                .map(elements -> {
                    IoLogging.logger.jsonArray("Tag");
                    return elements.filter(JsonNode::isObject)
                            .map(ObjectNode.class::cast)
                            .map(this::read)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .orElse(null);
    }

    @Override
    public Parameter read(ObjectNode node) {
        IoLogging.logger.singleJsonObject("Parameter");
        Parameter parameter = new ParameterImpl();

        parameter.setName(JsonUtil.stringProperty(node, PROP_NAME));
        parameter.setIn(readParameterIn(node.get(PROP_IN)));
        parameter.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        parameter.setRequired(JsonUtil.booleanProperty(node, PROP_REQUIRED).orElse(null));
        parameter.setDeprecated(JsonUtil.booleanProperty(node, PROP_DEPRECATED).orElse(null));
        parameter.setAllowEmptyValue(JsonUtil.booleanProperty(node, PROP_ALLOW_EMPTY_VALUE).orElse(null));
        parameter.setStyle(readParameterStyle(node.get(PROP_STYLE)));
        parameter.setExplode(JsonUtil.booleanProperty(node, PROP_EXPLODE).orElse(null));
        parameter.setAllowReserved(JsonUtil.booleanProperty(node, PROP_ALLOW_RESERVED).orElse(null));
        parameter.setSchema(schemaIO.read(node.get(PROP_SCHEMA)));
        parameter.setContent(contentIO.read(node.get(PROP_CONTENT)));
        parameter.setExamples(exampleObjectIO.readMap(node.get(PROP_EXAMPLES)));
        parameter.setExample(JsonUtil.readObject(node.get(PROP_EXAMPLE)));
        parameter.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        parameter.setExtensions(extensionIO.readMap(node));

        return parameter;
    }

    public Optional<ArrayNode> write(List<Parameter> models) {
        return optionalJsonArray(models).map(array -> {
            models.stream()
                    .map(this::write)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(array::add);

            return array;
        });
    }

    /**
     * Writes a {@link Parameter} into the JSON node.
     *
     * @param node
     * @param model
     */
    public Optional<ObjectNode> write(Parameter model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
            } else {
                JsonUtil.stringProperty(node, PROP_NAME, model.getName());
                JsonUtil.enumProperty(node, PROP_IN, model.getIn());
                JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
                JsonUtil.booleanProperty(node, PROP_REQUIRED, model.getRequired());
                setIfPresent(node, PROP_SCHEMA, schemaIO.write(model.getSchema()));
                JsonUtil.booleanProperty(node, PROP_ALLOW_EMPTY_VALUE, model.getAllowEmptyValue());
                JsonUtil.booleanProperty(node, PROP_DEPRECATED, model.getDeprecated());
                JsonUtil.enumProperty(node, PROP_STYLE, model.getStyle());
                JsonUtil.booleanProperty(node, PROP_EXPLODE, model.getExplode());
                JsonUtil.booleanProperty(node, PROP_ALLOW_RESERVED, model.getAllowReserved());
                ObjectWriter.writeObject(node, PROP_EXAMPLE, model.getExample());
                setIfPresent(node, PROP_EXAMPLES, exampleObjectIO.write(model.getExamples()));
                setIfPresent(node, PROP_CONTENT, contentIO.write(model.getContent()));
                setAllIfPresent(node, extensionIO.write(model));
            }
            return node;
        });
    }

    /**
     * Converts from an Explode enum to a true/false/null.
     *
     * @param enumValue
     */
    private static Boolean readExplode(AnnotationScannerContext context, AnnotationInstance parameterAnnoatation) {
        Explode explode = context.annotations().enumValue(parameterAnnoatation, PROP_EXPLODE, Explode.class);

        if (explode == Explode.TRUE) {
            return Boolean.TRUE;
        }
        if (explode == Explode.FALSE) {
            return Boolean.FALSE;
        }
        return null; // NOSONAR
    }

    /**
     * Reads a parameter 'in' property.
     *
     * @param node json node
     * @return In enum
     */
    private static Parameter.In readParameterIn(final JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return PARAMETER_IN_LOOKUP.get(node.asText());
    }

    /**
     * Reads a parameter style.
     *
     * @param node the json node
     * @return style enum
     */
    private static Parameter.Style readParameterStyle(final JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        return PARAMETER_STYLE_LOOKUP.get(node.asText());
    }

    private static final Map<String, Parameter.In> PARAMETER_IN_LOOKUP = new LinkedHashMap<>();
    private static final Map<String, Parameter.Style> PARAMETER_STYLE_LOOKUP = new LinkedHashMap<>();

    static {
        Parameter.In[] parameterIns = Parameter.In.values();
        for (Parameter.In type : parameterIns) {
            PARAMETER_IN_LOOKUP.put(type.toString(), type);
        }
        Parameter.Style[] parameterStyleValues = Parameter.Style.values();
        for (Parameter.Style style : parameterStyleValues) {
            PARAMETER_STYLE_LOOKUP.put(style.toString(), style);
        }
    }
}
