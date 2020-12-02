@org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition(info = @org.eclipse.microprofile.openapi.annotations.info.Info(title = "Test", version = "1.0"), components = @org.eclipse.microprofile.openapi.annotations.Components(schemas = {
        @org.eclipse.microprofile.openapi.annotations.media.Schema(name = "CatType", implementation = io.smallrye.openapi.runtime.scanner.StandaloneSchemaScanTest.Cat.class)
}))
package io.smallrye.openapi.runtime.scanner;