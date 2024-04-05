package io.smallrye.openapi.api.util;

import static org.eclipse.microprofile.openapi.OASFactory.createAPIResponse;
import static org.eclipse.microprofile.openapi.OASFactory.createAPIResponses;
import static org.eclipse.microprofile.openapi.OASFactory.createComponents;
import static org.eclipse.microprofile.openapi.OASFactory.createContent;
import static org.eclipse.microprofile.openapi.OASFactory.createMediaType;
import static org.eclipse.microprofile.openapi.OASFactory.createOpenAPI;
import static org.eclipse.microprofile.openapi.OASFactory.createOperation;
import static org.eclipse.microprofile.openapi.OASFactory.createPathItem;
import static org.eclipse.microprofile.openapi.OASFactory.createPaths;
import static org.eclipse.microprofile.openapi.OASFactory.createSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnusedSchemaFilterTest {

    UnusedSchemaFilter target;
    OpenAPI openAPI;

    @BeforeEach
    void setUp() throws Exception {
        target = new UnusedSchemaFilter();

        openAPI = createOpenAPI();
        openAPI.paths(createPaths()
                .addPathItem("/data", createPathItem()
                        .GET(createOperation()
                                .responses(createAPIResponses()
                                        .addAPIResponse("200", createAPIResponse()
                                                .content(createContent()
                                                        .addMediaType("text/plain", createMediaType()
                                                                .schema(createSchema().ref("#/components/schemas/Data")))
                                                        .addMediaType("text/html", createMediaType()
                                                                .schema(createSchema().ref(
                                                                        "http://example.com/schemas/Data?type=html")))))))))
                .components(createComponents()
                        .addSchema("Data", createSchema()
                                .addType(SchemaType.STRING)
                                .description("The data returned by the API")));
    }

    @Test
    void testUnusedSchemaPropertyRemoved() {
        openAPI.getComponents()
                .addSchema("RemovedSchema", createSchema()
                        .addType(SchemaType.OBJECT)
                        .description("Schema to be removed, pass 1")
                        .addProperty("prop1", createSchema()
                                .ref("#/components/schemas/RemovedPropertySchema"))
                        .addProperty("prop2", createSchema()
                                .ref("#/components/schemas/RemovedPropertySchema"))
                        .addProperty("prop3", createSchema()
                                .addAllOf(createSchema()
                                        .ref("#/components/schemas/RemovedPropertySchema"))
                                .addAnyOf(createSchema()
                                        .ref("#/components/schemas/RemovedPropertySchema"))
                                .addOneOf(createSchema()
                                        .ref("#/components/schemas/RemovedPropertySchema"))))
                .addSchema("RemovedPropertySchema", createSchema()
                        .addType(SchemaType.STRING)
                        .description("Schema to be removed, pass 2"));

        assertEquals(3, openAPI.getComponents().getSchemas().size());

        openAPI = FilterUtil.applyFilter(target, openAPI);
        assertEquals(1, openAPI.getComponents().getSchemas().size());
        assertEquals("Data", openAPI.getComponents().getSchemas().keySet().iterator().next());
    }

    @Test
    void testUnusedOneOfSchemasRemoved() {
        openAPI.getComponents()
                .addSchema("RemovedSchema", createSchema()
                        .addType(SchemaType.OBJECT)
                        .description("Schema to be removed, pass 1")
                        .addOneOf(createSchema()
                                .ref("#/components/schemas/RemovedAllOfSchema1"))
                        .addOneOf(createSchema()
                                .ref("#/components/schemas/RemovedAllOfSchema2"))
                        .addOneOf(createSchema()
                                .addType(SchemaType.BOOLEAN)))
                .addSchema("RemovedAllOfSchema1", createSchema()
                        .addType(SchemaType.INTEGER)
                        .description("Schema to be removed, pass 2"))
                .addSchema("RemovedAllOfSchema2", createSchema()
                        .addType(SchemaType.STRING)
                        .description("Schema to be removed, pass 2"));

        assertEquals(4, openAPI.getComponents().getSchemas().size());

        openAPI = FilterUtil.applyFilter(target, openAPI);
        assertEquals(1, openAPI.getComponents().getSchemas().size());
        assertEquals("Data", openAPI.getComponents().getSchemas().keySet().iterator().next());
    }

}
