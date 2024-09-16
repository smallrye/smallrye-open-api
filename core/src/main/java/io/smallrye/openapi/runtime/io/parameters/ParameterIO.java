package io.smallrye.openapi.runtime.io.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.api.models.parameters.ParameterImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IOContext.OpenApiVersion;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;

public class ParameterIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Parameter, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_ALLOW_EMPTY_VALUE = "allowEmptyValue";
    private static final String PROP_ALLOW_RESERVED = "allowReserved";
    private static final String PROP_CONTENT = "content";
    private static final String PROP_DEPRECATED = "deprecated";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_EXAMPLE = "example";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_EXPLODE = "explode";
    private static final String PROP_HIDDEN = "hidden";
    private static final String PROP_IN = "in";
    private static final String PROP_NAME = "name";
    private static final String PROP_REQUIRED = "required";
    private static final String PROP_SCHEMA = "schema";
    private static final String PROP_STYLE = "style";

    public ParameterIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.PARAMETER, Names.create(Parameter.class));
    }

    public List<Parameter> readList(AnnotationValue annotations) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(this::readList)
                .orElse(null);
    }

    public List<Parameter> readList(AnnotationInstance[] annotations) {
        return readList(Arrays.asList(annotations));
    }

    public List<Parameter> readList(Collection<AnnotationInstance> annotations) {
        return annotations.stream()
                .filter(not(this::isReference))
                .map(this::read)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Parameter read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@Parameter");
        ParameterImpl parameter = new ParameterImpl();
        parameter.setName(value(annotation, PROP_NAME));
        parameter.setIn(enumValue(annotation, PROP_IN, Parameter.In.class));

        // Params can be hidden. Skip if that's the case.
        if (Boolean.TRUE.equals(value(annotation, PROP_HIDDEN))) {
            ParameterImpl.setHidden(parameter, true);
            return parameter;
        }

        parameter.setDescription(value(annotation, PROP_DESCRIPTION));
        parameter.setRequired(value(annotation, PROP_REQUIRED));
        parameter.setDeprecated(value(annotation, PROP_DEPRECATED));
        parameter.setAllowEmptyValue(value(annotation, PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(enumValue(annotation, PROP_STYLE, Parameter.Style.class));
        parameter.setExplode(readExplode(scannerContext(), annotation));
        parameter.setAllowReserved(value(annotation, PROP_ALLOW_RESERVED));
        parameter.setSchema(schemaIO().read(annotation.value(PROP_SCHEMA)));
        parameter.setContent(contentIO().read(annotation.value(PROP_CONTENT), ContentIO.Direction.PARAMETER));
        parameter.setExamples(exampleObjectIO().readMap(annotation.value(PROP_EXAMPLES)));
        parameter.setExample(exampleObjectIO().parseValue(value(annotation, PROP_EXAMPLE)));
        parameter.setRef(ReferenceType.PARAMETER.refValue(annotation));

        if (annotation.target() != null) {
            switch (annotation.target().kind()) {
                case FIELD:
                case METHOD_PARAMETER:
                    /*
                     * Limit to field and parameter. Extensions on methods are ambiguous and pertain
                     * instead to the operation.
                     *
                     */
                    parameter.setExtensions(extensionIO().readExtensible(annotation));
                    break;
                default:
                    break;
            }

            parameter.setParamRef(JandexUtil.createUniqueAnnotationTargetRef(annotation.target()));
        }

        return parameter;
    }

    public List<Parameter> readList(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO()::isArray)
                .map(jsonIO()::asArray)
                .map(jsonIO()::entries)
                .map(Collection::stream)
                .map(elements -> {
                    IoLogging.logger.jsonArray("Parameter");
                    return elements.filter(jsonIO()::isObject)
                            .map(jsonIO()::asObject)
                            .map(this::readObject)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .orElse(null);
    }

    @Override
    public Parameter readObject(O node) {
        IoLogging.logger.singleJsonObject("Parameter");
        Parameter parameter = new ParameterImpl();
        parameter.setName(jsonIO().getString(node, PROP_NAME));
        parameter.setIn(enumValue(jsonIO().getValue(node, PROP_IN), Parameter.In.class));
        parameter.setDescription(jsonIO().getString(node, PROP_DESCRIPTION));
        parameter.setRequired(jsonIO().getBoolean(node, PROP_REQUIRED));
        parameter.setDeprecated(jsonIO().getBoolean(node, PROP_DEPRECATED));
        parameter.setAllowEmptyValue(jsonIO().getBoolean(node, PROP_ALLOW_EMPTY_VALUE));
        parameter.setStyle(enumValue(jsonIO().getValue(node, PROP_STYLE), Parameter.Style.class));
        parameter.setExplode(jsonIO().getBoolean(node, PROP_EXPLODE));
        parameter.setAllowReserved(jsonIO().getBoolean(node, PROP_ALLOW_RESERVED));
        parameter.setSchema(schemaIO().readValue(jsonIO().getValue(node, PROP_SCHEMA)));
        parameter.setContent(contentIO().readValue(jsonIO().getValue(node, PROP_CONTENT)));
        parameter.setExamples(exampleObjectIO().readMap(jsonIO().getValue(node, PROP_EXAMPLES)));
        parameter.setExample(jsonIO().fromJson(jsonIO().getValue(node, PROP_EXAMPLE)));
        parameter.setRef(readReference(node));
        parameter.setExtensions(extensionIO().readMap(node));

        return parameter;
    }

    public Optional<A> write(List<Parameter> models) {
        return optionalJsonArray(models).map(array -> {
            models.forEach(model -> write(model).ifPresent(v -> jsonIO().add(array, v)));
            return array;
        }).map(jsonIO()::buildArray);
    }

    /**
     * Writes a {@link Parameter} into the JSON node.
     *
     * @param model
     */
    public Optional<O> write(Parameter model) {
        return optionalJsonObject(model).map(node -> {
            if (isReference(model)) {
                setReference(node, model);
                if (openApiVersion() == OpenApiVersion.V3_1) {
                    setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
                }
            } else {
                setIfPresent(node, PROP_NAME, jsonIO().toJson(model.getName()));
                setIfPresent(node, PROP_IN, jsonIO().toJson(model.getIn()));
                setIfPresent(node, PROP_DESCRIPTION, jsonIO().toJson(model.getDescription()));
                setIfPresent(node, PROP_REQUIRED, jsonIO().toJson(model.getRequired()));
                setIfPresent(node, PROP_SCHEMA, schemaIO().write(model.getSchema()));
                setIfPresent(node, PROP_ALLOW_EMPTY_VALUE, jsonIO().toJson(model.getAllowEmptyValue()));
                setIfPresent(node, PROP_DEPRECATED, jsonIO().toJson(model.getDeprecated()));
                setIfPresent(node, PROP_STYLE, jsonIO().toJson(model.getStyle()));
                setIfPresent(node, PROP_EXPLODE, jsonIO().toJson(model.getExplode()));
                setIfPresent(node, PROP_ALLOW_RESERVED, jsonIO().toJson(model.getAllowReserved()));
                setIfPresent(node, PROP_EXAMPLE, jsonIO().toJson(model.getExample()));
                setIfPresent(node, PROP_EXAMPLES, exampleObjectIO().write(model.getExamples()));
                setIfPresent(node, PROP_CONTENT, contentIO().write(model.getContent()));
                setAllIfPresent(node, extensionIO().write(model));
            }
            return node;
        }).map(jsonIO()::buildObject);
    }

    /**
     * Converts from an Explode enum to a true/false/null.
     *
     * @param enumValue
     */
    private static Boolean readExplode(AnnotationScannerContext context, AnnotationInstance parameterAnnoatation) {
        Explode explode = context.annotations().enumValue(parameterAnnoatation, PROP_EXPLODE, Explode.class);

        if (explode == Explode.TRUE) {
            return Boolean.TRUE;
        }
        if (explode == Explode.FALSE) {
            return Boolean.FALSE;
        }
        return null; // NOSONAR
    }

}
