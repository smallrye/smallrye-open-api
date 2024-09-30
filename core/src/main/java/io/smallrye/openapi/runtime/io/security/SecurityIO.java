package io.smallrye.openapi.runtime.io.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.io.IOContext;

public class SecurityIO<V, A extends V, O extends V, AB, OB> {

    private final IOContext<V, A, O, AB, OB> context;

    public SecurityIO(IOContext<V, A, O, AB, OB> context) {
        this.context = context;
    }

    public List<SecurityRequirement> readRequirements(AnnotationTarget target) {
        return Stream.of(
                context.securityRequirementIO().readList(target),
                context.securityRequirementsSetIO().readList(target))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<SecurityRequirement> readRequirements(AnnotationValue annotations, AnnotationValue setAnnotations) {
        List<List<SecurityRequirement>> requirements = Stream.of(
                context.securityRequirementIO().readList(annotations),
                context.securityRequirementsSetIO().readList(setAnnotations))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (requirements.isEmpty()) {
            return null; // NOSONAR - null is the desired result when both sublists were null
        }

        return requirements.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public Map<String, SecurityScheme> readSchemes(AnnotationTarget target) {
        return context.securitySchemeIO().readMap(target);
    }
}
