package io.smallrye.openapi.runtime.io.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class SecurityRequirementsSetIO extends ModelIO<SecurityRequirement> {

    private final SecurityRequirementIO securityRequirementIO;

    public SecurityRequirementsSetIO(AnnotationScannerContext context) {
        super(context, Names.SECURITY_REQUIREMENTS_SET, Names.create(SecurityRequirement.class));
        securityRequirementIO = new SecurityRequirementIO(context);
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

    private List<SecurityRequirement> readList(AnnotationInstance[] annotations) {
        return readList(Arrays.asList(annotations));
    }

    public List<SecurityRequirement> readList(Collection<AnnotationInstance> annotations) {
        return annotations.stream()
            .map(set -> Optional.ofNullable(set.value())
                .map(AnnotationValue::asNestedArray)
                .map(this::readSet)
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    SecurityRequirement readSet(AnnotationInstance[] annotations) {
        SecurityRequirement requirement = new SecurityRequirementImpl();

        Arrays.stream(annotations)
            .map(securityRequirementIO::readEntry)
            .forEach(scheme -> requirement.addScheme(scheme.getKey(), scheme.getValue()));

            return requirement;
    }

    @Override
    public SecurityRequirement read(AnnotationInstance annotation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SecurityRequirement read(ObjectNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ObjectNode> write(SecurityRequirement model) {
        throw new UnsupportedOperationException();
    }

}
