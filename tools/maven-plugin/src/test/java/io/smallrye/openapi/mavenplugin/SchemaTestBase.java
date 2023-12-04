package io.smallrye.openapi.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import com.soebes.itf.jupiter.maven.MavenExecutionResult;

import io.smallrye.openapi.runtime.io.OpenApiParser;

public class SchemaTestBase {

    /**
     * Helper method to check that yaml and json schemas match against given test conditions, which are given as consumer.
     *
     * @param result
     * @param schemaConsumer
     */
    protected void testSchema(MavenExecutionResult result, Consumer<OpenAPI> schemaConsumer) throws IOException {
        schemaConsumer.accept(readJson(result));
        schemaConsumer.accept(readYaml(result));
    }

    protected void deleteDirectory(Path directory) {
        try {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private OpenAPI readJson(MavenExecutionResult result) throws IOException {
        File openapiFile = result.getMavenProjectResult()
                .getTargetProjectDirectory()
                .resolve("target/generated/openapi.json")
                .toFile();

        return OpenApiParser.parse(openapiFile.toURI().toURL());
    }

    private OpenAPI readYaml(MavenExecutionResult result) throws IOException {
        File openapiFile = result.getMavenProjectResult()
                .getTargetProjectDirectory()
                .resolve("target/generated/openapi.yaml")
                .toFile();

        return OpenApiParser.parse(openapiFile.toURI().toURL());
    }
}
