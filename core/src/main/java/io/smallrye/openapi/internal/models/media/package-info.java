@OASModelType(name = "Content", constructible = org.eclipse.microprofile.openapi.models.media.Content.class, properties = {
        @OASModelProperty(name = "mediaTypes", unwrapped = true, type = Map.class, valueType = org.eclipse.microprofile.openapi.models.media.MediaType.class),
})
@OASModelType(name = "Discriminator", constructible = org.eclipse.microprofile.openapi.models.media.Discriminator.class, properties = {
        @OASModelProperty(name = "propertyName", type = String.class),
        @OASModelProperty(name = "mapping", singularName = "mapping", type = Map.class, valueType = String.class),
})
@OASModelType(name = "Encoding", constructible = org.eclipse.microprofile.openapi.models.media.Encoding.class, properties = {
        @OASModelProperty(name = "contentType", type = String.class),
        @OASModelProperty(name = "headers", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.headers.Header.class),
        @OASModelProperty(name = "style", type = org.eclipse.microprofile.openapi.models.media.Encoding.Style.class),
        @OASModelProperty(name = "explode", type = Boolean.class),
        @OASModelProperty(name = "allowReserved", type = Boolean.class),
})
@OASModelType(name = "MediaType", constructible = org.eclipse.microprofile.openapi.models.media.MediaType.class, properties = {
        @OASModelProperty(name = "schema", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "example", type = Object.class),
        @OASModelProperty(name = "examples", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.examples.Example.class),
        @OASModelProperty(name = "encoding", singularName = "encoding", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.media.Encoding.class),
})
@OASModelType(name = "AbstractSchema", incomplete = true, constructible = org.eclipse.microprofile.openapi.models.media.Schema.class, properties = {
        @OASModelProperty(name = "additionalProperties", methodNameOverride = "AdditionalPropertiesSchema", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "allOf", singularName = "allOf", type = List.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "anyOf", singularName = "anyOf", type = List.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "$comment", methodNameOverride = "Comment", type = String.class),
        @OASModelProperty(name = "const", methodNameOverride = "ConstValue", type = Object.class),
        @OASModelProperty(name = "contains", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "contentEncoding", type = String.class),
        @OASModelProperty(name = "contentMediaType", type = String.class),
        @OASModelProperty(name = "contentSchema", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "default", methodNameOverride = "DefaultValue", type = Object.class),
        @OASModelProperty(name = "dependentRequired", singularName = "dependentRequired", type = Map.class, valueTypeLiteral = "java.util.List<String>"),
        @OASModelProperty(name = "dependentSchemas", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "deprecated", type = Boolean.class),
        @OASModelProperty(name = "description", type = String.class),
        @OASModelProperty(name = "discriminator", type = org.eclipse.microprofile.openapi.models.media.Discriminator.class),
        @OASModelProperty(name = "else", methodNameOverride = "ElseSchema", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "enum", singularName = "Enumeration", methodNameOverride = "Enumeration", type = List.class, valueType = Object.class),
        @OASModelProperty(name = "example", type = Object.class),
        @OASModelProperty(name = "examples", type = List.class, valueType = Object.class),
        @OASModelProperty(name = "exclusiveMaximum", type = BigDecimal.class),
        @OASModelProperty(name = "exclusiveMinimum", type = BigDecimal.class),
        @OASModelProperty(name = "externalDocs", type = org.eclipse.microprofile.openapi.models.ExternalDocumentation.class),
        @OASModelProperty(name = "format", type = String.class),
        @OASModelProperty(name = "if", methodNameOverride = "IfSchema", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "items", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "maxContains", type = Integer.class),
        @OASModelProperty(name = "maximum", type = BigDecimal.class),
        @OASModelProperty(name = "maxItems", type = Integer.class),
        @OASModelProperty(name = "maxLength", type = Integer.class),
        @OASModelProperty(name = "maxProperties", type = Integer.class),
        @OASModelProperty(name = "minContains", type = Integer.class),
        @OASModelProperty(name = "minimum", type = BigDecimal.class),
        @OASModelProperty(name = "minItems", type = Integer.class),
        @OASModelProperty(name = "minLength", type = Integer.class),
        @OASModelProperty(name = "minProperties", type = Integer.class),
        @OASModelProperty(name = "multipleOf", type = BigDecimal.class),
        @OASModelProperty(name = "not", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "oneOf", singularName = "oneOf", type = List.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "pattern", type = String.class),
        @OASModelProperty(name = "patternProperties", singularName = "patternProperty", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "prefixItems", type = List.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "properties", singularName = "property", type = Map.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "propertyNames", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "readOnly", type = Boolean.class),
        @OASModelProperty(name = "required", singularName = "required", type = List.class, valueType = String.class),
        @OASModelProperty(name = "$schema", methodNameOverride = "SchemaDialect", type = String.class),
        @OASModelProperty(name = "then", methodNameOverride = "ThenSchema", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "title", type = String.class),
        @OASModelProperty(name = "type", singularName = "type", type = List.class, valueType = org.eclipse.microprofile.openapi.models.media.Schema.SchemaType.class),
        @OASModelProperty(name = "unevaluatedItems", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "unevaluatedProperties", type = org.eclipse.microprofile.openapi.models.media.Schema.class),
        @OASModelProperty(name = "uniqueItems", type = Boolean.class),
        @OASModelProperty(name = "writeOnly", type = Boolean.class),
        @OASModelProperty(name = "xml", type = org.eclipse.microprofile.openapi.models.media.XML.class),
})
@OASModelType(name = "XML", constructible = org.eclipse.microprofile.openapi.models.media.XML.class, properties = {
        @OASModelProperty(name = "name", type = String.class),
        @OASModelProperty(name = "namespace", type = String.class),
        @OASModelProperty(name = "prefix", type = String.class),
        @OASModelProperty(name = "attribute", type = Boolean.class),
        @OASModelProperty(name = "wrapped", type = Boolean.class),
})
package io.smallrye.openapi.internal.models.media;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import io.smallrye.openapi.model.OASModelProperty;
import io.smallrye.openapi.model.OASModelType;
