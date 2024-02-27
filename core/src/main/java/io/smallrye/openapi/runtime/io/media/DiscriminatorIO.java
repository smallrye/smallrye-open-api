package io.smallrye.openapi.runtime.io.media;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.models.media.DiscriminatorImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.util.ModelUtil;

public class DiscriminatorIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Discriminator, V, A, O, AB, OB> {

    private static final String PROP_MAPPING = "mapping";
    private static final String PROP_PROPERTY_NAME = "propertyName";

    public DiscriminatorIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.DISCRIMINATOR_MAPPING, Names.create(Discriminator.class));
    }

    /**
     * Reads a discriminator property name and an optional array of
     * {@link org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping @DiscriminatorMapping}
     * annotations into a {@link Discriminator} model.
     *
     * @param annotation
     *        a {@code @Schema} annotation containing the
     *        {@link org.eclipse.microprofile.openapi.annotations.media.Schema#discriminatorProperty()
     *        discriminatorProperty} and
     *        {@link org.eclipse.microprofile.openapi.annotations.media.Schema#discriminatorMapping()
     *        discriminatorMapping} properties.
     * @return a {@linkplain Discriminator} or null
     */
    @Override
    public Discriminator read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotationAs("@Schema", "Discriminator");
        String propertyName = value(annotation, SchemaConstant.PROP_DISCRIMINATOR_PROPERTY);
        AnnotationInstance[] mapping = value(annotation, SchemaConstant.PROP_DISCRIMINATOR_MAPPING);

        if (propertyName == null && mapping == null) {
            return null;
        }

        Discriminator discriminator = new DiscriminatorImpl();

        /*
         * The name is required by OAS, however MP OpenAPI allows for a default
         * (blank) name. This results in an invalid OpenAPI document if
         * considering annotation scanning in isolation.
         */
        if (propertyName != null) {
            discriminator.setPropertyName(propertyName);
        }

        if (mapping != null) {
            IoLogging.logger.annotationsList("@DiscriminatorMapping");
            Arrays.stream(mapping).map(this::readMapping)
                    .forEach(e -> discriminator.addMapping(e.getKey(), e.getValue()));
        }

        return discriminator;
    }

    private Map.Entry<String, String> readMapping(AnnotationInstance mapping) {
        String propertyValue = value(mapping, SchemaConstant.PROP_VALUE);
        Type schemaType = value(mapping, SchemaConstant.PROP_SCHEMA);

        String schemaRef;

        if (schemaType != null) {
            Schema schema = SchemaFactory.typeToSchema(context, schemaType);
            schemaRef = schema != null ? schema.getRef() : null;
        } else {
            schemaRef = null;
        }

        if (propertyValue == null && schemaRef != null) {
            // No mapping key provided, use the implied value.
            propertyValue = ModelUtil.nameFromRef(schemaRef);
        }

        return entry(propertyValue, schemaRef);
    }

    @Override
    public Discriminator readObject(O node) {
        Discriminator discriminator = new DiscriminatorImpl();
        discriminator.setPropertyName(jsonIO.getString(node, PROP_PROPERTY_NAME));
        discriminator.setMapping(jsonIO.getObject(node, PROP_MAPPING)
                .map(jsonIO::properties)
                .map(properties -> properties.stream()
                        .map(entry -> entry(entry.getKey(), jsonIO.asString(entry.getValue())))
                        .collect(toLinkedMap()))
                .orElse(null));
        return discriminator;
    }

    public Optional<O> write(Discriminator model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_PROPERTY_NAME, jsonIO.toJson(model.getPropertyName()));
            setIfPresent(node, PROP_MAPPING, jsonIO.toJson(model.getMapping()));
            return node;
        }).map(jsonIO::buildObject);
    }
}
