package io.smallrye.openapi.api;

import java.util.Optional;

import org.eclipse.microprofile.config.Config;

/**
 * Implementation of the {@link OpenApiConfig} interface that gets config information from a
 * standard MP Config object.
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApiConfigImpl extends OpenApiConfigBase implements OpenApiConfig {

    private Config config;

    public static OpenApiConfig fromConfig(Config config) {
        return new OpenApiConfigImpl(config);
    }

    /**
     * Constructor.
     *
     * @param config MicroProfile Config instance
     */
    public OpenApiConfigImpl(Config config) {
        this.config = config;
    }

    /**
     * @return the MP config instance
     */
    protected Config getConfig() {
        // We cannot use ConfigProvider.getConfig() as the archive is not deployed yet - TCCL cannot be set
        return config;
    }

    protected Iterable<String> getPropertyNames() {
        return getConfig().getPropertyNames();
    }

    protected <T> T getValue(String propertyName, Class<T> type) {
        return getConfig().getValue(propertyName, type);
    }

    protected <T> Optional<T> getOptionalValue(String propertyName, Class<T> type) {
        return getConfig().getOptionalValue(propertyName, type);
    }

}
