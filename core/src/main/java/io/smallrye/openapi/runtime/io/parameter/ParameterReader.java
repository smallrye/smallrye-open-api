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
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#parameter-object">parameter-object</a>
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
            String name = JandexUtil.stringValue(nested, Parameterizable.PROP_NAME);
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
        Parameter parameter = new ParameterImpl();
        parameter.setName(JandexUtil.stringValue(annotationInstance, Parameterizable.PROP_NAME));
        parameter.setIn(JandexUtil.enumValue(annotationInstance, ParameterConstant.PROP_IN,
                org.eclipse.microprofile.openapi.models.parameters.Parameter.In.class));

        // Params can be hidden. Skip if that's the case.
        Optional<Boolean> isHidden = JandexUtil.booleanValue(annotationInstance, ParameterConstant.PROP_HIDDEN);
        if (isHidden.isPresent() && isHidden.get()) {
            ParameterImpl.setHidden(parameter, true);
            return parameter;
        }

        parameter.setDescription(JandexUtil.stringValue(annotationInstance, Parameterizable.PROP_DESCRIPTION));
        parameter.setRequired(JandexUtil.booleanValue(annotationInstance, Parameterizable.PROP_REQUIRED).orElse(null));
        parameter.setDeprecated(JandexUtil.booleanValue(annotationInstance, Parameterizable.PROP_DEPRECATED).orElse(null));
        parameter.setAllowEmptyValue(
                JandexUtil.booleanValue(annotationInstance, Parameterizable.PROP_ALLOW_EMPTY_VALUE).orElse(null));
        parameter.setStyle(JandexUtil.enumValue(annotationInstance, Parameterizable.PROP_STYLE,
                org.eclipse.microprofile.openapi.models.parameters.Parameter.Style.class));
        parameter.setExplode(readExplode(JandexUtil.enumValue(annotationInstance, Parameterizable.PROP_EXPLODE,
                org.eclipse.microprofile.openapi.annotations.enums.Explode.class)).orElse(null));
        parameter.setAllowReserved(
                JandexUtil.booleanValue(annotationInstance, ParameterConstant.PROP_ALLOW_RESERVED).orElse(null));
        parameter.setSchema(
                SchemaFactory.readSchema(context.getIndex(),
                        context.getClassLoader(),
                        annotationInstance.value(Parameterizable.PROP_SCHEMA)));
        parameter.setContent(
                ContentReader.readContent(context, annotationInstance.value(Parameterizable.PROP_CONTENT),
                        ContentDirection.PARAMETER));
        parameter.setExamples(ExampleReader.readExamples(annotationInstance.value(Parameterizable.PROP_EXAMPLES)));
        parameter.setExample(JandexUtil.stringValue(annotationInstance, Parameterizable.PROP_EXAMPLE));
        parameter.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.PARAMETER));
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
    private static Optional<Boolean> readExplode(Explode enumValue) {
        if (enumValue == Explode.TRUE) {
            return Optional.of(Boolean.TRUE);
        }
        if (enumValue == Explode.FALSE) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
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
