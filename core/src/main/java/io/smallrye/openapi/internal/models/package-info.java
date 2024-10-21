@OASModelType(name = "Components", constructible = org.eclipse.microprofile.openapi.models.Components.class, properties = {
        @OASModelProperty(name = "schemas", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "responses", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.responses.APIResponse.class),
        @OASModelProperty(name = "parameters", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.parameters.Parameter.class),
        @OASModelProperty(name = "examples", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.examples.Example.class),
        @OASModelProperty(name = "requestBodies", singularName = "requestBody", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.parameters.RequestBody.class),
        @OASModelProperty(name = "headers", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.headers.Header.class),
        @OASModelProperty(name = "securitySchemes", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.security.SecurityScheme.class),
        @OASModelProperty(name = "links", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.links.Link.class),
        @OASModelProperty(name = "callbacks", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.callbacks.Callback.class),
        @OASModelProperty(name = "pathItems", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.PathItem.class),
})
@OASModelType(name = "ExternalDocumentation", constructible = org.eclipse.microprofile.openapi.models.ExternalDocumentation.class, properties = {
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "url", type = String.class),
})
@OASModelType(name = "AbstractOpenAPI", incomplete = true, constructible = org.eclipse.microprofile.openapi.models.OpenAPI.class, properties = {
        @OASModelProperty(name = "openapi", type = String.class),
        @OASModelProperty(name = "info", type = org.eclipse.microprofile.openapi.models.info.Info.class),
        @OASModelProperty(name = "externalDocs", type = org.eclipse.microprofile.openapi.models.ExternalDocumentation.class),
        @OASModelProperty(name = "servers", type = List.class, valueType = org.eclipse.microprofile.openapi.models.servers.Server.class),
        @OASModelProperty(name = "security", singularName = "securityRequirement", type = List.class, valueType = org.eclipse.microprofile.openapi.models.security.SecurityRequirement.class),
        @OASModelProperty(name = "tags", type = List.class, valueType = org.eclipse.microprofile.openapi.models.tags.Tag.class),
        @OASModelProperty(name = "paths", type = org.eclipse.microprofile.openapi.models.Paths.class),
        @OASModelProperty(name = "webhooks", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.PathItem.class),
        @OASModelProperty(name = "components", type = org.eclipse.microprofile.openapi.models.Components.class),
})
@OASModelType(name = "Operation", constructible = org.eclipse.microprofile.openapi.models.Operation.class, properties = {
        @OASModelProperty(name = "tags", type = List.class, valueType = String.class),
        @OASModelProperty(name = "summary", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "externalDocs", type = org.eclipse.microprofile.openapi.models.ExternalDocumentation.class),
        @OASModelProperty(name = "operationId", type = String.class),
        @OASModelProperty(name = "parameters", type = List.class, valueType = org.eclipse.microprofile.openapi.models.parameters.Parameter.class),
        @OASModelProperty(name = "requestBody", type = org.eclipse.microprofile.openapi.models.parameters.RequestBody.class),
        @OASModelProperty(name = "responses", type = org.eclipse.microprofile.openapi.models.responses.APIResponses.class),
        @OASModelProperty(name = "callbacks", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.callbacks.Callback.class),
        @OASModelProperty(name = "deprecated", type = Boolean.class),
        @OASModelProperty(name = "servers", type = List.class, valueType = org.eclipse.microprofile.openapi.models.servers.Server.class),
        @OASModelProperty(name = "security", singularName = "securityRequirement", type = List.class, valueType = org.eclipse.microprofile.openapi.models.security.SecurityRequirement.class),
})
@OASModelType(name = "AbstractPathItem", incomplete = true, constructible = org.eclipse.microprofile.openapi.models.PathItem.class, properties = {
        @OASModelProperty(name = "ref", type = String.class),
        @OASModelProperty(name = "summary", type = String.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "get", methodNameOverride = "GET", type = org.eclipse.microprofile.openapi.models.Operation.class),
        @OASModelProperty(name = "put", methodNameOverride = "PUT", type = org.eclipse.microprofile.openapi.models.Operation.class),
        @OASModelProperty(name = "post", methodNameOverride = "POST", type = org.eclipse.microprofile.openapi.models.Operation.class),
        @OASModelProperty(name = "delete", methodNameOverride = "DELETE", type = org.eclipse.microprofile.openapi.models.Operation.class),
        @OASModelProperty(name = "options", methodNameOverride = "OPTIONS", type = org.eclipse.microprofile.openapi.models.Operation.class),
        @OASModelProperty(name = "head", methodNameOverride = "HEAD", type = org.eclipse.microprofile.openapi.models.Operation.class),
        @OASModelProperty(name = "patch", methodNameOverride = "PATCH", type = org.eclipse.microprofile.openapi.models.Operation.class),
        @OASModelProperty(name = "trace", methodNameOverride = "TRACE", type = org.eclipse.microprofile.openapi.models.Operation.class),
        @OASModelProperty(name = "parameters", type = List.class, valueType = org.eclipse.microprofile.openapi.models.parameters.Parameter.class),
        @OASModelProperty(name = "servers", type = List.class, valueType = org.eclipse.microprofile.openapi.models.servers.Server.class),
})
@OASModelType(name = "Paths", constructible = org.eclipse.microprofile.openapi.models.Paths.class, properties = {
        @OASModelProperty(name = "PathItems", unwrapped = true, type = Map.class, valueType = org.eclipse.microprofile.openapi.models.PathItem.class),
})
package io.smallrye.openapi.internal.models;

import java.util.List;
import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
