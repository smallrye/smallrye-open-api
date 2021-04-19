package io.smallrye.openapi.runtime.scanner.dataobject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import io.smallrye.openapi.api.constants.JsonbConstants;

public class PropertyNamingStrategyFactory {

    private static final String JSONB_TRANSLATE_NAME = "translateName";
    private static final String JACKSON_TRANSLATE = "translate";
    private static final List<String> knownMethods = Arrays.asList(JSONB_TRANSLATE_NAME, JACKSON_TRANSLATE);
    private static final Map<String, UnaryOperator<String>> STRATEGY_CACHE = new ConcurrentHashMap<>();

    private PropertyNamingStrategyFactory() {
    }

    public static UnaryOperator<String> getStrategy(String configValue, ClassLoader loader) {
        return STRATEGY_CACHE.computeIfAbsent(configValue, key -> create(configValue, loader));
    }

    private static UnaryOperator<String> create(String configValue, ClassLoader loader) {
        switch (configValue) {
            case JsonbConstants.IDENTITY:
                return propertyName -> propertyName;
            case JsonbConstants.LOWER_CASE_WITH_DASHES:
                return new ConfigurableNamingStrategy(Character::toLowerCase, '-');
            case JsonbConstants.LOWER_CASE_WITH_UNDERSCORES:
                return new ConfigurableNamingStrategy(Character::toLowerCase, '_');
            case JsonbConstants.UPPER_CAMEL_CASE:
                return camelCaseStrategy();
            case JsonbConstants.UPPER_CAMEL_CASE_WITH_SPACES:
                final UnaryOperator<String> camelCase = camelCaseStrategy();
                final UnaryOperator<String> space = new ConfigurableNamingStrategy(UnaryOperator.identity(), ' ');
                return propertyName -> camelCase.apply(space.apply(propertyName));
            case JsonbConstants.CASE_INSENSITIVE:
                return propertyName -> propertyName;
            default:
                final Class<?> strategyType;
                final Object strategy;

                try {
                    strategyType = loader.loadClass(configValue);
                    strategy = strategyType.getConstructor().newInstance();
                } catch (Exception e) {
                    throw DataObjectMessages.msg.invalidPropertyNamingStrategyWithCause(configValue, e);
                }

                return Arrays.stream(strategyType.getMethods())
                        .filter(PropertyNamingStrategyFactory::isStringUnaryOperator)
                        .filter(method -> knownMethods.contains(method.getName()))
                        .map(method -> (UnaryOperator<String>) propertyName -> {
                            try {
                                return (String) method.invoke(strategy, propertyName);
                            } catch (Exception e) {
                                throw DataObjectMessages.msg.invalidPropertyNamingStrategyWithCause(configValue, e);
                            }
                        })
                        .findFirst()
                        .orElseThrow(() -> DataObjectMessages.msg.invalidPropertyNamingStrategy(configValue));
        }
    }

    private static boolean isStringUnaryOperator(Method method) {
        if (!String.class.equals(method.getReturnType())) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }

        return String.class.equals(method.getParameterTypes()[0]);
    }

    private static UnaryOperator<String> camelCaseStrategy() {
        return propertyName -> Character.toUpperCase(propertyName.charAt(0))
                + (propertyName.length() > 1 ? propertyName.substring(1) : "");
    }

    private static class ConfigurableNamingStrategy implements UnaryOperator<String> {
        private final Function<Character, Character> converter;
        private final char separator;

        public ConfigurableNamingStrategy(final UnaryOperator<Character> wordConverter, final char sep) {
            this.converter = wordConverter;
            this.separator = sep;
        }

        @Override
        public String apply(final String propertyName) {
            final StringBuilder global = new StringBuilder();
            final StringBuilder current = new StringBuilder();

            for (int i = 0; i < propertyName.length(); i++) {
                final char c = propertyName.charAt(i);

                if (Character.isUpperCase(c)) {
                    final char transformed = converter.apply(c);

                    if (current.length() > 0) {
                        global.append(current).append(separator);
                        current.setLength(0);
                    }

                    current.append(transformed);
                } else {
                    current.append(c);
                }
            }

            if (current.length() > 0) {
                global.append(current);
            } else {
                global.setLength(global.length() - 1); // remove last sep
            }

            return global.toString();
        }
    }

}
