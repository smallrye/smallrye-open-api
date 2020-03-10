package io.smallrye.openapi.api.reader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;

/**
 * Reading the Parameter annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#parameter-object
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
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return Map of Parameter model
     */
    public static List<Parameter> readParametersAsList(final AnnotationScannerContext context,
            final AnnotationValue annotationValue,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a list of @Parameter annotations.");
        List<Parameter> parameters = new ArrayList<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            Parameter parameter = readParameter(context, nested, currentConsumes, currentProduces);
            if (parameter != null) {
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    /**
     * Reads a map of Parameter annotations.
     * 
     * @param context the scanning context
     * @param annotationValue Map of {@literal @}Parameter annotations
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return Map of Parameter model
     */
    public static Map<String, Parameter> readParameters(final AnnotationScannerContext context,
            final AnnotationValue annotationValue,
            final String[] currentConsumes,
            final String[] currentProduces) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @Parameter annotations.");
        Map<String, Parameter> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                Parameter parameter = readParameter(context, nested, currentConsumes, currentProduces);
                if (parameter != null) {
                    map.put(name, parameter);
                }
            }
        }
        return map;
    }

    /**
     * Reads a Parameter annotation into a model.
     * 
     * @param context the scanning context
     * @param annotationInstance {@literal @}Parameter model
     * @param currentConsumes the current document consumes value
     * @param currentProduces the current document produces value
     * @return Parameter model
     */
    public static Parameter readParameter(final AnnotationScannerContext context,
            final AnnotationInstance annotationInstance,
            final String[] currentConsumes,
            final String[] currentProduces) {

        if (annotationInstance == null) {
            return null;
        }

        LOG.debug("Processing a single @Parameter annotation.");

        Parameter parameter = new ParameterImpl();
        parameter.setName(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_NAME));
        parameter.setIn(JandexUtil.enumValue(annotationInstance, OpenApiConstants.PROP_IN,
                org.eclipse.microprofile.openapi.models.parameters.Parameter.In.class));

        // Params can be hidden. Skip if that's the case.
        Boolean isHidden = JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_HIDDEN);
        if (Boolean.TRUE.equals(isHidden)) {
            ParameterImpl.setHidden(parameter, true);
            return parameter;
        }

        parameter.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        parameter.setRequired(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_REQUIRED));
        parameter.setDeprecated(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_DEPRECATED));
        parameter.setAllowEmptyValue(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(JandexUtil.enumValue(annotationInstance, OpenApiConstants.PROP_STYLE,
                org.eclipse.microprofile.openapi.models.parameters.Parameter.Style.class));
        parameter.setExplode(readExplode(JandexUtil.enumValue(annotationInstance, OpenApiConstants.PROP_EXPLODE,
                org.eclipse.microprofile.openapi.annotations.enums.Explode.class)));
        parameter.setAllowReserved(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_ALLOW_RESERVED));
        parameter.setSchema(
                SchemaFactory.readSchema(context.getIndex(), annotationInstance.value(OpenApiConstants.PROP_SCHEMA)));
        parameter.setContent(MediaTypeObjectReader.readContent(context, annotationInstance.value(OpenApiConstants.PROP_CONTENT),
                ContentDirection.Parameter, currentConsumes, currentProduces));
        parameter.setExamples(ExampleReader.readExamples(annotationInstance.value(OpenApiConstants.PROP_EXAMPLES)));
        parameter.setExample(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_EXAMPLE));
        parameter.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.Parameter));
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
}
