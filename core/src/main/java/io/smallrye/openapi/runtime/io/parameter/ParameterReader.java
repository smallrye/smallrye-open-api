package io.smallrye.openapi.runtime.io.parameter;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.runtime.io.ContentDirection;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.Parameterizable;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.content.ContentReader;
import io.smallrye.openapi.runtime.io.example.ExampleReader;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.io.schema.SchemaReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Parameter annotation
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#parameter-object">parameter-object</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ParameterReader {

    private ParameterReader() {
    }

    /**
     * Reads a map of Parameter annotations.
     *
     * @param context the scanning context
     * @param annotationValue Map of {@literal @}Parameter annotations
     * @return List of Parameter model
     */
    public static Optional<List<Parameter>> readParametersList(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue != null) {
            IoLogging.logger.annotationsList("@Parameter");
            List<Parameter> parameters = new ArrayList<>();
            AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
            for (AnnotationInstance nested : nestedArray) {
                Parameter parameter = readParameter(context, nested);
                if (parameter != null) {
                    parameters.add(parameter);
                }
            }
            return Optional.of(parameters);
        }
        return Optional.empty();
    }

    /**
     * Reads a {@link Parameter} OpenAPI node.
     *
     * @param node json list
     * @return List of Parameter model
     */
    public static Optional<List<Parameter>> readParameterList(final JsonNode node) {
        if (node != null && node.isArray()) {
            IoLogging.logger.jsonList("Parameter");
            List<Parameter> params = new ArrayList<>();
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode paramNode : arrayNode) {
                params.add(ParameterReader.readParameter(paramNode));
            }
            return Optional.of(params);
        }
        return Optional.empty();
    }

    /**
     * Reads a map of Parameter annotations.
     *
     * @param context the scanning context
     * @param annotationValue Map of {@literal @}Parameter annotations
     * @return Map of Parameter model
     */
    public static Map<String, Parameter> readParameters(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        IoLogging.logger.annotationsMap("@Parameter");
        Map<String, Parameter> parameters = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = context.annotations().value(nested, Parameterizable.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                Parameter parameter = readParameter(context, nested);
                if (parameter != null) {
                    parameters.put(name, parameter);
                }
            }
        }
        return parameters;
    }

    /**
     * Reads the {@link Parameter} OpenAPI nodes.
     *
     * @param node json map of Parameters
     * @return Map of Parameter model
     */
    public static Map<String, Parameter> readParameters(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.jsonMap("Parameters");
        Map<String, Parameter> parameters = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            parameters.put(fieldName, readParameter(childNode));
        }

        return parameters;
    }

    /**
     * Reads a Parameter annotation into a model.
     *
     * @param context the scanning context
     * @param annotationInstance {@literal @}Parameter model
     * @return Parameter model
     */
    public static Parameter readParameter(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance) {

        if (annotationInstance == null) {
            return null;
        }
        IoLogging.logger.singleAnnotation("@Parameter");
        ParameterImpl parameter = new ParameterImpl();
        parameter.setName(context.annotations().value(annotationInstance, Parameterizable.PROP_NAME));
        parameter.setIn(context.annotations().enumValue(annotationInstance, ParameterConstant.PROP_IN, Parameter.In.class));

        // Params can be hidden. Skip if that's the case.
        Boolean isHidden = context.annotations().value(annotationInstance, ParameterConstant.PROP_HIDDEN);
        if (Boolean.TRUE.equals(isHidden)) {
            ParameterImpl.setHidden(parameter, true);
            return parameter;
        }

        parameter.setDescription(context.annotations().value(annotationInstance, Parameterizable.PROP_DESCRIPTION));
        parameter.setRequired(context.annotations().value(annotationInstance, Parameterizable.PROP_REQUIRED));
        parameter.setDeprecated(context.annotations().value(annotationInstance, Parameterizable.PROP_DEPRECATED));
        parameter.setAllowEmptyValue(context.annotations().value(annotationInstance, Parameterizable.PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(
                context.annotations().enumValue(annotationInstance, Parameterizable.PROP_STYLE, Parameter.Style.class));
        parameter.setExplode(readExplode(context, annotationInstance));
        parameter.setAllowReserved(context.annotations().value(annotationInstance, ParameterConstant.PROP_ALLOW_RESERVED));
        parameter.setSchema(SchemaFactory.readSchema(context, annotationInstance.value(Parameterizable.PROP_SCHEMA)));
        parameter.setContent(
                ContentReader.readContent(context, annotationInstance.value(Parameterizable.PROP_CONTENT),
                        ContentDirection.PARAMETER));
        parameter.setExamples(ExampleReader.readExamples(context, annotationInstance.value(Parameterizable.PROP_EXAMPLES)));
        parameter.setExample(
                ExampleReader.parseValue(context,
                        context.annotations().value(annotationInstance, Parameterizable.PROP_EXAMPLE)));
        parameter.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.PARAMETER));

        if (annotationInstance.target() != null) {
            switch (annotationInstance.target().kind()) {
                case FIELD:
                case METHOD_PARAMETER:
                    /*
                     * Limit to field and parameter. Extensions on methods are ambiguous and pertain
                     * instead to the operation.
                     *
                     */
                    parameter.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
                    break;
                default:
                    break;
            }

            parameter.setParamRef(JandexUtil.createUniqueAnnotationTargetRef(annotationInstance.target()));
        }

        return parameter;
    }

    /**
     * Reads a {@link Parameter} OpenAPI node.
     *
     * @param node the json object
     * @return Parameter model
     */
    public static Parameter readParameter(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        IoLogging.logger.singleJsonObject("Parameter");
        Parameter parameter = new ParameterImpl();

        parameter.setName(JsonUtil.stringProperty(node, Parameterizable.PROP_NAME));
        parameter.setIn(readParameterIn(node.get(ParameterConstant.PROP_IN)));
        parameter.setDescription(JsonUtil.stringProperty(node, Parameterizable.PROP_DESCRIPTION));
        parameter.setRequired(JsonUtil.booleanProperty(node, Parameterizable.PROP_REQUIRED).orElse(null));
        parameter.setDeprecated(JsonUtil.booleanProperty(node, Parameterizable.PROP_DEPRECATED).orElse(null));
        parameter.setAllowEmptyValue(JsonUtil.booleanProperty(node, Parameterizable.PROP_ALLOW_EMPTY_VALUE).orElse(null));
        parameter.setStyle(readParameterStyle(node.get(Parameterizable.PROP_STYLE)));
        parameter.setExplode(JsonUtil.booleanProperty(node, Parameterizable.PROP_EXPLODE).orElse(null));
        parameter.setAllowReserved(JsonUtil.booleanProperty(node, ParameterConstant.PROP_ALLOW_RESERVED).orElse(null));
        parameter.setSchema(SchemaReader.readSchema(node.get(Parameterizable.PROP_SCHEMA)));
        parameter.setContent(ContentReader.readContent(node.get(Parameterizable.PROP_CONTENT)));
        parameter.setExamples(ExampleReader.readExamples(node.get(Parameterizable.PROP_EXAMPLES)));
        parameter.setExample(readObject(node.get(Parameterizable.PROP_EXAMPLE)));
        parameter.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        ExtensionReader.readExtensions(node, parameter);

        return parameter;
    }

    /**
     * Converts from an Explode enum to a true/false/null.
     *
     * @param enumValue
     */
    private static Boolean readExplode(AnnotationScannerContext context, AnnotationInstance parameterAnnoatation) {
        Explode explode = context.annotations().enumValue(parameterAnnoatation, Parameterizable.PROP_EXPLODE, Explode.class);

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
