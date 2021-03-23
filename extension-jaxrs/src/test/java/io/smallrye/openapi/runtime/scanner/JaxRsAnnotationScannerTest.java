package io.smallrye.openapi.runtime.scanner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiParser;

/**
 * @author eric.wittmann@gmail.com
 */
class JaxRsAnnotationScannerTest extends JaxRsDataObjectScannerTestBase {

    @Test
    void testJavaxHiddenOperationNotPresent() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/HiddenOperationResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/VisibleOperationResource.class");

        testHiddenOperationNotPresent(indexer.complete());
    }

    @Test
    void testJakartaHiddenOperationNotPresent() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/HiddenOperationResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/VisibleOperationResource.class");

        testHiddenOperationNotPresent(indexer.complete());
    }

    void testHiddenOperationNotPresent(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testHiddenOperationNotPresent.json", result);
    }

    @Test
    void testJavaxHiddenOperationPathNotPresent() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/HiddenOperationResource.class");

        testHiddenOperationPathNotPresent(indexer.complete());
    }

    @Test
    void testJakartaHiddenOperationPathNotPresent() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/HiddenOperationResource.class");

        testHiddenOperationPathNotPresent(indexer.complete());
    }

    void testHiddenOperationPathNotPresent(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testHiddenOperationPathNotPresent.json", result);
    }

    @Test
    void testJavaxRequestBodyComponentGeneration() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/RequestBodyTestApplication.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/RequestBodyTestApplication$SomeObject.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/RequestBodyTestApplication$DifferentObject.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/RequestBodyTestApplication$RequestBodyResource.class");

        testRequestBodyComponentGeneration(indexer.complete());
    }

    @Test
    void testJakartaRequestBodyComponentGeneration() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/RequestBodyTestApplication.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/RequestBodyTestApplication$SomeObject.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/RequestBodyTestApplication$DifferentObject.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/RequestBodyTestApplication$RequestBodyResource.class");

        testRequestBodyComponentGeneration(indexer.complete());
    }

    void testRequestBodyComponentGeneration(Index i) throws IOException, JSONException {
        OpenApiConfig config = dynamicConfig(OpenApiConstants.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS,
                MyCustomSchemaRegistry.class.getName());
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testRequestBodyComponentGeneration.json", result);
    }

    @Test
    void testJavaxPackageInfoDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/package-info.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/PackageInfoTestApplication.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/PackageInfoTestApplication$PackageInfoTestResource.class");

        testPackageInfoDefinitionScanning(indexer.complete());
    }

    @Test
    void testJakartaPackageInfoDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/package-info.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/PackageInfoTestApplication.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/PackageInfoTestApplication$PackageInfoTestResource.class");

        testPackageInfoDefinitionScanning(indexer.complete());
    }

    void testPackageInfoDefinitionScanning(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testPackageInfoDefinitionScanning.json", result);
    }

    @Test
    void testJavaxTagScanning() throws IOException, JSONException {

        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/TagTestResource2.class");

        testTagScanning(indexer.complete());
    }

    @Test
    void testJakartaTagScanning() throws IOException, JSONException {

        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/TagTestResource2.class");

        testTagScanning(indexer.complete());
    }

    void testTagScanning(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, Object>()), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.tags.multilocation.json", result);
    }

    @Test
    void testJavaxTagScanning_OrderGivenAnnotations() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/TagTestApp.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/TagTestResource2.class");

        testTagScanning_OrderGivenAnnotations(indexer.complete());
    }

    @Test
    void testJakartaTagScanning_OrderGivenAnnotations() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/TagTestApp.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/TagTestResource2.class");

        testTagScanning_OrderGivenAnnotations(indexer.complete());
    }

    void testTagScanning_OrderGivenAnnotations(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, Object>()), i);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.tags.ordergiven.annotation.json", result);
    }

    @Test
    void testJavaxTagScanning_OrderGivenStaticFile() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/TagTestResource2.class");

        testTagScanning_OrderGivenStaticFile(indexer.complete());
    }

    @Test
    void testJakartaTagScanning_OrderGivenStaticFile() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/TagTestResource2.class");

        testTagScanning_OrderGivenStaticFile(indexer.complete());
    }

    void testTagScanning_OrderGivenStaticFile(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, Object>()), i);
        OpenAPI scanResult = scanner.scan();
        OpenAPI staticResult = OpenApiParser.parse(new ByteArrayInputStream(
                "{\"info\" : {\"title\" : \"Tag order in static file\",\"version\" : \"1.0.0-static\"},\"tags\": [{\"name\":\"tag3\"},{\"name\":\"tag1\"}]}"
                        .getBytes()),
                Format.JSON);
        OpenApiDocument doc = OpenApiDocument.INSTANCE;
        doc.config(dynamicConfig(new HashMap<String, Object>()));
        doc.modelFromStaticFile(staticResult);
        doc.modelFromAnnotations(scanResult);
        doc.initialize();
        OpenAPI result = doc.get();
        printToConsole(result);
        assertJsonEquals("resource.tags.ordergiven.staticfile.json", result);
        doc.reset();
    }

    @Test
    void testJavaxEmptySecurityRequirements() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/EmptySecurityRequirementsResource.class");

        testEmptySecurityRequirements(indexer.complete());
    }

    @Test
    void testJakartaEmptySecurityRequirements() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/jakarta/EmptySecurityRequirementsResource.class");

        testEmptySecurityRequirements(indexer.complete());
    }

    void testEmptySecurityRequirements(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.testEmptySecurityRequirements.json", result);
    }

    /**************************************************************************/

    @Test
    void testInterfaceWithoutImplentationExcluded() throws IOException, JSONException {
        Index index = indexOf(MissingImplementation.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("default.json", result);
    }

    @ParameterizedTest
    @CsvSource({
            OASConfig.SCAN_CLASSES + ", JaxRsAnnotationScannerTest$MissingImplementation",
            OASConfig.SCAN_CLASSES
                    + ", ^io.smallrye.openapi.runtime.scanner.JaxRsAnnotationScannerTest\\$MissingImplementation$",
            OASConfig.SCAN_PACKAGES + ", ^io.smallrye.openapi.runtime.scanner$",
            OASConfig.SCAN_PACKAGES + ", ^io.smallrye.openapi.*$",
    })
    void testInterfaceWithoutImplentationIncluded(String configKey, String configValue) throws IOException, JSONException {
        Index index = indexOf(MissingImplementation.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(configKey, configValue), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.interface-only.json", result);
    }

    @Path("/noimpl")
    interface MissingImplementation {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        String getNoImpl();
    }

    /**************************************************************************/

    @Test
    void testInterfaceWithConcreteImplentation() throws IOException, JSONException {
        Index index = indexOf(HasConcreteImplementation.class, ImplementsHasConcreteImplementation.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("resource.concrete-implementation.json", result);
    }

    @Path("/concrete")
    interface HasConcreteImplementation {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        String getConcrete();
    }

    static class ImplementsHasConcreteImplementation implements HasConcreteImplementation {
        @Override
        public String getConcrete() {
            return "";
        }
    }

    /**************************************************************************/

    @Test
    void testInterfaceWithAbstractImplentation() throws IOException, JSONException {
        Index index = indexOf(HasAbstractImplementation.class, ImplementsHasAbstractImplementation.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("default.json", result);
    }

    @Path("/abstract")
    interface HasAbstractImplementation {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        String getAbstract();
    }

    static abstract class ImplementsHasAbstractImplementation implements HasAbstractImplementation {
        @Override
        public String getAbstract() {
            return "";
        }
    }

    /**
     * Example of a simple custom schema registry that has only UUID type schema.
     */
    static class MyCustomSchemaRegistry implements CustomSchemaRegistry {

        @Override
        public void registerCustomSchemas(SchemaRegistry schemaRegistry) {
            Type uuidType = Type.create(componentize(UUID.class.getName()), Kind.CLASS);
            Schema schema = new SchemaImpl();
            schema.setType(Schema.SchemaType.STRING);
            schema.setFormat("uuid");
            schema.setPattern("^[a-f0-9]{8}-?[a-f0-9]{4}-?[1-5][a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}$");
            schema.setTitle("UUID");
            schema.setDescription("Universally Unique Identifier");
            schema.setExample("de8681db-b4d6-4c47-a428-4b959c1c8e9a");
            schemaRegistry.register(uuidType, schema);
        }

    }
}
