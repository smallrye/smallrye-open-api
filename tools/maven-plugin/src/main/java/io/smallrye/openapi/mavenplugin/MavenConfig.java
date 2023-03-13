package io.smallrye.openapi.mavenplugin;

import java.util.Map;
import java.util.Optional;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigBase;

/**
 * Implementation of the {@link OpenApiConfig} interface that gets config information from maven
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MavenConfig extends OpenApiConfigBase implements OpenApiConfig {

    private final Map<String, String> properties;

    public MavenConfig(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    protected Iterable<String> getPropertyNames() {
        return properties.keySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T getValue(String propertyName, Class<T> type) {
        String rawValue = properties.get(propertyName);

        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }

        if (String.class.equals(type)) {
            return (T) rawValue;
        } else if (Boolean.class.equals(type)) {
            return (T) Boolean.valueOf(rawValue);
        } else if (String[].class.equals(type)) {
            return (T) rawValue.split("\\,");
        } else if (Integer.class.equals(type)) {
            return (T) Integer.valueOf(rawValue);
        }

        throw new IllegalArgumentException(type.toString());
    }

    @Override
    protected <T> Optional<T> getOptionalValue(String propertyName, Class<T> type) {
        return Optional.ofNullable(getValue(propertyName, type));
    }

}
