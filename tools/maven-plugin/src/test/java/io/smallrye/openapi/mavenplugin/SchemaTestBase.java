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

import io.smallrye.openapi.api.SmallRyeOpenAPI;

public class SchemaTestBase {

    /**
     * Helper method to check that yaml and json schemas match against given test conditions, which are given as consumer.
     *
     * @param result
     * @param schemaConsumer
     */
    protected void testSchema(MavenExecutionResult result, Consumer<OpenAPI> schemaConsumer) throws IOException {
        schemaConsumer.accept(read(result, "target/generated/openapi.json"));
        schemaConsumer.accept(read(result, "target/generated/openapi.yaml"));
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

    private OpenAPI read(MavenExecutionResult result, String path) throws IOException {
        Path openapiFile = result.getMavenProjectResult()
                .getTargetProjectDirectory()
                .resolve(path);

        return SmallRyeOpenAPI.builder()
                .enableAnnotationScan(false)
                .enableModelReader(false)
                .enableStandardFilter(false)
                .enableStandardStaticFiles(false)
                .withCustomStaticFile(Files.newInputStream(openapiFile))
                .build()
                .model();
    }
}
