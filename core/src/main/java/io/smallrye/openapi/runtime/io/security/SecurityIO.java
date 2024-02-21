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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class SecurityIO {

    private final SecurityRequirementIO securityRequirementIO;
    private final SecurityRequirementsSetIO securityRequirementsSetIO;
    private final SecuritySchemeIO securitySchemeIO;

    public SecurityIO(AnnotationScannerContext context) {
        securityRequirementIO = new SecurityRequirementIO(context);
        securityRequirementsSetIO = new SecurityRequirementsSetIO(context);
        securitySchemeIO = new SecuritySchemeIO(context);
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

    public List<SecurityRequirement> readRequirements(JsonNode node) {
        return securityRequirementIO.readList(node);
    }

    public Optional<ArrayNode> write(List<SecurityRequirement> models) {
        return securityRequirementIO.write(models);
    }
}
