package io.smallrye.openapi.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

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

    /**
     * Fetch the stream of all available configuration property names.
     */
    protected Stream<String> getPropertyNames() {
        /*
         * Obtain the names directly from the underlying ConfigSources.
         * This bypasses name caching known to occur in smallrye-config
         * that breaks several unit tests.
         */
        Iterable<ConfigSource> sources = getConfig().getConfigSources();

        return StreamSupport.stream(sources.spliterator(), false)
                .map(ConfigSource::getPropertyNames)
                .flatMap(Collection::stream);
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
    public <R, T extends Collection<R>> T getConfigValues(String propertyName,
            Class<R> elementType,
            Function<List<R>, T> converter,
            Supplier<T> defaultValue) {
        if (cache.containsKey(propertyName)) {
            return (T) cache.get(propertyName);
        }

        T value = getConfig().getOptionalValues(propertyName, elementType)
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

        Map<String, T> valueMap = getPropertyNames()
                .filter(propertyName -> propertyName.startsWith(propertyNamePrefix))
                .collect(Collectors.toMap(
                        name -> name.substring(propertyNamePrefix.length()),
                        name -> converter.apply(getValue(name, type))));

        cache.put(propertyNamePrefix, valueMap);

        return valueMap;
    }
}
