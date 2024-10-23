package io.smallrye.openapi.runtime.scanner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.api.SmallRyeOASConfig;
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
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/HiddenOperationResource.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/VisibleOperationResource.class");

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

    /**
     * Verify that no false positive failures/warnings for duplicate operation-IDs are generated when both
     * {@code javax} and {@code jakarta} annotations are present.
     */
    @Test
    void testMixedJakartaAndJavaxAnnotations() throws IOException {
        // Needed a resource implementation class and an interface to trigger the duplicate operation-ID
        // false positives.
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/mixed/RestResourceImpl.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/mixed/RestInterface.class");
        Index index = indexer.complete();

        Map<String, String> cfg = new HashMap<>();
        // fail hard to trigger the failure in this test
        cfg.put(SmallRyeOASConfig.DUPLICATE_OPERATION_ID_BEHAVIOR, OpenApiConfig.DuplicateOperationIdBehavior.FAIL.toString());
        // method-strategy needed for this test
        cfg.put(SmallRyeOASConfig.OPERATION_ID_STRAGEGY, OpenApiConfig.OperationIdStrategy.METHOD.toString());
        OpenApiConfig config = dynamicConfig(cfg);

        OpenApiAnnotationScanner s = new OpenApiAnnotationScanner(config, index);
        OpenAPI result = Assertions.assertDoesNotThrow(() -> s.scan("JAX-RS"));
        printToConsole(result);
    }

    void testHiddenOperationNotPresent(Index i) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), i);

        OpenAPI result = scanner.scan("JAX-RS");

        printToConsole(result);
        assertJsonEquals("resource.testHiddenOperationNotPresent.json", result);
    }

    @Test
    void testJavaxHiddenOperationPathNotPresent() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/HiddenOperationResource.class");

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

        OpenAPI result = scanner.scan("JAX-RS");

        printToConsole(result);
        assertJsonEquals("resource.testHiddenOperationPathNotPresent.json", result);
    }

    @Test
    void testJavaxRequestBodyComponentGeneration() throws IOException, JSONException {
        Indexer indexer = new Indexer();

        // Test samples
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/RequestBodyTestApplication.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/RequestBodyTestApplication$SomeObject.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/javax/RequestBodyTestApplication$DifferentObject.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/javax/RequestBodyTestApplication$RequestBodyResource.class");

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
        OpenApiConfig config = dynamicConfig(SmallRyeOASConfig.SMALLRYE_CUSTOM_SCHEMA_REGISTRY_CLASS,
                MyCustomSchemaRegistry.class.getName());
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, i);

        OpenAPI result = scanner.scan("JAX-RS");

        printToConsole(result);
        assertJsonEquals("resource.testRequestBodyComponentGeneration.json", result);
    }

    @Test
    void testJavaxPackageInfoDefinitionScanning() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/package-info.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/PackageInfoTestApplication.class");
        index(indexer,
                "test/io/smallrye/openapi/runtime/scanner/resources/javax/PackageInfoTestApplication$PackageInfoTestResource.class");

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

        OpenAPI result = scanner.scan("JAX-RS");

        printToConsole(result);
        assertJsonEquals("resource.testPackageInfoDefinitionScanning.json", result);
    }

    @Test
    void testJavaxTagScanning() throws IOException, JSONException {

        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/TagTestResource2.class");

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
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, String>()), i);
        OpenAPI result = scanner.scan("JAX-RS");
        printToConsole(result);
        assertJsonEquals("resource.tags.multilocation.json", result);
    }

    @Test
    void testJavaxTagScanning_OrderGivenAnnotations() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/TagTestApp.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/TagTestResource2.class");

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
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(new HashMap<String, String>()), i);
        OpenAPI result = scanner.scan("JAX-RS");
        printToConsole(result);
        assertJsonEquals("resource.tags.ordergiven.annotation.json", result);
    }

    @Test
    void testJavaxTagScanning_OrderGivenStaticFile() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/TagTestResource1.class");
        index(indexer, "test/io/smallrye/openapi/runtime/scanner/resources/javax/TagTestResource2.class");

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
        OpenApiConfig config = dynamicConfig(new HashMap<String, String>());
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, i);
        String json = "{\"info\" : {\"title\" : \"Tag order in static file\",\"version\" : \"1.0.0-static\"},\"tags\": [{\"name\":\"tag3\"},{\"name\":\"tag1\"}]}";
        OpenAPI scanResult = scanner.scan("JAX-RS");
        OpenAPI staticResult = OpenApiParser.parse(new ByteArrayInputStream(json.getBytes()), Format.JSON, config);
        OpenApiDocument doc = OpenApiDocument.newInstance();
        doc.config(dynamicConfig(new HashMap<String, String>()));
        doc.modelFromStaticFile(staticResult);
        doc.modelFromAnnotations(scanResult);
        doc.initialize();
        OpenAPI result = doc.get();
        printToConsole(result);
        assertJsonEquals("resource.tags.ordergiven.staticfile.json", result);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            test.io.smallrye.openapi.runtime.scanner.resources.jakarta.EmptySecurityRequirementsResource.class,
            test.io.smallrye.openapi.runtime.scanner.resources.jakarta.EmptySecurityRequirementsResourceMethod.class,
            test.io.smallrye.openapi.runtime.scanner.resources.javax.EmptySecurityRequirementsResource.class,
            test.io.smallrye.openapi.runtime.scanner.resources.javax.EmptySecurityRequirementsResourceMethod.class,
    })
    void testEmptySecurityRequirements(Class<?> resourceClass) throws IOException, JSONException {
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), Index.of(resourceClass));

        OpenAPI result = scanner.scan("JAX-RS");

        printToConsole(result);
        assertJsonEquals("resource.testEmptySecurityRequirements.json", result);
    }

    /**************************************************************************/

    @Test
    void testInterfaceWithoutImplentationExcluded() throws IOException, JSONException {
        Index index = indexOf(MissingImplementation.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan("JAX-RS");

        printToConsole(result);
        assertJsonEquals("default.json", result);
    }

    @ParameterizedTest
    @CsvSource({
            OASConfig.SCAN_CLASSES + ", JaxRsAnnotationScannerTest$MissingImplementation",
            OASConfig.SCAN_CLASSES
                    + ", ^io.smallrye.openapi.runtime.scanner.JaxRsAnnotationScannerTest\\\\$MissingImplementation$",
            OASConfig.SCAN_PACKAGES + ", ^io.smallrye.openapi.runtime.scanner$",
            OASConfig.SCAN_PACKAGES + ", ^io.smallrye.openapi.*$",
    })
    void testInterfaceWithoutImplentationIncluded(String configKey, String configValue) throws IOException, JSONException {
        Index index = indexOf(MissingImplementation.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(dynamicConfig(configKey, configValue), index);

        OpenAPI result = scanner.scan("JAX-RS");

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

        OpenAPI result = scanner.scan("JAX-RS");

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

        OpenAPI result = scanner.scan("JAX-RS");

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
            Schema schema = OASFactory.createSchema();
            schema.addType(Schema.SchemaType.STRING);
            schema.setFormat("uuid");
            schema.setPattern("^[a-f0-9]{8}-?[a-f0-9]{4}-?[1-5][a-f0-9]{3}-?[89ab][a-f0-9]{3}-?[a-f0-9]{12}$");
            schema.setTitle("UUID");
            schema.setDescription("Universally Unique Identifier");
            schema.setExample("de8681db-b4d6-4c47-a428-4b959c1c8e9a");
            schemaRegistry.register(uuidType, Collections.emptySet(), schema);
        }

    }

    /**************************************************************************/

    @Test
    void testIncludeProfile() {
        Index index = indexOf(ProfileResource.class);
        OpenApiConfig config = dynamicConfig(SmallRyeOASConfig.SCAN_PROFILES,
                "external");

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);

        OpenAPI result = scanner.scan("JAX-RS");

        Assertions.assertEquals(1, result.getPaths().getPathItems().size());
        Assertions.assertTrue(result.getPaths().getPathItems().containsKey("/profile/{id}"));
    }

    @Test
    void testExcludeProfile() {
        Index index = indexOf(ProfileResource.class);
        OpenApiConfig config = dynamicConfig(SmallRyeOASConfig.SCAN_EXCLUDE_PROFILES,
                "external");

        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, index);

        OpenAPI result = scanner.scan("JAX-RS");

        Assertions.assertEquals(1, result.getPaths().getPathItems().size());
        Assertions.assertTrue(result.getPaths().getPathItems().containsKey("/profile"));
    }

    @Path("/profile")
    static class ProfileResource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String read() {
            return "";
        }

        @Path("{id}")
        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Extension(name = "x-smallrye-profile-external", value = "")
        public String create(@PathParam("id") Long id) {
            return "";
        }
    }

    /**************************************************************************/

    @Test
    void testCsvProducesConsumes() throws IOException, JSONException {
        assertJsonEquals("resource.testCsvConsumesProduces.json",
                test.io.smallrye.openapi.runtime.scanner.javax.MultiProduceConsumeResource.class);
    }

    @Test
    void testCsvProducesConsumesJakarta() throws IOException, JSONException {
        assertJsonEquals("resource.testCsvConsumesProduces.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.MultiProduceConsumeResource.class);
    }

}
