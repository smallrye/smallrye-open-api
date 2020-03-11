package io.smallrye.openapi.runtime.reader;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.examples.ExampleImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
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
    public static Map<String, Example> readExamples(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @ExampleObject annotations.");
        Map<String, Example> examples = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, MPOpenApiConstants.EXAMPLE.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                examples.put(name, readExample(nested));
            }
        }
        return examples;
    }

    /**
     * Reads the {@link Example} OpenAPI nodes.
     * 
     * @param node map of json nodes
     * @return Map of Example model
     */
    public static Map<String, Example> readExamples(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Example> examples = new LinkedHashMap<>();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode childNode = node.get(fieldName);
            examples.put(fieldName, readExample(childNode));
        }

        return examples;
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
        example.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.Example));
        example.setSummary(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.EXAMPLE.PROP_SUMMARY));
        example.setDescription(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.EXAMPLE.PROP_DESCRIPTION));
        example.setValue(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.EXAMPLE.PROP_VALUE));
        example.setExternalValue(JandexUtil.stringValue(annotationInstance, MPOpenApiConstants.EXAMPLE.PROP_EXTERNAL_VALUE));

        return example;
    }

    /**
     * Reads a {@link Example} OpenAPI node.
     * 
     * @param node the example json node
     * @return Example model
     */
    private static Example readExample(final JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        LOG.debug("Processing a single ExampleObject json.");
        Example example = new ExampleImpl();
        example.setRef(JsonUtil.stringProperty(node, MPOpenApiConstants.EXAMPLE.PROP_REF_VAR));
        example.setSummary(JsonUtil.stringProperty(node, MPOpenApiConstants.EXAMPLE.PROP_SUMMARY));
        example.setDescription(JsonUtil.stringProperty(node, MPOpenApiConstants.EXAMPLE.PROP_DESCRIPTION));
        example.setValue(readObject(node.get(MPOpenApiConstants.EXAMPLE.PROP_VALUE)));
        example.setExternalValue(JsonUtil.stringProperty(node, MPOpenApiConstants.EXAMPLE.PROP_EXTERNAL_VALUE));
        ExtensionReader.readExtensions(node, example);
        return example;
    }
}
