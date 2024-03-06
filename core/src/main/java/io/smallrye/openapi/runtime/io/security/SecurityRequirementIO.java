package io.smallrye.openapi.runtime.io.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class SecurityRequirementIO<V, A extends V, O extends V, AB, OB> extends ModelIO<SecurityRequirement, V, A, O, AB, OB> {

    private static final String PROP_NAME = "name";
    private static final String PROP_SCOPES = "scopes";

    public SecurityRequirementIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.SECURITY_REQUIREMENT, Names.create(SecurityRequirement.class));
    }

    public List<SecurityRequirement> readList(AnnotationTarget target) {
        return readList(getRepeatableAnnotations(target));
    }

    public List<SecurityRequirement> readList(AnnotationValue annotations) {
        return Optional.ofNullable(annotations)
                .map(AnnotationValue::asNestedArray)
                .map(this::readList)
                .orElse(null);
    }

    public List<SecurityRequirement> readList(AnnotationInstance[] annotations) {
        return readList(Arrays.asList(annotations));
    }

    public List<SecurityRequirement> readList(Collection<AnnotationInstance> annotations) {
        return annotations.stream().map(this::read).collect(Collectors.toList());
    }

    @Override
    public SecurityRequirement read(AnnotationInstance annotation) {
        Map.Entry<String, List<String>> scheme = readEntry(annotation);
        return new SecurityRequirementImpl().addScheme(scheme.getKey(), scheme.getValue());
    }

    Map.Entry<String, List<String>> readEntry(AnnotationInstance annotation) {
        String name = value(annotation, PROP_NAME);
        String[] scopes = value(annotation, PROP_SCOPES);

        if (scopes != null) {
            return entry(name, new ArrayList<>(Arrays.asList(scopes)));
        } else {
            return entry(name, null);
        }
    }

    public List<SecurityRequirement> readList(V node) {
        return Optional.ofNullable(node)
                .filter(jsonIO()::isArray)
                .map(jsonIO()::asArray)
                .map(jsonIO()::entries)
                .map(Collection::stream)
                .map(elements -> {
                    IoLogging.logger.jsonArray("SecurityRequirement");
                    return elements.filter(jsonIO()::isObject)
                            .map(jsonIO()::asObject)
                            .map(this::readObject)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .orElse(null);
    }

    @Override
    public SecurityRequirement readObject(O node) {
        SecurityRequirement requirement = new SecurityRequirementImpl();

        jsonIO().properties(node).forEach(field -> {
            String schemeName = field.getKey();

            if (jsonIO().isArray(field.getValue())) {
                A scopeArray = jsonIO().asArray(field.getValue());
                List<String> scopes = jsonIO().entries(scopeArray)
                        .stream()
                        .map(jsonIO()::asString)
                        .collect(Collectors.toList());
                requirement.addScheme(schemeName, scopes);
            } else {
                requirement.addScheme(schemeName);
            }
        });

        return requirement;
    }

    public Optional<A> write(List<SecurityRequirement> models) {
        return optionalJsonArray(models).map(array -> {
            for (SecurityRequirement model : models) {
                write(model).ifPresent(object -> jsonIO().add(array, object));
            }
            return array;
        }).map(jsonIO()::buildArray);
    }

    @Override
    public Optional<O> write(SecurityRequirement model) {
        return optionalJsonObject(model.getSchemes()).map(node -> {
            model.getSchemes()
                    .forEach((schemeName, scopes) -> jsonIO().set(node, schemeName, jsonIO().toJson(scopes)
                            .orElseGet(() -> jsonIO().buildArray(jsonIO().createArray()))));
            return node;
        }).map(jsonIO()::buildObject);
    }
}
