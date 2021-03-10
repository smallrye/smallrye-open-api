@OpenAPIDefinition(info = @Info(title = "Test", version = "1.0"), tags = {}, components = @Components(callbacks = {
        @Callback(name = "DEF"),
        @Callback(name = "XYZ"),
        @Callback(name = "ABC")
}, examples = {
        @ExampleObject(name = "DEF"),
        @ExampleObject(name = "XYZ"),
        @ExampleObject(name = "ABC")
}, headers = {
        @Header(name = "DEF"),
        @Header(name = "XYZ"),
        @Header(name = "ABC")
}, links = {
        @Link(name = "DEF"),
        @Link(name = "XYZ"),
        @Link(name = "ABC")
}, parameters = {
        @Parameter(name = "DEF"),
        @Parameter(name = "XYZ"),
        @Parameter(name = "ABC")
}, requestBodies = {
        @RequestBody(name = "DEF"),
        @RequestBody(name = "XYZ"),
        @RequestBody(name = "ABC")
}, responses = {
        @APIResponse(name = "DEF"),
        @APIResponse(name = "XYZ"),
        @APIResponse(name = "ABC")
}, schemas = {
        @Schema(name = "DEF"),
        @Schema(name = "XYZ"),
        @Schema(name = "ABC")
}, securitySchemes = {
        @SecurityScheme(securitySchemeName = "DEF"),
        @SecurityScheme(securitySchemeName = "XYZ"),
        @SecurityScheme(securitySchemeName = "ABC")
}))
package io.smallrye.openapi.runtime.scanner.sorttest1;

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
