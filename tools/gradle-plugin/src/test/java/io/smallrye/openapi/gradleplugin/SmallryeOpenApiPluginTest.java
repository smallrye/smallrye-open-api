package io.smallrye.openapi.gradleplugin;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipFile;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.openapi.api.OpenApiConfig.OperationIdStrategy;

class SmallryeOpenApiPluginTest {
    @Test
    void pluginRegistersExtensionAndTask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java-library");
        project.getPlugins().apply("io.smallrye.openapi");

        Object ext = project.getExtensions().findByName(SmallryeOpenApiPlugin.EXTENSION_NAME);
        assertThat(ext).isInstanceOf(SmallryeOpenApiExtension.class);

        // Verify the result
        Task task = project.getTasks().findByName(SmallryeOpenApiPlugin.TASK_NAME);
        assertThat(task).isInstanceOf(SmallryeOpenApiTask.class);
    }

    @Test
    void generated() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java-library");
        project.getPlugins().apply("io.smallrye.openapi");

        SmallryeOpenApiTask task = project.getTasks()
                .named(SmallryeOpenApiPlugin.TASK_NAME, SmallryeOpenApiTask.class).get();
        assertThat(task.getOutputDirectory().getAsFile().get().toPath())
                .matches(p -> p.endsWith(Paths.get("build", "generated", "openapi")));
    }

    @Test
    void taskPropertiesInheritance() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("java-library");
        project.getPlugins().apply("io.smallrye.openapi");

        SmallryeOpenApiExtension ext = project.getExtensions()
                .findByType(SmallryeOpenApiExtension.class);
        assertThat(ext).isNotNull();

        SmallryeOpenApiTask task = project.getTasks()
                .named(SmallryeOpenApiPlugin.TASK_NAME, SmallryeOpenApiTask.class).get();

        ext.applicationPathDisable.set(true);
        ext.configProperties.set(new File("/foo/bar"));
        ext.encoding.set("ISO-8859-1");
        ext.openApiVersion.set("3.0.0");
        ext.customSchemaRegistryClass.set("foo.bar.Baz");
        ext.filter.set("filter");
        ext.infoContactEmail.set("info-email");
        ext.infoContactName.set("info-name");
        ext.infoContactUrl.set("info-contact-url");
        ext.infoDescription.set("info-description");
        ext.infoLicenseName.set("info-license-name");
        ext.infoLicenseUrl.set("info-license-url");
        ext.infoTermsOfService.set("info-tos");
        ext.infoTitle.set("info-title");
        ext.infoVersion.set("info-version");
        ext.modelReader.set("model-reader");
        ext.operationIdStrategy.set(OperationIdStrategy.CLASS_METHOD);
        ext.operationServers.put("server", "foo");
        ext.pathServers.put("path", "server");
        ext.scanClasses.addAll("scan", "classes");
        ext.scanDependenciesDisable.set(true);
        ext.scanDisabled.set(true);
        ext.scanExcludeClasses.addAll("scan", "exclude", "classes");
        ext.scanExcludePackages.addAll("scan", "exclude", "packages");
        ext.scanPackages.addAll("scan", "packages");
        ext.scanExcludeProfiles.addAll("scan", "exclude", "profiles");
        ext.scanProfiles.addAll("scan", "profiles");
        ext.schemaFilename.set("schema-filename");
        ext.servers.addAll("servers");

        List<Function<SmallryeOpenApiProperties, Provider<?>>> getters = asList(
                SmallryeOpenApiProperties::getOpenApiVersion,
                SmallryeOpenApiProperties::getConfigProperties,
                SmallryeOpenApiProperties::getScanDependenciesDisable,
                SmallryeOpenApiProperties::getFilter,
                SmallryeOpenApiProperties::getEncoding,
                SmallryeOpenApiProperties::getCustomSchemaRegistryClass,
                SmallryeOpenApiProperties::getApplicationPathDisable,
                SmallryeOpenApiProperties::getInfoContactEmail,
                SmallryeOpenApiProperties::getInfoContactName,
                SmallryeOpenApiProperties::getInfoContactUrl,
                SmallryeOpenApiProperties::getInfoDescription,
                SmallryeOpenApiProperties::getInfoLicenseName,
                SmallryeOpenApiProperties::getInfoLicenseUrl,
                SmallryeOpenApiProperties::getInfoTermsOfService,
                SmallryeOpenApiProperties::getInfoTitle,
                SmallryeOpenApiProperties::getInfoVersion,
                SmallryeOpenApiProperties::getModelReader,
                SmallryeOpenApiProperties::getOperationIdStrategy,
                SmallryeOpenApiProperties::getOperationServers,
                SmallryeOpenApiProperties::getPathServers,
                SmallryeOpenApiProperties::getScanClasses,
                SmallryeOpenApiProperties::getScanDependenciesDisable,
                SmallryeOpenApiProperties::getScanDisabled,
                SmallryeOpenApiProperties::getScanExcludeClasses,
                SmallryeOpenApiProperties::getScanExcludePackages,
                SmallryeOpenApiProperties::getScanExcludeProfiles,
                SmallryeOpenApiProperties::getScanPackages,
                SmallryeOpenApiProperties::getScanProfiles,
                SmallryeOpenApiProperties::getSchemaFilename,
                SmallryeOpenApiProperties::getServers);

        for (Function<SmallryeOpenApiProperties, ? extends Provider<?>> getter : getters) {
            assertThat(getter.apply(task).get()).isEqualTo(getter.apply(ext).get());
        }
    }

    @Test
    void simpleProject(@TempDir Path buildDir) throws Exception {
        // "Simple" Gradle project
        smokeProject(buildDir, false, SmallryeOpenApiPlugin.TASK_NAME);
    }

    @Test
    void simpleProjectWithJustYamlOutput(@TempDir Path buildDir) throws Exception {
        // "Simple" Gradle project
        smokeProject(buildDir, false, SmallryeOpenApiPlugin.TASK_NAME, "YAML");
    }

    @Test
    void simpleProjectWithJustJsonOutput(@TempDir Path buildDir) throws Exception {
        // "Simple" Gradle project
        smokeProject(buildDir, false, SmallryeOpenApiPlugin.TASK_NAME, "JSON");
    }

    @Test
    void quarkusProjectGenApiOnly(@TempDir Path buildDir) throws Exception {
        // Quarkus Gradle project, just call the generateOpenApiSpec task
        smokeProject(buildDir, true, SmallryeOpenApiPlugin.TASK_NAME);
    }

    @Test
    void quarkusProject(@TempDir Path buildDir) throws Exception {
        // Quarkus Gradle project, perform a "full Quarkus build"
        smokeProject(buildDir, true, "quarkusBuild");
    }

    void smokeProject(Path buildDir, boolean withQuarkus, String taskName) throws Exception {
        smokeProject(buildDir, withQuarkus, taskName, "ALL");
    }

    void smokeProject(Path buildDir, boolean withQuarkus, String taskName, String outputFileTypeFilter) throws Exception {
        Files.write(buildDir.resolve("settings.gradle"),
                singletonList("rootProject.name = 'smoke-test-project'"));

        Files.write(buildDir.resolve("build.gradle"),
                asList(
                        "import io.smallrye.openapi.api.OpenApiConfig.OperationIdStrategy",
                        "",
                        "plugins {",
                        "    id 'java-library'",
                        "    id 'io.smallrye.openapi'",
                        withQuarkus ? "    id 'io.quarkus' version '2.15.2.Final'" : "",
                        "}",
                        "",
                        "repositories {",
                        "    mavenCentral()",
                        "}",
                        "",
                        "dependencies {",
                        "  implementation(\"javax.ws.rs:javax.ws.rs-api:2.1.1\")",
                        "  implementation(\"org.eclipse.microprofile.openapi:microprofile-openapi-api:3.0\")",
                        "}",
                        "",
                        "smallryeOpenApi {",
                        "  openApiVersion.set(\"3.0.2\")",
                        "  schemaFilename.set(\"my-openapi-schema-file\")",
                        "  infoTitle.set(\"Info Title\")",
                        "  infoVersion.set(\"Info Version\")",
                        "  infoDescription.set(\"Info Description\")",
                        "  infoTermsOfService.set(\"Info TOS\")",
                        "  infoContactEmail.set(\"Info Email\")",
                        "  infoContactName.set(\"Info Contact\")",
                        "  infoContactUrl.set(\"https://github.com/smallrye/smallrye-open-api/issues/1231\")",
                        "  infoLicenseName.set(\"Apache 2.0\")",
                        "  infoLicenseUrl.set(\"http://www.apache.org/licenses/LICENSE-2.0.html\")",
                        "  operationIdStrategy.set(OperationIdStrategy.METHOD)",
                        "  outputFileTypeFilter.set(\"" + outputFileTypeFilter + "\")",
                        "}"));

        Path javaDir = Paths.get("src/main/java/testcases");
        Files.createDirectories(buildDir.resolve(javaDir));
        Files.write(buildDir.resolve(javaDir.resolve("DummyJaxRs.java")),
                asList("package testcases;",
                        "",
                        "import javax.ws.rs.GET;",
                        "import javax.ws.rs.Path;",
                        "import javax.ws.rs.Produces;",
                        "import javax.ws.rs.core.MediaType;",
                        "import org.eclipse.microprofile.openapi.annotations.Operation;",
                        "import org.eclipse.microprofile.openapi.annotations.media.Content;",
                        "import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;",
                        "import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;",
                        "",
                        "@Path(\"/mypath\")",
                        "public class DummyJaxRs {",
                        "    @GET",
                        "    @Produces(MediaType.APPLICATION_JSON)",
                        "    @Operation(summary = \"List all configuration settings\")",
                        "    @APIResponses({",
                        "      @APIResponse(",
                        "          description = \"Dummy get thing.\",",
                        "          content = @Content(mediaType = \"application/text\")",
                        "      )})",
                        "    public String dummyThing() {",
                        "        return \"foo\";",
                        "    }",
                        "}"));

        runGradleTask(buildDir, taskName, withQuarkus);

        checkGeneratedFiles(buildDir, outputFileTypeFilter);

        checkJarContents(buildDir, withQuarkus, outputFileTypeFilter);
    }

    private static void checkJarContents(Path buildDir, boolean withQuarkus, String expectedOutputFileType) throws Exception {
        runGradleTask(buildDir, "jar", withQuarkus);

        Path jarFile = buildDir.resolve("build/libs/smoke-test-project.jar");
        assertThat(jarFile).isRegularFile();

        try (ZipFile zipFile = new ZipFile(jarFile.toFile())) {
            if ("ALL".equals(expectedOutputFileType) || "YAML".equals(expectedOutputFileType)) {
                assertThat(zipFile.getEntry("my-openapi-schema-file.yaml")).isNotNull();
            }
            if ("ALL".equals(expectedOutputFileType) || "JSON".equals(expectedOutputFileType)) {
                assertThat(zipFile.getEntry("my-openapi-schema-file.json")).isNotNull();
            }
            assertThat(zipFile.getEntry("testcases/DummyJaxRs.class")).isNotNull();
            assertThat(zipFile.getEntry("META-INF/MANIFEST.MF")).isNotNull();
        }
    }

    private static void checkGeneratedFiles(Path buildDir, String expectedOutputFileType) throws IOException {
        Path targetOpenapiDir = buildDir.resolve("build/generated/openapi");
        assertThat(targetOpenapiDir).isDirectory();

        if ("ALL".equals(expectedOutputFileType)) {
            assertThat(targetOpenapiDir.resolve("my-openapi-schema-file.yaml")).isRegularFile();
            assertThat(targetOpenapiDir.resolve("my-openapi-schema-file.json")).isRegularFile();

            JsonNode root = new ObjectMapper().readValue(
                    targetOpenapiDir.resolve("my-openapi-schema-file.json").toUri().toURL(),
                    JsonNode.class);
            assertThat(root.get("openapi").asText()).isEqualTo("3.0.2");

            JsonNode info = root.get("info");
            assertThat(info).isNotNull();
            assertThat(info.get("title").asText()).isEqualTo("Info Title");
            assertThat(info.get("description").asText()).isEqualTo("Info Description");
            assertThat(info.get("termsOfService").asText()).isEqualTo("Info TOS");
            assertThat(info.get("version").asText()).isEqualTo("Info Version");
            assertThat(info.get("contact").get("email").asText()).isEqualTo("Info Email");
            assertThat(info.get("contact").get("name").asText()).isEqualTo("Info Contact");
            assertThat(info.get("contact").get("url").asText())
                    .isEqualTo("https://github.com/smallrye/smallrye-open-api/issues/1231");
            assertThat(info.get("license").get("name").asText()).isEqualTo("Apache 2.0");
            assertThat(info.get("license").get("url").asText()).isEqualTo("http://www.apache.org/licenses/LICENSE-2.0.html");

            JsonNode paths = root.get("paths");
            assertThat(paths.get("/mypath").get("get").get("operationId").asText()).isEqualTo("dummyThing");
        }

        if ("YAML".equals(expectedOutputFileType)) {
            assertThat(targetOpenapiDir.resolve("my-openapi-schema-file.yaml")).isRegularFile();
            assertThat(targetOpenapiDir.resolve("my-openapi-schema-file.json")).doesNotExist();
        }

        if ("JSON".equals(expectedOutputFileType)) {
            assertThat(targetOpenapiDir.resolve("my-openapi-schema-file.yaml")).doesNotExist();
            assertThat(targetOpenapiDir.resolve("my-openapi-schema-file.json")).isRegularFile();
        }
    }

    private static void runGradleTask(Path buildDir, String taskName, boolean withQuarkus) {
        List<String> args = new ArrayList<>();
        if (!withQuarkus) {
            // The Quarkus plugin **might** be ready for Gradle configuration cache starting with
            // Quarkus 3.0.0.GA.
            args.add("--configuration-cache");
        }
        args.addAll(Arrays.asList("--build-cache",
                // Quarkus plugin just needs this property to be present
                "-Dquarkus.native.builder-image=x",
                "--info",
                "--stacktrace",
                taskName));

        GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(buildDir.toFile())
                .withArguments(args)
                .withDebug(true)
                .forwardOutput().build();
    }
}
