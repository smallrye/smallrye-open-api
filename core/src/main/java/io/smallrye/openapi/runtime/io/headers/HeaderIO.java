package io.smallrye.openapi.runtime.io.headers;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.headers.HeaderImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.io.media.ExampleObjectIO;
import io.smallrye.openapi.runtime.io.media.SchemaIO;

public class HeaderIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Header, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_EXAMPLE = "example";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_CONTENT = "content";
    private static final String PROP_SCHEMA = "schema";
    private static final String PROP_EXPLODE = "explode";
    private static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
    private static final String PROP_REQUIRED = "required";
    private static final String PROP_DEPRECATED = "deprecated";
    private static final String PROP_STYLE = "style";

    private final SchemaIO<V, A, O, AB, OB> schemaIO;
    private final ContentIO<V, A, O, AB, OB> contentIO;
    private final ExampleObjectIO<V, A, O, AB, OB> exampleObjectIO;
    private final ExtensionIO<V, A, O, AB, OB> extensionIO;

    public HeaderIO(IOContext<V, A, O, AB, OB> context, ContentIO<V, A, O, AB, OB> contentIO) {
        super(context, Names.HEADER, Names.create(Header.class));
        this.contentIO = contentIO;
        exampleObjectIO = new ExampleObjectIO<>(context);
        schemaIO = new SchemaIO<>(context);
        extensionIO = new ExtensionIO<>(context);
    }

    @Override
    public Header read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Header");
        Header header = new HeaderImpl();
        header.setRef(ReferenceType.HEADER.refValue(annotation));
        header.setDescription(context.annotations().value(annotation, PROP_DESCRIPTION));
        header.setSchema(schemaIO.read(annotation.value(PROP_SCHEMA)));
        header.setRequired(context.annotations().value(annotation, PROP_REQUIRED));
        header.setDeprecated(context.annotations().value(annotation, PROP_DEPRECATED));
        header.setAllowEmptyValue(context.annotations().value(annotation, PROP_ALLOW_EMPTY_VALUE));
        header.setExtensions(extensionIO.readExtensible(annotation));
        return header;
    }

    @Override
    public Header readObject(O node) {
        IoLogging.logger.singleJsonNode("Header");
        Header header = new HeaderImpl();
        header.setRef(readReference(node));
        header.setDescription(jsonIO.getString(node, PROP_DESCRIPTION));
        header.setSchema(schemaIO.readValue(jsonIO.getValue(node, PROP_SCHEMA)));
        header.setRequired(jsonIO.getBoolean(node, PROP_REQUIRED));
        header.setDeprecated(jsonIO.getBoolean(node, PROP_DEPRECATED));
        header.setAllowEmptyValue(jsonIO.getBoolean(node, PROP_ALLOW_EMPTY_VALUE));
        header.setStyle(enumValue(jsonIO.getValue(node, PROP_STYLE), Header.Style.class));
        header.setExplode(jsonIO.getBoolean(node, PROP_EXPLODE));
        header.setExample(jsonIO.fromJson(jsonIO.getValue(node, PROP_EXAMPLE)));
        header.setExamples(exampleObjectIO.readMap(jsonIO.getValue(node, PROP_EXAMPLES)));
        header.setContent(contentIO.readValue(jsonIO.getValue(node, PROP_CONTENT)));
        header.setExtensions(extensionIO.readMap(node));
        return header;
    }

    @Override
    public Optional<O> write(Header model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                setReference(node, model);
            } else {
                setIfPresent(node, PROP_DESCRIPTION, jsonIO.toJson(model.getDescription()));
                setIfPresent(node, PROP_REQUIRED, jsonIO.toJson(model.getRequired()));
                setIfPresent(node, PROP_DEPRECATED, jsonIO.toJson(model.getDeprecated()));
                setIfPresent(node, PROP_ALLOW_EMPTY_VALUE, jsonIO.toJson(model.getAllowEmptyValue()));
                setIfPresent(node, PROP_STYLE, jsonIO.toJson(model.getStyle()));
                setIfPresent(node, PROP_EXPLODE, jsonIO.toJson(model.getExplode()));
                setIfPresent(node, PROP_SCHEMA, schemaIO.write(model.getSchema()));
                setIfPresent(node, PROP_EXAMPLE, jsonIO.toJson(model.getExample()));
                setIfPresent(node, PROP_EXAMPLES, exampleObjectIO.write(model.getExamples()));
                setIfPresent(node, PROP_CONTENT, contentIO.write(model.getContent()));
                setAllIfPresent(node, extensionIO.write(model));
            }
            return node;
        }).map(jsonIO::buildObject);
    }
}
