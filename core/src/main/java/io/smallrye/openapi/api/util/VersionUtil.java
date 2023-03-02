package io.smallrye.openapi.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.runtime.OpenApiRuntimeException;
import io.smallrye.openapi.runtime.util.ModelUtil;

public final class VersionUtil {

    private VersionUtil() {
    }

    static final String SR_VERSION_PROPERTIES = VersionUtil.class.getName() + ".properties";
    static final String MP_POM_PROPERTIES = "/META-INF/maven/org.eclipse.microprofile.openapi/microprofile-openapi-api/pom.properties"; // NOSONAR

    static final String MP_VERSION = ModelUtil.supply(() -> loadProperty(
            VersionUtil.class.getResource(SR_VERSION_PROPERTIES), "microprofile.openapi.version")
            .orElseGet(() -> loadProperty(OpenAPI.class.getResource(MP_POM_PROPERTIES), "version")
                    .orElseThrow(() -> new OpenApiRuntimeException("Unable to determine MicroProfile OpenAPI version"))));

    static Optional<String> loadProperty(URL source, String key) {
        if (source != null) {
            try (InputStream properties = source.openStream()) {
                Properties p = new Properties();
                p.load(properties);
                return Optional.ofNullable(p.getProperty(key));
            } catch (IOException e) {
                // Ignore it
            }
        }

        return Optional.empty();
    }

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
