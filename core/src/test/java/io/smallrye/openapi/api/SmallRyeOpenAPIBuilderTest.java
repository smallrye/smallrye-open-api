package io.smallrye.openapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.junit.jupiter.api.Test;

class SmallRyeOpenAPIBuilderTest {

    @Test
    void testStaticFileLoadedFromClasspath() throws Exception {
        URL loaderRoot = getClass()
                .getClassLoader()
                .getResource("classloader/META-INF/openapi.yaml")
                .toURI()
                .resolve("..")
                .toURL();

        ClassLoader custom = new URLClassLoader(
                new URL[] { loaderRoot },
                Thread.currentThread().getContextClassLoader());

        SmallRyeOpenAPI result = SmallRyeOpenAPI.builder()
                .withApplicationClassLoader(custom)
                .enableModelReader(false)
                .enableStandardFilter(false)
                .enableAnnotationScan(false)
                .enableStandardStaticFiles(true)
                .build();

        OpenAPI model = result.model();
        assertEquals("Loaded from the class path", model.getInfo().getTitle());
    }

}
