package io.smallrye.openapi.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Reference;
import org.eclipse.microprofile.openapi.models.callbacks.Callback;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.headers.Header;
import org.eclipse.microprofile.openapi.models.links.Link;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import io.smallrye.openapi.model.ReferenceType;

/**
 * An implementation of OASFilter that scans the OpenAPI model and removes
 * any entries in `components` that are not referenced elsewhere in the
 * same model.
 */
public class UnusedComponentFilter implements OASFilter {

    /**
     * Set of component types that should be filtered and removed if not
     * referenced from elsewhere in the OpenAPI model.
     */
    private final Set<ReferenceType> filterTypes;

    /**
     * Map of schemas present in {@code /components/<type>} with a list of the
     * instances that refer to them.
     */
    private final Map<ReferenceType, Map<String, List<Reference<?>>>> references;

    /**
     * By default, unused components of all types will be removed.
     */
    public UnusedComponentFilter() {
        this(Set.of(ReferenceType.values()));
    }

    /**
     * Only remove unused components of the given types.
     */
    public UnusedComponentFilter(Set<ReferenceType> filterTypes) {
        this.filterTypes = filterTypes;
        this.references = new EnumMap<>(ReferenceType.class);
    }

    @Override
    public Callback filterCallback(Callback callback) {
        filterReference(ReferenceType.CALLBACK, callback);
        return callback;
    }

    @Override
    public Header filterHeader(Header header) {
        filterReference(ReferenceType.HEADER, header);
        filterContent(header.getContent());
        filterExamples(header.getExamples());
        return header;
    }

    @Override
    public Link filterLink(Link link) {
        filterReference(ReferenceType.LINK, link);
        return link;
    }

    @Override
    public Parameter filterParameter(Parameter parameter) {
        filterReference(ReferenceType.PARAMETER, parameter);
        filterContent(parameter.getContent());
        filterExamples(parameter.getExamples());
        return parameter;
    }

    @Override
    public PathItem filterPathItem(PathItem pathItem) {
        filterReference(ReferenceType.PATH_ITEM, pathItem);
        return pathItem;
    }

    @Override
    public Operation filterOperation(Operation operation) {
        filterSecurity(operation.getSecurity());
        return operation;
    }

    @Override
    public RequestBody filterRequestBody(RequestBody requestBody) {
        filterReference(ReferenceType.REQUEST_BODY, requestBody);
        filterContent(requestBody.getContent());
        return requestBody;
    }

    @Override
    public APIResponse filterAPIResponse(APIResponse response) {
        filterReference(ReferenceType.RESPONSE, response);
        filterContent(response.getContent());
        return response;
    }

    @Override
    public Schema filterSchema(Schema schema) {
        filterReference(ReferenceType.SCHEMA, schema);
        return schema;
    }

    @Override
    public SecurityScheme filterSecurityScheme(SecurityScheme securityScheme) {
        filterReference(ReferenceType.SECURITY_SCHEME, securityScheme);
        return securityScheme;
    }

    private void filterContent(Content content) {
        if (content != null) {
            filterMediaTypes(content.getMediaTypes());
        }
    }

    private void filterMediaTypes(Map<String, MediaType> mediaTypes) {
        if (mediaTypes != null) {
            mediaTypes.values().forEach(m -> filterExamples(m.getExamples()));
        }
    }

    private void filterExamples(Map<String, Example> examples) {
        if (examples != null) {
            examples.values().forEach(e -> filterReference(ReferenceType.EXAMPLE, e));
        }
    }

    private static class SecuritySchemeReference implements Reference<SecurityScheme> {
        private final SecurityRequirement requirement;
        private final String schemeName;

        private SecuritySchemeReference(SecurityRequirement requirement, String schemeName) {
            this.requirement = requirement;
            this.schemeName = schemeName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SecuritySchemeReference) {
                SecuritySchemeReference other = (SecuritySchemeReference) obj;
                return requirement.equals(other.requirement)
                        && schemeName.equals(other.schemeName);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(requirement, schemeName);
        }

        @Override
        public String getRef() {
            return ReferenceType.SECURITY_SCHEME.referenceOf(schemeName);
        }

        @Override
        public void setRef(String ref) {
            throw new UnsupportedOperationException();
        }
    }

    private void filterSecurity(List<SecurityRequirement> securityRequirements) {
        if (securityRequirements == null) {
            return;
        }

        for (SecurityRequirement requirement : securityRequirements) {
            var schemes = requirement.getSchemes();

            if (schemes != null) {
                for (String schemeName : schemes.keySet()) {
                    filterReference(ReferenceType.SECURITY_SCHEME, new SecuritySchemeReference(requirement, schemeName));
                }
            }
        }
    }

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        filterSecurity(openAPI.getSecurity());

        final Components components = openAPI.getComponents();

        if (components != null) {
            boolean componentsRemoved;

            do {
                componentsRemoved = false;

                for (ReferenceType type : ReferenceType.values()) {
                    Set<String> names = componentNames(components, type);
                    Set<String> unusedNames = unusedNames(type, names);

                    while (!unusedNames.isEmpty()) {
                        unusedNames.forEach(name -> remove(type, components, name));
                        componentsRemoved = true;
                        unusedNames = unusedNames(type, names);
                    }
                }
                /*
                 * As long as the previous loop iteration removed something, we need to
                 * check again for orphaned/unreferenced components.
                 */
            } while (componentsRemoved);
        }
    }

    private void filterReference(ReferenceType type, Reference<?> object) {
        if (!filterTypes.contains(type)) {
            return;
        }

        String name = referencedName(type, object);

        if (name != null) {
            references
                    .computeIfAbsent(type, k -> new HashMap<>())
                    .computeIfAbsent(name, k -> new ArrayList<>())
                    .add(object);
        }
    }

    private Set<String> componentNames(Components components, ReferenceType type) {
        if (filterTypes.contains(type)) {
            Map<String, ?> map = type.get(components);

            if (map != null) {
                return map.keySet();
            }
        }

        return Collections.emptySet();
    }

    private String referencedName(ReferenceType type, Reference<?> reference) {
        final String ref = reference.getRef();

        if (ref != null) {
            String prefix = type.referencePrefix() + '/';

            if (ref.startsWith(prefix)) {
                return ref.substring(prefix.length());
            }
        }

        return null;
    }

    private Set<String> unusedNames(ReferenceType type, Set<String> allNames) {
        Set<String> unused = new HashSet<>(allNames);
        unused.removeIf(references.getOrDefault(type, Collections.emptyMap())::containsKey);
        return unused;
    }

    private void remove(ReferenceType type, Components components, String name) {
        Reference<?> unused = type.get(components).get(name);
        removeReference(type, unused);
        type.remove(components, name);
        UtilLogging.logger.unusedComponentRemoved(unused.getClass().getSimpleName(), type.referencePrefix(), name);
    }

    private void removeReference(ReferenceType type, Reference<?> unused) {
        if (unused == null) {
            return;
        }

        removeReferenceEntry(type, unused);

        switch (type) {
            case RESPONSE:
                removeAPIResponseReferences((APIResponse) unused);
                break;
            case CALLBACK:
                removeCallbackReferences((Callback) unused);
                break;
            case HEADER:
                removeHeaderReferences((Header) unused);
                break;
            case PARAMETER:
                removeParameterReferences((Parameter) unused);
                break;
            case PATH_ITEM:
                removePathItemReferences((PathItem) unused);
                break;
            case REQUEST_BODY:
                removeRequestBodyReferences((RequestBody) unused);
                break;
            case SCHEMA:
                removeSchemaReferences((Schema) unused);
                break;
            default:
                break;
        }
    }

    private void removeReferenceEntry(ReferenceType type, Reference<?> object) {
        String name = referencedName(type, object);

        if (name != null) {
            references
                    .getOrDefault(type, Collections.emptyMap())
                    .computeIfPresent(name, (k, v) -> {
                        v.remove(object);
                        return v.isEmpty() ? null : v;
                    });
        }
    }

    private void removeReferences(ReferenceType type, Map<String, ? extends Reference<?>> items) {
        if (items != null) {
            removeReferences(type, items.values());
        }
    }

    private void removeReferences(ReferenceType type, Collection<? extends Reference<?>> items) {
        if (items != null) {
            for (Reference<?> item : items) {
                removeReference(type, item);
            }
        }
    }

    private void removeAPIResponseReferences(APIResponse response) {
        removeContentReferences(response.getContent());
        removeReferences(ReferenceType.HEADER, response.getHeaders());
        removeReferences(ReferenceType.LINK, response.getLinks());
    }

    private void removeCallbackReferences(Callback callback) {
        removeReferences(ReferenceType.PATH_ITEM, callback.getPathItems());
    }

    private void removeContentReferences(Content content) {
        if (content != null) {
            removeMediaTypeReferences(content.getMediaTypes());
        }
    }

    private void removeMediaTypeReferences(Map<String, MediaType> mediaTypes) {
        if (mediaTypes != null) {
            for (MediaType mediaType : mediaTypes.values()) {
                removeEncodingReferences(mediaType.getEncoding());
                removeReferences(ReferenceType.EXAMPLE, mediaType.getExamples());
                removeReference(ReferenceType.SCHEMA, mediaType.getSchema());
            }
        }
    }

    private void removeEncodingReferences(Map<String, Encoding> encodings) {
        if (encodings != null) {
            for (Encoding encoding : encodings.values()) {
                removeHeaderReferences(encoding.getHeaders());
            }
        }
    }

    private void removeHeaderReferences(Map<String, Header> headers) {
        if (headers != null) {
            for (Header header : headers.values()) {
                removeReference(ReferenceType.HEADER, header);
            }
        }
    }

    private void removeHeaderReferences(Header header) {
        removeContentReferences(header.getContent());
        removeReferences(ReferenceType.EXAMPLE, header.getExamples());
        removeReference(ReferenceType.SCHEMA, header.getSchema());
    }

    private void removeParameterReferences(Parameter parameter) {
        removeContentReferences(parameter.getContent());
        removeReferences(ReferenceType.EXAMPLE, parameter.getExamples());
        removeReference(ReferenceType.SCHEMA, parameter.getSchema());
    }

    private void removePathItemReferences(PathItem pathItem) {
        removeReferences(ReferenceType.PARAMETER, pathItem.getParameters());

        var operations = pathItem.getOperations();

        if (operations != null) {
            for (Operation operation : operations.values()) {
                removeReferences(ReferenceType.CALLBACK, operation.getCallbacks());
                removeReferences(ReferenceType.PARAMETER, operation.getParameters());
                removeReference(ReferenceType.REQUEST_BODY, operation.getRequestBody());

                var responses = operation.getResponses();

                if (responses != null) {
                    removeReferences(ReferenceType.RESPONSE, responses.getAPIResponses());
                }

                removeSecuritySchemeReferences(operation.getSecurity());
            }
        }
    }

    private void removeSecuritySchemeReferences(List<SecurityRequirement> securityRequirements) {
        if (securityRequirements == null) {
            return;
        }

        for (SecurityRequirement requirement : securityRequirements) {
            var schemes = requirement.getSchemes();

            if (schemes != null) {
                for (String schemeName : schemes.keySet()) {
                    removeReference(
                            ReferenceType.SECURITY_SCHEME,
                            new SecuritySchemeReference(requirement, schemeName));
                }
            }
        }
    }

    private void removeRequestBodyReferences(RequestBody requestBody) {
        removeContentReferences(requestBody.getContent());
    }

    private void removeSchemaReferences(Schema schema) {
        removeReference(ReferenceType.SCHEMA, schema.getAdditionalPropertiesSchema());
        removeReferences(ReferenceType.SCHEMA, schema.getAllOf());
        removeReferences(ReferenceType.SCHEMA, schema.getAnyOf());
        removeReferences(ReferenceType.SCHEMA, schema.getOneOf());
        removeReference(ReferenceType.SCHEMA, schema.getItems());
        removeReference(ReferenceType.SCHEMA, schema.getNot());
        removeReferences(ReferenceType.SCHEMA, schema.getProperties());
        // New for OAS 3.1
        removeReference(ReferenceType.SCHEMA, schema.getContains());
        removeReference(ReferenceType.SCHEMA, schema.getContentSchema());
        removeReferences(ReferenceType.SCHEMA, schema.getDependentSchemas());
        removeReference(ReferenceType.SCHEMA, schema.getElseSchema());
        removeReference(ReferenceType.SCHEMA, schema.getIfSchema());
        removeReferences(ReferenceType.SCHEMA, schema.getPatternProperties());
        removeReferences(ReferenceType.SCHEMA, schema.getPrefixItems());
        removeReference(ReferenceType.SCHEMA, schema.getPropertyNames());
        removeReference(ReferenceType.SCHEMA, schema.getThenSchema());
        removeReference(ReferenceType.SCHEMA, schema.getUnevaluatedItems());
        removeReference(ReferenceType.SCHEMA, schema.getUnevaluatedProperties());
    }
}
