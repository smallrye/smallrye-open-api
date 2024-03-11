package io.smallrye.openapi.runtime.io.media;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.examples.Example;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.examples.ExampleImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;

public class ExampleObjectIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Example, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_VALUE = "value";
    private static final String PROP_SUMMARY = "summary";
    private static final String PROP_EXTERNAL_VALUE = "externalValue";
    private static final String PROP_DESCRIPTION = "description";

    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public ExampleObjectIO(IOContext<V, A, O, AB, OB> context, ExtensionIO<V, A, O, AB, OB> extensionIO) {
        super(context, Names.EXAMPLE_OBJECT, Names.create(Example.class));
        this.extensionIO = extensionIO;
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
    public Example readObject(O node) {
        IoLogging.logger.singleJsonNode("ExampleObjectIO");
        Example example = new ExampleImpl();
        example.setRef(readReference(node));
        example.setSummary(jsonIO().getString(node, PROP_SUMMARY));
        example.setDescription(jsonIO().getString(node, PROP_DESCRIPTION));
        example.setValue(jsonIO().fromJson(jsonIO().getValue(node, PROP_VALUE)));
        example.setExternalValue(jsonIO().getString(node, PROP_EXTERNAL_VALUE));
        example.setExtensions(extensionIO.readMap(node));
        return example;
    }

    public Optional<O> write(Example model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                setReference(node, model);
            } else {
                setIfPresent(node, PROP_SUMMARY, jsonIO().toJson(model.getSummary()));
                setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
                setIfPresent(node, PROP_VALUE, jsonIO().toJson(model.getValue()));
                setIfPresent(node, PROP_EXTERNAL_VALUE, jsonIO().toJson(model.getExternalValue()));
                setAllIfPresent(node, extensionIO.write(model));
            }

            return node;
        }).map(jsonIO()::buildObject);
    }

    /**
     * Reads an example value and decode it, the parsing is delegated to the extensions
     * currently set in the scanner. The default value will parse the string using Jackson.
     *
     * @param value the value to decode
     * @return a Java representation of the 'value' property, either a String or parsed value
     *
     */
    public Object parseValue(String value) {
        Object parsedValue = value;

        if (value != null) {
            for (AnnotationScannerExtension e : scannerContext().getExtensions()) {
                parsedValue = e.parseValue(value);
                if (parsedValue != null) {
                    break;
                }
            }
        }

        return parsedValue;
    }
}
