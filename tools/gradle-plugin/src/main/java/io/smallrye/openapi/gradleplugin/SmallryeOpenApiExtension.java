package io.smallrye.openapi.gradleplugin;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;

/**
 * Gradle extension objects, which allows Gradle project wide defaults, or just easier configuration
 * in Gradle build scripts.
 *
 * <p>
 * See {@link SmallryeOpenApiProperties} for information about the individual options.
 */
public class SmallryeOpenApiExtension extends Configs {

    @Inject
    public SmallryeOpenApiExtension(ObjectFactory objects) {
        super(objects);
    }

}
