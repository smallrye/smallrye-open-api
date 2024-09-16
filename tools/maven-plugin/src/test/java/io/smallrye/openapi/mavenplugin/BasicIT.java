package io.smallrye.openapi.mavenplugin;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.junit.jupiter.api.Assertions;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

@MavenJupiterExtension
@MavenGoal("compile")
@MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:generate-schema")
public class BasicIT extends SchemaTestBase {
    @MavenTest
    void basic_info(MavenExecutionResult result) throws IOException {

        assertThat(result).isSuccessful();

        // Test which checks that the configured info parameters match the resulting schema info texts
        Properties properties = result.getMavenProjectResult().getModel().getProperties();
        Consumer<OpenAPI> schemaConsumer = (schema) -> {
            assertEquals(properties.get("openApiVersion"), schema.getOpenapi());
            assertEquals(properties.get("infoTitle"), schema.getInfo().getTitle());
            assertEquals(properties.get("infoDescription"), schema.getInfo().getDescription());
            assertEquals(properties.get("infoTermsOfService"), schema.getInfo().getTermsOfService());
            assertEquals(properties.get("infoSummary"), schema.getInfo().getSummary());
            assertEquals(properties.get("infoContactName"), schema.getInfo().getContact().getName());
            assertEquals(properties.get("infoContactUrl"), schema.getInfo().getContact().getUrl());
            assertEquals(properties.get("infoContactEmail"), schema.getInfo().getContact().getEmail());
            assertEquals(properties.get("infoLicenseName"), schema.getInfo().getLicense().getName());
            assertEquals(properties.get("infoLicenseIdentifier"),
                    schema.getInfo().getLicense().getIdentifier());
            //The URL is not tested here due to being exclusive with Identifier
            assertEquals(properties.get("infoVersion"), schema.getInfo().getVersion());

            List<String> servers = schema.getServers().stream().map(Server::getUrl).collect(Collectors.toList());

            assertTrue(servers.contains(properties.get("server1").toString()));
            assertTrue(servers.contains(properties.get("server2").toString()));

            assertTrue(schema.getPaths().hasPathItem("/hello"));
        };

        testSchema(result, schemaConsumer);
    }

    @MavenTest
    void outputFileTypeFilter_All(MavenExecutionResult result) throws IOException {

        assertThat(result).isSuccessful();

        // Test which checks that the output file type is as expected
        Assertions.assertEquals(1, filterGeneratedFileTypes(".yaml").size());
        Assertions.assertEquals(1, filterGeneratedFileTypes(".json").size());
        // clean the outputFileTypeFilter generation path for other tests to come
        deleteDirectory(OUTPUT_FILE_TYPE_FILTER_GENERATION_PATH);
    }

    @MavenTest
    void outputFileTypeFilter_Yaml(MavenExecutionResult result) throws IOException {

        assertThat(result).isSuccessful();

        // Test which checks that the output file type is as expected
        Assertions.assertEquals(1, filterGeneratedFileTypes(".yaml").size());
        Assertions.assertEquals(0, filterGeneratedFileTypes(".json").size());
        // clean the outputFileTypeFilter generation path for other tests to come
        deleteDirectory(OUTPUT_FILE_TYPE_FILTER_GENERATION_PATH);
    }

    @MavenTest
    void outputFileTypeFilter_Json(MavenExecutionResult result) throws IOException {

        assertThat(result).isSuccessful();

        // Test which checks that the output file type is as expected
        Assertions.assertEquals(0, filterGeneratedFileTypes(".yaml").size());
        Assertions.assertEquals(1, filterGeneratedFileTypes(".json").size());
        // clean the outputFileTypeFilter generation path for other tests to come
        deleteDirectory(OUTPUT_FILE_TYPE_FILTER_GENERATION_PATH);
    }

    private static final Path OUTPUT_FILE_TYPE_FILTER_GENERATION_PATH = Paths.get(System.getProperty("java.io.tmpdir"),
            "smallrye-openapi", "maven-plugin", "it");

    private static List<String> filterGeneratedFileTypes(final String fileExtension)
            throws IOException {

        if (!Files.isDirectory(OUTPUT_FILE_TYPE_FILTER_GENERATION_PATH)) {
            throw new IllegalArgumentException("The generated file path does not exist, or is not a directory");
        }

        try (Stream<Path> walk = Files.walk(OUTPUT_FILE_TYPE_FILTER_GENERATION_PATH)) {
            return walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
}
