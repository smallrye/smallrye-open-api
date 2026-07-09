package io.smallrye.openapi.runtime.io;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

abstract class JacksonJsonIOTest<V, A extends V, O extends V, AB, OB> extends JsonIOTest<V, A, O, AB, OB> {

    private Properties originalSystemProperties;

    @BeforeEach
    void setup(TestInfo testInfo) {
        originalSystemProperties = System.getProperties();
        Properties testSystemProperties = new Properties();
        originalSystemProperties.forEach(testSystemProperties::put);
        System.setProperties(testSystemProperties);

        testInfo.getTags().stream()
                .filter(tag -> tag.startsWith("property:"))
                .map(tag -> tag.substring("property:".length()))
                .map(tag -> tag.split("="))
                .forEach(tag -> System.setProperty(tag[0], tag[1]));
    }

    @AfterEach
    void teardown() {
        System.setProperties(originalSystemProperties);
    }
}
