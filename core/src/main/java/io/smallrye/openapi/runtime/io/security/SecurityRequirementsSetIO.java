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

import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class SecurityRequirementsSetIO<V, A extends V, O extends V, AB, OB>
        extends ModelIO<SecurityRequirement, V, A, O, AB, OB> {

    private final SecurityRequirementIO<V, A, O, AB, OB> securityRequirementIO;

    public SecurityRequirementsSetIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.SECURITY_REQUIREMENTS_SET, Names.create(SecurityRequirement.class));
        securityRequirementIO = new SecurityRequirementIO<>(context);
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
                        .orElseGet(SecurityRequirementImpl::new))
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
    public SecurityRequirement readObject(O node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<O> write(SecurityRequirement model) {
        throw new UnsupportedOperationException();
    }

}
