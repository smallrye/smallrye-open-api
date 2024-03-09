package io.smallrye.openapi.runtime.io.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class SecurityIO<V, A extends V, O extends V, AB, OB> {

    private final SecurityRequirementIO<V, A, O, AB, OB> securityRequirementIO;
    private final SecurityRequirementsSetIO<V, A, O, AB, OB> securityRequirementsSetIO;
    private final SecuritySchemeIO<V, A, O, AB, OB> securitySchemeIO;

    public SecurityIO(IOContext<V, A, O, AB, OB> context, ExtensionIO<V, A, O, AB, OB> extensionIO) {
        securityRequirementIO = new SecurityRequirementIO<>(context);
        securityRequirementsSetIO = new SecurityRequirementsSetIO<>(context);
        securitySchemeIO = new SecuritySchemeIO<>(context, extensionIO);
    }

    public List<SecurityRequirement> readRequirements(AnnotationTarget target) {
        return Stream.of(
                securityRequirementIO.readList(target),
                securityRequirementsSetIO.readList(target))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<SecurityRequirement> readRequirements(AnnotationValue annotations, AnnotationValue setAnnotations) {
        List<List<SecurityRequirement>> requirements = Stream.of(
                securityRequirementIO.readList(annotations),
                securityRequirementsSetIO.readList(setAnnotations))
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
        return securitySchemeIO.readMap(target);
    }

    public List<SecurityRequirement> readRequirements(V node) {
        return securityRequirementIO.readList(node);
    }

    public Optional<A> write(List<SecurityRequirement> models) {
        return securityRequirementIO.write(models);
    }
}
