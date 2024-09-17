package io.smallrye.openapi.runtime.scanner.dataobject;

import static io.smallrye.openapi.api.constants.JacksonConstants.ENUM_NAMING;
import static io.smallrye.openapi.api.constants.JacksonConstants.JSON_PROPERTY;
import static io.smallrye.openapi.api.constants.JacksonConstants.JSON_VALUE;
import static io.smallrye.openapi.api.constants.JacksonConstants.PROP_VALUE;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

public class EnumProcessor {

    private static final int ENUM = 0x00004000; // see java.lang.reflect.Modifier#ENUM

    private EnumProcessor() {
    }

    public static List<Object> enumConstants(AnnotationScannerContext context, Type enumType) {
        AugmentedIndexView index = context.getAugmentedIndex();
        ClassInfo enumKlazz = index.getClassByName(TypeUtil.getName(enumType));
        Function<FieldInfo, String> nameTranslator = nameTranslator(context, enumKlazz);

        return index.inheritanceChain(enumKlazz, enumType)
                .keySet()
                .stream()
                .flatMap(clazz -> Stream.concat(
                        Stream.of(clazz),
                        clazz.interfaceTypes().stream().map(index::getClass).filter(Objects::nonNull)))
                .filter(clazz -> clazz.hasAnnotation(JSON_VALUE))
                .flatMap(clazz -> clazz.annotationsMap().get(JSON_VALUE).stream())
                // @JsonValue#value (default = true) allows for the functionality to be disabled
                .filter(atJsonValue -> context.annotations().value(atJsonValue, PROP_VALUE, true))
                .map(AnnotationInstance::target)
                .filter(JandexUtil::isSupplier)
                .map(valueTarget -> jacksonJsonValues(context, enumKlazz, valueTarget))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> JandexUtil.fields(context, enumKlazz)
                        .stream()
                        .filter(field -> (field.flags() & ENUM) != 0)
                        .map(nameTranslator::apply)
                        .collect(Collectors.toList()));
    }

    private static List<Object> jacksonJsonValues(AnnotationScannerContext context, ClassInfo enumKlazz,
            AnnotationTarget valueTarget) {
        String className = enumKlazz.name().toString();
        String methodName = valueTarget.asMethod().name();

        try {
            Class<?> loadedEnum = Class.forName(className, false, context.getClassLoader());
            Method valueMethod = loadedEnum.getDeclaredMethod(methodName);
            Object[] constants = loadedEnum.getEnumConstants();

            List<Object> reflectedEnumeration = new ArrayList<>(constants.length);

            for (Object constant : constants) {
                reflectedEnumeration.add(valueMethod.invoke(constant));
            }

            return reflectedEnumeration;
        } catch (Exception e) {
            DataObjectLogging.logger.exceptionReadingEnumJsonValue(className, methodName, e);
        }

        return null; // NOSONAR
    }

    private static Function<FieldInfo, String> nameTranslator(AnnotationScannerContext context, ClassInfo enumKlazz) {
        return Optional.<Type> ofNullable(context.annotations().getAnnotationValue(enumKlazz, ENUM_NAMING, PROP_VALUE))
                .map(namingClass -> namingClass.name().toString())
                .map(namingClass -> PropertyNamingStrategyFactory.getStrategy(namingClass, context.getClassLoader()))
                .<Function<FieldInfo, String>> map(nameStrategy -> fieldInfo -> nameStrategy.apply(fieldInfo.name()))
                .orElse(fieldInfo -> Optional
                        .<String> ofNullable(context.annotations().getAnnotationValue(fieldInfo, JSON_PROPERTY, PROP_VALUE))
                        .orElseGet(fieldInfo::name));
    }
}
