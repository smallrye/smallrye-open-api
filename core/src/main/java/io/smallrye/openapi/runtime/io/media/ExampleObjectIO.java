package io.smallrye.openapi.runtime.io.media;

import static io.smallrye.openapi.runtime.io.JsonUtil.readObject;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.jboss.jandex.AnnotationInstance;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.examples.ExampleImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.Referenceable;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ExampleObjectIO extends MapModelIO<Example> implements ReferenceIO {

    private static final String PROP_VALUE = "value";
    private static final String PROP_SUMMARY = "summary";
    private static final String PROP_EXTERNAL_VALUE = "externalValue";
    private static final String PROP_DESCRIPTION = "description";

    private final ExtensionIO extensionIO;

    public ExampleObjectIO(AnnotationScannerContext context) {
        super(context, Names.EXAMPLE_OBJECT, Names.create(Example.class));
        extensionIO = new ExtensionIO(context);
    }

    @Override
    public Example read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@ExampleObject");
        Example example = new ExampleImpl();
        example.setRef(ReferenceType.EXAMPLE.refValue(annotation));
        example.setSummary(value(annotation, PROP_SUMMARY));
        example.setDescription(value(annotation, PROP_DESCRIPTION));
        example.setValue(parseValue(value(annotation, PROP_VALUE)));
        example.setExternalValue(value(annotation, PROP_EXTERNAL_VALUE));
        example.setExtensions(extensionIO.readExtensible(annotation));
        return example;
    }

    @Override
    public Example read(ObjectNode node) {
        IoLogging.logger.singleJsonNode("ExampleObjectIO");
        Example example = new ExampleImpl();
        example.setRef(JsonUtil.stringProperty(node, Referenceable.PROP_$REF));
        example.setSummary(JsonUtil.stringProperty(node, PROP_SUMMARY));
        example.setDescription(JsonUtil.stringProperty(node, PROP_DESCRIPTION));
        example.setValue(readObject(node.get(PROP_VALUE)));
        example.setExternalValue(JsonUtil.stringProperty(node, PROP_EXTERNAL_VALUE));
        extensionIO.readMap(node).forEach(example::addExtension);
        return example;
    }

    public Optional<ObjectNode> write(Example model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                JsonUtil.stringProperty(node, Referenceable.PROP_$REF, model.getRef());
            } else {
                JsonUtil.stringProperty(node, PROP_SUMMARY, model.getSummary());
                JsonUtil.stringProperty(node, PROP_DESCRIPTION, model.getDescription());
                ObjectWriter.writeObject(node, PROP_VALUE, model.getValue());
                JsonUtil.stringProperty(node, PROP_EXTERNAL_VALUE, model.getExternalValue());
                extensionIO.write(model).ifPresent(node::setAll);
            }

            return node;
        });
    }

    /**
     * Reads an example value and decode it, the parsing is delegated to the extensions
     * currently set in the scanner. The default value will parse the string using Jackson.
     *
     * @param context the scanning context
     * @param value the value to decode
     * @return a Java representation of the 'value' property, either a String or parsed value
     *
     */
    public Object parseValue(String value) {
        Object parsedValue = value;

        if (value != null) {
            for (AnnotationScannerExtension e : context.getExtensions()) {
                parsedValue = e.parseValue(value);
                if (parsedValue != null) {
                    break;
                }
            }
        }

        return parsedValue;
    }
}
