package io.smallrye.openapi.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class OpenApiConfigBase implements OpenApiConfig {

    private Map<String, Object> cache = new HashMap<>();
    private Optional<Boolean> allowNakedPathParameter = Optional.empty();

    protected abstract Iterable<String> getPropertyNames();

    protected abstract <T> T getValue(String propertyName, Class<T> type);

    protected abstract <T> Optional<T> getOptionalValue(String propertyName, Class<T> type);

    @Override
    public Optional<Boolean> allowNakedPathParameter() {
        return allowNakedPathParameter;
    }

    @Override
    public void doAllowNakedPathParameter() {
        this.allowNakedPathParameter = Optional.of(true);
    }

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
