package io.smallrye.openapi.runtime.io.example;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.examples.ExampleImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * Reading the Example annotation
 *
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#exampleObject">exampleObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExampleReader {

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
        IoLogging.logger.annotationsMap("@ExampleObject");
        Map<String, Example> examples = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, ExampleConstant.PROP_NAME);
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
        IoLogging.logger.singleAnnotation("@ExampleObject");
        Example example = new ExampleImpl();
        example.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.EXAMPLE));
        example.setSummary(JandexUtil.stringValue(annotationInstance, ExampleConstant.PROP_SUMMARY));
        example.setDescription(JandexUtil.stringValue(annotationInstance, ExampleConstant.PROP_DESCRIPTION));
        example.setValue(JsonUtil.parseValue(JandexUtil.stringValue(annotationInstance, ExampleConstant.PROP_VALUE)));
        example.setExternalValue(JandexUtil.stringValue(annotationInstance, ExampleConstant.PROP_EXTERNAL_VALUE));

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
        IoLogging.logger.singleJsonNode("ExampleObject");
        Example example = new ExampleImpl();
        example.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        example.setSummary(JsonUtil.stringProperty(node, ExampleConstant.PROP_SUMMARY));
        example.setDescription(JsonUtil.stringProperty(node, ExampleConstant.PROP_DESCRIPTION));
        example.setValue(readObject(node.get(ExampleConstant.PROP_VALUE)));
        example.setExternalValue(JsonUtil.stringProperty(node, ExampleConstant.PROP_EXTERNAL_VALUE));
        ExtensionReader.readExtensions(node, example);
        return example;
    }
}
