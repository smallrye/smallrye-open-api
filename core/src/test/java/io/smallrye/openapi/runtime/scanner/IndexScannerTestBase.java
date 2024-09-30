package io.smallrye.openapi.runtime.scanner;

import static io.smallrye.openapi.api.constants.OpenApiConstants.DUPLICATE_OPERATION_ID_BEHAVIOR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.skyscreamer.jsonassert.JSONAssert;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.SmallRyeOpenAPI;
import io.smallrye.openapi.model.Extensions;

public class IndexScannerTestBase {

    private static final Logger LOG = Logger.getLogger(IndexScannerTestBase.class);
    static final Pattern PATTERN_CLASS_DOTNAME_COMPONENTIZE = Pattern.compile("([\\.$]|$)");

    protected static String pathOf(Class<?> clazz) {
        return clazz.getName().replace('.', '/').concat(".class");
    }

    protected static void indexDirectory(Indexer indexer, String baseDir) {
        InputStream directoryStream = tcclGetResourceAsStream(baseDir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(directoryStream));
        reader.lines()
                .filter(resName -> resName.endsWith(".class"))
                .map(resName -> Paths.get(baseDir, resName)) // e.g. test/io/smallrye/openapi/runtime/scanner/entities/ + Bar.class
                .forEach(path -> index(indexer, path.toString()));
    }

    private static InputStream tcclGetResourceAsStream(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }

    public static Index indexOf(String... classNames) {
        Indexer indexer = new Indexer();

        for (String name : classNames) {
            index(indexer, name.replace('.', '/').concat(".class"));
        }

        return indexer.complete();
    }

    public static Index indexOf(Class<?>... classes) {
        Indexer indexer = new Indexer();

        for (Class<?> klazz : classes) {
            index(indexer, pathOf(klazz));
        }

        return indexer.complete();
    }

    protected static void index(Indexer indexer, String resName) {
        try {
            InputStream stream = tcclGetResourceAsStream(resName);
            indexer.index(stream);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    protected static DotName componentize(String className) {
        Matcher matcher = PATTERN_CLASS_DOTNAME_COMPONENTIZE.matcher(className);
        String previousDelimiter = null;
        int previousEnd = 0;
        DotName name = null;

        while (matcher.find()) {
            String localName = className.substring(previousEnd, matcher.start());
            boolean innerClass = "$".equals(previousDelimiter);
            name = DotName.createComponentized(name, localName, innerClass);

            previousDelimiter = matcher.group();
            previousEnd = matcher.end();
        }

        return name;
    }

    public static void printToConsole(String entityName, Schema schema) {
        // Remember to set debug level logging.
        LOG.debug(schemaToString(entityName, schema));
    }

    public static void printToConsole(OpenAPI oai) {
        // Remember to set debug level logging.
        LOG.debug(toJSON(oai));
    }

    public static String toJSON(OpenAPI oai) {
        return SmallRyeOpenAPI.builder()
                .withConfig(config(Collections.emptyMap()))
                .withInitialModel(oai)
                .defaultRequiredProperties(false)
                .enableModelReader(false)
                .enableStandardStaticFiles(false)
                .enableAnnotationScan(false)
                .enableStandardFilter(false)
                .build()
                .toJSON();
    }

    public static void verifyMethodAndParamRefsPresent(OpenAPI oai) {
        if (oai.getPaths() != null && oai.getPaths().getPathItems() != null) {
            for (Map.Entry<String, PathItem> pathItemEntry : oai.getPaths().getPathItems().entrySet()) {
                final PathItem pathItem = pathItemEntry.getValue();
                if (pathItem.getOperations() != null) {
                    for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : pathItem.getOperations().entrySet()) {
                        final Operation operation = operationEntry.getValue();
                        String opRef = operationEntry.getKey() + " " + pathItemEntry.getKey();
                        Assertions.assertNotNull(Extensions.getMethodRef(operation), "methodRef: " + opRef);
                        if (operation.getParameters() != null) {
                            for (Parameter parameter : operation.getParameters()) {
                                /*
                                 * if @Parameter style=matrix was not specified at the same @Path segment
                                 * a synthetic parameter is created which cannot be mapped to a field or method parameter
                                 */
                                if (!isPathMatrixObject(parameter)) {
                                    // in all other cases paramRef should be set
                                    String pRef = opRef + ", " + parameter.getIn() + ": " + parameter.getName();
                                    Assertions.assertNotNull(Extensions.getParamRef(parameter), "paramRef: " + pRef);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isPathMatrixObject(Parameter parameter) {
        return parameter.getIn() == Parameter.In.PATH && parameter.getStyle() == Parameter.Style.MATRIX
                && parameter.getSchema() != null && parameter.getSchema().getType() != null
                && parameter.getSchema().getType().equals(Collections.singletonList(Schema.SchemaType.OBJECT));
    }

    public static String schemaToString(String entityName, Schema schema) {
        return toJSON(OASFactory.createOpenAPI()
                .components(OASFactory.createComponents()
                        .addSchema(entityName, schema)));
    }

    public static void assertJsonEquals(String entityName, String expectedResource, Schema actual)
            throws JSONException, IOException {
        URL resourceUrl = IndexScannerTestBase.class.getResource(expectedResource);
        JSONAssert.assertEquals(loadResource(resourceUrl), schemaToString(entityName, actual), true);
    }

    public static void assertJsonEquals(String expectedResource, OpenAPI actual) throws JSONException, IOException {
        URL resourceUrl = IndexScannerTestBase.class.getResource(expectedResource);
        assertJsonEquals(resourceUrl, actual);
    }

    public static void assertJsonEquals(URL expectedResourceUrl, OpenAPI actual) throws JSONException, IOException {
        String json = toJSON(actual);
        try {
            JSONAssert.assertEquals(loadResource(expectedResourceUrl), json, true);
        } catch (AssertionError e) {
            // If the JSON did not match, we want to add the serialized version to the end
            throw new AssertionError(e.getMessage() + "\nFull result:\n" + json, e);
        }
    }

    public static OpenAPI scan(Class<?>... classes) {
        return scan(config(Collections.emptyMap()), null, classes);
    }

    public static OpenAPI scan(InputStream customStaticFile, Class<?>... classes) {
        return scan(config(Collections.emptyMap()), customStaticFile, classes);
    }

    public static OpenAPI scan(Config config, InputStream customStaticFile, Class<?>... classes) {
        return scan(config, false, customStaticFile, classes);
    }

    public static OpenAPI scan(Config config, Class<?>... classes) {
        return scan(config, false, null, classes);
    }

    public static OpenAPI scan(Config config, boolean defaultRequiredProperties, InputStream customStaticFile,
            Class<?>... classes) {
        Index index = indexOf(classes);

        OpenAPI result = SmallRyeOpenAPI.builder()
                .defaultRequiredProperties(defaultRequiredProperties)
                .withCustomStaticFile(() -> customStaticFile)
                .withIndex(index)
                .withConfig(config)
                .build()
                .model();

        printToConsole(result);
        return result;
    }

    public static void assertJsonEquals(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        assertJsonEquals(expectedResource, scan(config(Collections.emptyMap()), null, classes));
    }

    public static String loadResource(URL testResource) throws IOException {
        final char[] buffer = new char[8192];
        final StringBuilder result = new StringBuilder();

        try (Reader reader = new InputStreamReader(testResource.openStream(), StandardCharsets.UTF_8)) {
            int count;
            while ((count = reader.read(buffer, 0, buffer.length)) > 0) {
                result.append(buffer, 0, count);
            }
        }

        return result.toString();
    }

    public static OpenApiConfig emptyConfig() {
        return dynamicConfig(Collections.emptyMap());
    }

    public static OpenApiConfig dynamicConfig(String key, Object value) {
        Map<String, String> config = new HashMap<>(1);
        config.put(key, value.toString());
        return dynamicConfig(config);
    }

    public static OpenApiConfig failOnDuplicateOperationIdsConfig() {
        return dynamicConfig(DUPLICATE_OPERATION_ID_BEHAVIOR, OpenApiConfig.DuplicateOperationIdBehavior.FAIL.name());
    }

    public static OpenApiConfig dynamicConfig(Map<String, String> properties) {
        return OpenApiConfig.fromConfig(config(properties));
    }

    public static Config config(String key, Object value) {
        Map<String, String> config = new HashMap<>(1);
        config.put(key, value.toString());
        return config(config);
    }

    public static Config config(Map<String, String> properties) {
        return new SmallRyeConfigBuilder()
                .addDefaultSources()
                .withSources(new PropertiesConfigSource(properties, "unit-test", ConfigSource.DEFAULT_ORDINAL))
                .build();
    }
}
