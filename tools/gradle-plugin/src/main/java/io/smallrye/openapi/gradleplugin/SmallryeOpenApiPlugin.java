package io.smallrye.openapi.gradleplugin;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.internal.jvm.ClassDirectoryBinaryNamingScheme;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

/**
 * Gradle schema generator plugin.
 *
 * <p>
 * Registers {@link SmallryeOpenApiExtension} as {@value #EXTENSION_NAME} and
 * {@link SmallryeOpenApiTask} as {@value #TASK_NAME} to the Gradle project.
 *
 * <p>
 * Requires any of the Gradle Java plugins, that provide the {@link JavaPluginExtension}.
 *
 * <p>
 * Currently only supports the {@code "main"} source set and uses the {@code compileClasspath}
 * configuration for dependencies.
 */
public class SmallryeOpenApiPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "smallryeOpenApi";
    public static final String TASK_NAME = "generateOpenApiSpec";
    public static final String CONFIG_NAME = "openapi";

    public void apply(Project project) {
        SmallryeOpenApiExtension ext = project.getExtensions()
                .create(SmallryeOpenApiExtension.class, EXTENSION_NAME, SmallryeOpenApiExtension.class);

        JavaPluginExtension javaExtension = project.getExtensions()
                .getByType(JavaPluginExtension.class);
        SourceSet sourceSet = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        ClassDirectoryBinaryNamingScheme namingScheme = new ClassDirectoryBinaryNamingScheme(
                sourceSet.getName());
        String genTaskName = namingScheme.getTaskName(null, TASK_NAME);

        NamedDomainObjectProvider<Configuration> configProvider = project.getConfigurations()
                .named(sourceSet.getCompileClasspathConfigurationName());

        ConfigurableFileCollection resourcesSrcDirs = project.getObjects()
                .fileCollection();
        resourcesSrcDirs.from(sourceSet.getResources().getSrcDirs());

        TaskProvider<SmallryeOpenApiTask> task = project.getTasks()
                .register(
                        genTaskName,
                        SmallryeOpenApiTask.class,
                        ext,
                        configProvider,
                        resourcesSrcDirs,
                        sourceSet.getOutput().getClassesDirs());
        task
                .configure(t -> {
                    t.setGroup("build");
                    t.setDescription("Smallrye OpenAPI generator");
                    t.dependsOn(sourceSet.getCompileJavaTaskName());
                    t.getInputs().files(sourceSet.getAllSource().getSourceDirectories());
                    t.getInputs().files(sourceSet.getOutput().getDirs()).withPathSensitivity(
                            PathSensitivity.RELATIVE);
                    t.getInputs().files(configProvider).withPathSensitivity(PathSensitivity.RELATIVE);
                });

        project.getTasks().named(sourceSet.getJarTaskName(), Jar.class)
                // Adds the generated YAML + JSON files
                .configure(t -> t.from(project.getTasks().getByName(genTaskName)));

        project.getConfigurations().create(CONFIG_NAME, c -> {
            c.setCanBeConsumed(true);
            c.setCanBeResolved(false);
            c.setDescription("Generated OpenAPI spec files");
        });

        project.getArtifacts().add(CONFIG_NAME, project.provider(() -> task.get().getOutputDirectory()),
                artifact -> artifact.builtBy(task));
    }
}
