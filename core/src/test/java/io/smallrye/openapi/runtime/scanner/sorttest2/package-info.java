@OpenAPIDefinition(info = @Info(title = "Test", version = "1.0"), tags = {
        @Tag(name = "DEF"),
        @Tag(name = "XYZ"),
        @Tag(name = "ABC")
}, components = @Components(callbacks = {
        @Callback(name = "KLM"),
        @Callback(name = "GHI"),
        @Callback(name = "123")
}, examples = {
        @ExampleObject(name = "KLM"),
        @ExampleObject(name = "GHI"),
        @ExampleObject(name = "123")
}, headers = {
        @Header(name = "KLM"),
        @Header(name = "GHI"),
        @Header(name = "123")
}, links = {
        @Link(name = "KLM"),
        @Link(name = "GHI"),
        @Link(name = "123")
}, parameters = {
        @Parameter(name = "KLM"),
        @Parameter(name = "GHI"),
        @Parameter(name = "123")
}, requestBodies = {
        @RequestBody(name = "KLM"),
        @RequestBody(name = "GHI"),
        @RequestBody(name = "123")
}, responses = {
        @APIResponse(name = "KLM"),
        @APIResponse(name = "GHI"),
        @APIResponse(name = "123")
}, schemas = {
        @Schema(name = "KLM"),
        @Schema(name = "GHI"),
        @Schema(name = "123")
}, securitySchemes = {
        @SecurityScheme(securitySchemeName = "KLM"),
        @SecurityScheme(securitySchemeName = "GHI"),
        @SecurityScheme(securitySchemeName = "123")
}))
package io.smallrye.openapi.runtime.scanner.sorttest2;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.links.Link;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
