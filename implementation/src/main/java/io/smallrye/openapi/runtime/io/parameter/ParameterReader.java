package io.smallrye.openapi.runtime.io.parameter;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.runtime.io.ContentDirection;
import io.smallrye.openapi.runtime.io.JsonUtil;
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
    private static final Logger LOG = Logger.getLogger(ParameterReader.class);

    private ParameterReader() {
    }

    /**
     * Reads a map of Parameter annotations.
     * 
     * @param context the scanning context
     * @param annotationValue Map of {@literal @}Parameter annotations
     * @return List of Parameter model
     */
    public static List<Parameter> readParametersList(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a list of @Parameter annotations.");
        List<Parameter> parameters = new ArrayList<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            Parameter parameter = readParameter(context, nested);
            if (parameter != null) {
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    /**
     * Reads a {@link Parameter} OpenAPI node.
     * 
     * @param node json list
     * @return List of Parameter model
     */
    public static List<Parameter> readParameterList(final JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        LOG.debug("Processing a json list of Parameter.");
        List<Parameter> params = new ArrayList<>();
        ArrayNode arrayNode = (ArrayNode) node;
        for (JsonNode paramNode : arrayNode) {
            params.add(ParameterReader.readParameter(paramNode));
        }
        return params;
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
        LOG.debug("Processing a map of @Parameter annotations.");
        Map<String, Parameter> parameters = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, ParameterConstant.PROP_NAME);
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
        LOG.debug("Processing a json map of Parameters.");
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
        LOG.debug("Processing a single @Parameter annotation.");
        Parameter parameter = new ParameterImpl();
        parameter.setName(JandexUtil.stringValue(annotationInstance, ParameterConstant.PROP_NAME));
        parameter.setIn(JandexUtil.enumValue(annotationInstance, ParameterConstant.PROP_IN,
                org.eclipse.microprofile.openapi.models.parameters.Parameter.In.class));

        // Params can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotationInstance, ParameterConstant.PROP_HIDDEN);
        if (Boolean.TRUE.equals(isHidden)) {
            ParameterImpl.setHidden(parameter, true);
            return parameter;
        }

        parameter.setDescription(JandexUtil.stringValue(annotationInstance, ParameterConstant.PROP_DESCRIPTION));
        parameter.setRequired(JandexUtil.booleanValue(annotationInstance, ParameterConstant.PROP_REQUIRED));
        parameter.setDeprecated(JandexUtil.booleanValue(annotationInstance, ParameterConstant.PROP_DEPRECATED));
        parameter.setAllowEmptyValue(
                JandexUtil.booleanValue(annotationInstance, ParameterConstant.PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(JandexUtil.enumValue(annotationInstance, ParameterConstant.PROP_STYLE,
                org.eclipse.microprofile.openapi.models.parameters.Parameter.Style.class));
        parameter.setExplode(readExplode(JandexUtil.enumValue(annotationInstance, ParameterConstant.PROP_EXPLODE,
                org.eclipse.microprofile.openapi.annotations.enums.Explode.class)));
        parameter.setAllowReserved(
                JandexUtil.booleanValue(annotationInstance, ParameterConstant.PROP_ALLOW_RESERVED));
        parameter.setSchema(
                SchemaFactory.readSchema(context.getIndex(),
                        annotationInstance.value(ParameterConstant.PROP_SCHEMA)));
        parameter.setContent(
                ContentReader.readContent(context, annotationInstance.value(ParameterConstant.PROP_CONTENT),
                        ContentDirection.Parameter));
        parameter.setExamples(ExampleReader.readExamples(annotationInstance.value(ParameterConstant.PROP_EXAMPLES)));
        parameter.setExample(JandexUtil.stringValue(annotationInstance, ParameterConstant.PROP_EXAMPLE));
        parameter.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.Parameter));
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
        LOG.debug("Processing a single Parameter json object.");
        Parameter parameter = new ParameterImpl();

        parameter.setName(JsonUtil.stringProperty(node, ParameterConstant.PROP_NAME));
        parameter.setIn(readParameterIn(node.get(ParameterConstant.PROP_IN)));
        parameter.setDescription(JsonUtil.stringProperty(node, ParameterConstant.PROP_DESCRIPTION));
        parameter.setRequired(JsonUtil.booleanProperty(node, ParameterConstant.PROP_REQUIRED));
        parameter.setDeprecated(JsonUtil.booleanProperty(node, ParameterConstant.PROP_DEPRECATED));
        parameter.setAllowEmptyValue(JsonUtil.booleanProperty(node, ParameterConstant.PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(readParameterStyle(node.get(ParameterConstant.PROP_STYLE)));
        parameter.setExplode(JsonUtil.booleanProperty(node, ParameterConstant.PROP_EXPLODE));
        parameter.setAllowReserved(JsonUtil.booleanProperty(node, ParameterConstant.PROP_ALLOW_RESERVED));
        parameter.setSchema(SchemaReader.readSchema(node.get(ParameterConstant.PROP_SCHEMA)));
        parameter.setContent(ContentReader.readContent(node.get(ParameterConstant.PROP_CONTENT)));
        parameter.setExamples(ExampleReader.readExamples(node.get(ParameterConstant.PROP_EXAMPLES)));
        parameter.setExample(readObject(node.get(ParameterConstant.PROP_EXAMPLE)));
        parameter.setRef(JsonUtil.stringProperty(node, ParameterConstant.PROP_$REF));
        ExtensionReader.readExtensions(node, parameter);

        return parameter;
    }

    /**
     * Converts from an Explode enum to a true/false/null.
     * 
     * @param enumValue
     */
    private static Boolean readExplode(Explode enumValue) {
        if (enumValue == Explode.TRUE) {
            return Boolean.TRUE;
        }
        if (enumValue == Explode.FALSE) {
            return Boolean.FALSE;
        }
        return null;
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
