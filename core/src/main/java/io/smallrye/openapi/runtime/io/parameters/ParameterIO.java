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

import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.MapModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ReferenceIO;
import io.smallrye.openapi.runtime.io.media.ContentIO;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class ParameterIO<V, A extends V, O extends V, AB, OB> extends MapModelIO<Parameter, V, A, O, AB, OB>
        implements ReferenceIO<V, A, O, AB, OB> {

    private static final String PROP_CONTENT = "content";
    private static final String PROP_EXAMPLE = "example";
    private static final String PROP_EXAMPLES = "examples";
    private static final String PROP_EXPLODE = "explode";
    private static final String PROP_HIDDEN = "hidden";
    private static final String PROP_IN = "in";
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
    protected boolean setProperty(Parameter model, AnnotationValue value) {
        switch (value.name()) {
            case PROP_IN:
                model.setIn(scannerContext().annotations().enumValue(Parameter.In.class, value));
                return true;
            case PROP_HIDDEN:
                Extensions.setHidden(model, value.asBoolean());
                return true;
            case PROP_STYLE:
                model.setStyle(scannerContext().annotations().enumValue(Parameter.Style.class, value));
                return true;
            case PROP_EXPLODE:
                model.setExplode(readExplode(scannerContext(), value.asString()));
                return true;
            case PROP_SCHEMA:
                model.setSchema(schemaIO().read(value));
                return true;
            case PROP_CONTENT:
                model.setContent(contentIO().read(value, ContentIO.Direction.PARAMETER));
                return true;
            case PROP_EXAMPLES:
                model.setExamples(exampleObjectIO().readMap(value));
                return true;
            case PROP_EXAMPLE:
                model.setExample(exampleObjectIO().parseValue(value.asString()));
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public Parameter read(AnnotationInstance annotation) {
        Parameter parameter = read(Parameter.class, annotation);

        if (annotation.target() != null) {
            Extensions.setParamRef(parameter, annotation.target());
        }

        return parameter;
    }

    /**
     * Converts from an Explode enum to a true/false/null.
     *
     * @param enumValue
     */
    private static Boolean readExplode(AnnotationScannerContext context, String value) {
        Explode explode = context.annotations().enumValue(Explode.class, value);

        if (explode == Explode.TRUE) {
            return Boolean.TRUE;
        }
        if (explode == Explode.FALSE) {
            return Boolean.FALSE;
        }

        return null; // NOSONAR
    }

}
