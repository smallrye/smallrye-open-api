package io.smallrye.openapi.mavenplugin;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.servers.Server;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

@MavenJupiterExtension
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
            assertEquals(properties.get("infoContactName"), schema.getInfo().getContact().getName());
            assertEquals(properties.get("infoContactUrl"), schema.getInfo().getContact().getUrl());
            assertEquals(properties.get("infoContactEmail"), schema.getInfo().getContact().getEmail());
            assertEquals(properties.get("infoLicenseName"), schema.getInfo().getLicense().getName());
            assertEquals(properties.get("infoLicenseUrl"),
                    schema.getInfo().getLicense().getUrl());
            assertEquals(properties.get("infoVersion"), schema.getInfo().getVersion());

            List<String> servers = schema.getServers().stream().map(Server::getUrl).collect(Collectors.toList());

            assertTrue(servers.contains(properties.get("server1").toString()));
            assertTrue(servers.contains(properties.get("server2").toString()));
        };

        testSchema(result, schemaConsumer);
    }
}
