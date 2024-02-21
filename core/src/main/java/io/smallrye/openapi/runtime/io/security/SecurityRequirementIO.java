package io.smallrye.openapi.runtime.io.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.openapi.api.models.security.SecurityRequirementImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.ObjectWriter;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

public class SecurityRequirementIO extends ModelIO<SecurityRequirement> {

    private static final String PROP_NAME = "name";
    private static final String PROP_SCOPES = "scopes";

    public SecurityRequirementIO(AnnotationScannerContext context) {
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

    public List<SecurityRequirement> readList(JsonNode node) {
        return Optional.ofNullable(node)
                .filter(JsonNode::isArray)
                .map(ArrayNode.class::cast)
                .map(ArrayNode::elements)
                .map(elements -> Spliterators.spliteratorUnknownSize(elements, Spliterator.ORDERED))
                .map(elements -> StreamSupport.stream(elements, false))
                .map(elements -> {
                    IoLogging.logger.jsonArray("SecurityRequirement");
                    return elements.filter(JsonNode::isObject)
                            .map(ObjectNode.class::cast)
                            .map(this::read)
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .orElse(null);
    }

    @Override
    public SecurityRequirement read(ObjectNode node) {
        SecurityRequirement requirement = new SecurityRequirementImpl();
        for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
            String fieldName = fieldNames.next();
            JsonNode scopesNode = node.get(fieldName);
            Optional<List<String>> maybeScopes = JsonUtil.readStringArray(scopesNode);
            if (maybeScopes.isPresent()) {
                requirement.addScheme(fieldName, maybeScopes.get());
            } else {
                requirement.addScheme(fieldName);
            }
        }
        return requirement;
    }

    public Optional<ArrayNode> write(List<SecurityRequirement> models) {
        return optionalJsonArray(models).map(array -> {
            for (SecurityRequirement model : models) {
                write(model).ifPresent(array::add);
            }
            return array;
        });
    }

    @Override
    public Optional<ObjectNode> write(SecurityRequirement model) {
        return optionalJsonObject(model.getSchemes()).map(node -> {
            model.getSchemes().forEach((key, value) -> ObjectWriter.writeStringArray(node, value, key));
            return node;
        });
    }
}
