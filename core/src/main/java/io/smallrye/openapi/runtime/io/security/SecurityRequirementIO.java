package io.smallrye.openapi.runtime.io.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import io.smallrye.openapi.runtime.io.IOContext;
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
        return OASFactory.createSecurityRequirement().addScheme(scheme.getKey(), scheme.getValue());
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
}
