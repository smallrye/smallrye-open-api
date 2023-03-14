package io.smallrye.openapi.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.Config;

/**
 * Implementation of the {@link OpenApiConfig} interface that gets config information from a
 * standard MP Config object.
 *
 * @author eric.wittmann@gmail.com
 */
public class OpenApiConfigImpl implements OpenApiConfig {

    private Config config;
    private Map<String, Object> cache = new HashMap<>();
    private Optional<Boolean> allowNakedPathParameter = Optional.empty();

    /**
     * @deprecated use {@link OpenApiConfig#fromConfig(Config)} instead
     */
    @Deprecated
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

    @Override
    public Optional<Boolean> allowNakedPathParameter() {
        return allowNakedPathParameter;
    }

    @Override
    public void setAllowNakedPathParameter(Boolean allowNakedPathParameter) {
        this.allowNakedPathParameter = Optional.ofNullable(allowNakedPathParameter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, T> T getConfigValue(String propertyName, Class<R> type, Function<R, T> converter, Supplier<T> defaultValue) {
        if (cache.containsKey(propertyName)) {
            return (T) cache.get(propertyName);
        }

        T value = getOptionalValue(propertyName, type)
                .filter(configValue -> {
                    /*
                     * configValue can be "" if optional {@link org.eclipse.microprofile.config.spi.Converter}s are used.
                     * Enforce a null value if we get an empty string back.
                     */
                    if (String.class.equals(type)) {
                        return !configValue.toString().trim().isEmpty();
                    }

                    return true;
                })
                .map(converter)
                .orElseGet(defaultValue);

        cache.put(propertyName, value);

        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, T> Map<String, T> getConfigValueMap(String propertyNamePrefix, Class<R> type, Function<R, T> converter) {
        if (cache.containsKey(propertyNamePrefix)) {
            return (Map<String, T>) cache.get(propertyNamePrefix);
        }

        Map<String, T> valueMap = StreamSupport.stream(getPropertyNames().spliterator(), false)
                .filter(propertyName -> propertyName.startsWith(propertyNamePrefix))
                .collect(Collectors.toMap(
                        name -> name.substring(propertyNamePrefix.length()),
                        name -> converter.apply(getValue(name, type))));

        cache.put(propertyNamePrefix, valueMap);

        return valueMap;
    }
}
