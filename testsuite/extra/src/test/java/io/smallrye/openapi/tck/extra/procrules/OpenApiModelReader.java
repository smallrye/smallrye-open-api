package io.smallrye.openapi.tck.extra.procrules;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.OpenAPI;

/**
 * Generates a base model to be used by the OpenAPI.
 */
public class OpenApiModelReader implements OASModelReader {
    /**
     * Creates a new {@link ExternalDocumentation} instance, modifying the output OpenAPI document.
     *
     * @return A new {@link OpenAPI} instance that will serve as base for generation
     */
    @Override
    public OpenAPI buildModel() {
        OpenAPI api = OASFactory.createObject(OpenAPI.class);
        ExternalDocumentation externalDocumentation = OASFactory.createExternalDocumentation();
        externalDocumentation.setUrl("http://oas-model-reader-based-external-docs.org");
        externalDocumentation.setDescription("Could be overridden by static files and/or annotations");
        api.setExternalDocs(externalDocumentation);
        return api;
    }
}
