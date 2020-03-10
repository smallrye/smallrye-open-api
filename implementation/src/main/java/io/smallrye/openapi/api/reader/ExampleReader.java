package io.smallrye.openapi.api.reader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.examples.ExampleImpl;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Example annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#exampleObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExampleReader {
    private static final Logger LOG = Logger.getLogger(ExampleReader.class);

    private ExampleReader() {
    }

    /**
     * Reads a map of Example annotations.
     * 
     * @param annotationValue map of {@literal @}ExampleObject annotations
     * @return Map of Example model
     */
    public static Map<String, Example> readExamples(AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @ExampleObject annotations.");
        Map<String, Example> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readExample(nested));
            }
        }
        return map;
    }

    /**
     * Reads a Example annotation into a model.
     * 
     * @param annotationInstance {@literal @}ExampleObject annotation
     * @return Example model
     */
    private static Example readExample(AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @ExampleObject annotation.");
        Example example = new ExampleImpl();
        example.setSummary(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_SUMMARY));
        example.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        example.setValue(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_VALUE));
        example.setExternalValue(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_EXTERNAL_VALUE));
        example.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.Example));
        return example;
    }
}
