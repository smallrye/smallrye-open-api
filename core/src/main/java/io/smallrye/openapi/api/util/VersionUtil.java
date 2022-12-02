package io.smallrye.openapi.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import io.smallrye.openapi.runtime.util.ModelUtil;

public final class VersionUtil {

    private VersionUtil() {
    }

    static final String MP_VERSION = ModelUtil.supply(() -> {
        try (InputStream pomProperties = org.eclipse.microprofile.openapi.models.OpenAPI.class.getResourceAsStream(
                "/META-INF/maven/org.eclipse.microprofile.openapi/microprofile-openapi-api/pom.properties")) {
            Properties p = new Properties();
            p.load(pomProperties);
            return p.getProperty("version");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    });

    static final String[] MP_VERSION_COMPONENTS = ModelUtil.supply(() -> {
        int suffix = MP_VERSION.indexOf('-');
        return (suffix > -1 ? MP_VERSION.substring(0, suffix) : MP_VERSION).split("\\.");
    });

    public static int compareMicroProfileVersion(String checkVersion) {
        String[] checkComponents = checkVersion.split("\\.");
        int max = Math.max(MP_VERSION_COMPONENTS.length, checkComponents.length);
        int result = 0;

        for (int i = 0; i < max; i++) {
            int mp = component(MP_VERSION_COMPONENTS, i);
            int cv = component(checkComponents, i);

            if ((result = Integer.compare(mp, cv)) != 0) {
                break;
            }
        }

        return result;
    }

    static int component(String[] components, int offset) {
        return offset < components.length ? Integer.parseInt(components[offset]) : 0;
    }
}
