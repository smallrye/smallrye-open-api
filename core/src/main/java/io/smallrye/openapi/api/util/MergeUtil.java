package io.smallrye.openapi.api.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.Extensible;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Reference;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.tags.Tag;

import io.smallrye.openapi.api.models.MapBasedModelImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.runtime.OpenApiRuntimeException;

/**
 * Used to merge OAI data models into a single one. The MP+OAI 1.0 spec
 * requires that any or all of the various mechanisms for producing an OAI document
 * can be used. When more than one mechanism is used, each mechanism produces an
 * OpenAPI document. These multiple documents must then be sensibly merged into
 * a final result.
 *
 * @author eric.wittmann@gmail.com
 */
public class MergeUtil {

    private static final Set<String> EXCLUDED_PROPERTIES = new HashSet<>();
    static {
        EXCLUDED_PROPERTIES.add("class");
        EXCLUDED_PROPERTIES.add("openapi");
    }

    private MergeUtil() {
    }

    /**
     * Merges documents and returns the result.
     *
     * @param document1 OpenAPIImpl instance
     * @param document2 OpenAPIImpl instance
     * @return Merged OpenAPIImpl instance
     */
    public static final OpenAPI merge(OpenAPI document1, OpenAPI document2) {
        return mergeObjects(document1, document2);
    }

    static <T, P> boolean cycleDetected(String propertyName, T obj1, P prop1, T obj2, P prop2) {
        if (prop1 == obj2 || prop2 == obj1) {
            UtilLogging.logger.cylicReferenceAvoided(propertyName, obj1.getClass().getName());
            return true;
        }
        return false;
    }

    /**
     * Generic merge of two objects of the same type.
     *
     * @param object1 First object
     * @param object2 Second object
     * @param <T> Type parameter
     * @return Merged object
     */
    public static <T> T mergeObjects(T object1, T object2) {
        if (object1 == object2) {
            return object1;
        }
        if (object1 == null) {
            return object2;
        }
        if (object2 == null) {
            return object1;
        }

        // It's uncommon, but in some cases (like Link Parameters or Examples) the values could
        // be different types.  In this case, just take the 2nd one (the override).
        if (!object1.getClass().equals(object2.getClass())) {
            return object2;
        }

        // Some model objects are just wrappers around a map of properties and need merged differently
        if (object1 instanceof MapBasedModelImpl) {
            ((MapBasedModelImpl) object1).mergeFrom((MapBasedModelImpl) object2);
            return object1;
        }

        try {
            Arrays.stream(Introspector.getBeanInfo(object1.getClass()).getPropertyDescriptors())
                    .filter(descriptor -> !EXCLUDED_PROPERTIES.contains(descriptor.getName()))
                    .filter(descriptor -> Objects.nonNull(descriptor.getWriteMethod()))
                    .forEach(descriptor -> {
                        try {
                            mergeProperty(object1, object2, descriptor);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            throw new OpenApiRuntimeException(e);
                        }
                    });
        } catch (IntrospectionException e) {
            UtilLogging.logger.failedToIntrospectBeanInfo(object1.getClass(), e);
        }

        return object1;
    }

    @SuppressWarnings({ "rawtypes" })
    static <T> void mergeProperty(T object1, T object2, PropertyDescriptor descriptor)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Class<?> ptype = descriptor.getPropertyType();
        Method writeMethod = descriptor.getWriteMethod();

        if (Constructible.class.isAssignableFrom(ptype)) {
            Object val1 = descriptor.getReadMethod().invoke(object1);
            Object val2 = descriptor.getReadMethod().invoke(object2);
            if (!cycleDetected(descriptor.getName(), object1, val1, object2, val2)) {
                Object newValue = mergeObjects(val1, val2);
                if (newValue != null) {
                    writeMethod.invoke(object1, newValue);
                }
            }
        } else if (Map.class.isAssignableFrom(ptype)) {
            Map values1 = (Map) descriptor.getReadMethod().invoke(object1);
            Map values2 = (Map) descriptor.getReadMethod().invoke(object2);
            Map newValues = mergeMaps(values1, values2);
            writeMethod.invoke(object1, newValues);
        } else if (List.class.isAssignableFrom(ptype)) {
            List values1 = (List) descriptor.getReadMethod().invoke(object1);
            List values2 = (List) descriptor.getReadMethod().invoke(object2);
            List newValues = mergeLists(values1, values2);
            writeMethod.invoke(object1, newValues);
        } else {
            Object newValue = descriptor.getReadMethod().invoke(object2);
            if (newValue != null) {
                writeMethod.invoke(object1, newValue);
            }
        }
    }

    /**
     * Merges two Maps. Any values missing from Map1 but present in Map2 will be added. If a value
     * is present in both maps, it will be overridden or merged.
     *
     * @param values1
     * @param values2
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Map mergeMaps(Map values1, Map values2) {
        if (values1 == values2) {
            return values1;
        }
        if (values1 == null) {
            return values2;
        }
        if (values2 == null) {
            return values1;
        }

        if (!(values1 instanceof ModelImpl)) {
            values1 = new LinkedHashMap<>(values1);
        }
        if (!(values2 instanceof ModelImpl)) {
            values2 = new LinkedHashMap<>(values2);
        }

        Map<Object, Object> targetValues = values1;
        Set<Map.Entry<?, ?>> entrySet = values2.entrySet();

        entrySet.stream()
                .map(entry -> {
                    Object key = entry.getKey();
                    Object pval2 = entry.getValue();
                    Object value;

                    if (targetValues.containsKey(key)) {
                        Object pval1 = targetValues.get(key);

                        if (pval1 instanceof Map) {
                            value = mergeMaps((Map) pval1, (Map) pval2);
                        } else if (pval1 instanceof List) {
                            value = mergeLists((List) pval1, (List) pval2);
                        } else if (pval1 instanceof Constructible) {
                            value = mergeObjects(pval1, pval2);
                        } else {
                            value = pval2;
                        }
                    } else {
                        value = pval2;
                    }

                    return new AbstractMap.SimpleEntry<>(key, value);
                })
                .forEach(modifiedEntry -> targetValues.put(modifiedEntry.getKey(), modifiedEntry.getValue()));

        if (values1 instanceof Constructible) {
            mergeConstructible(values1, values2);
        }

        return values1;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static void mergeConstructible(Map values1, Map values2) {
        if (values1 instanceof Reference) {
            Reference ref1 = (Reference) values1;
            Reference ref2 = (Reference) values2;
            if (ref2.getRef() != null) {
                ref1.setRef(ref2.getRef());
            }
        }

        if (values1 instanceof Extensible) {
            Extensible extensible1 = (Extensible) values1;
            Extensible extensible2 = (Extensible) values2;
            extensible1.setExtensions(mergeMaps(extensible1.getExtensions(), extensible2.getExtensions()));
        }

        if (values1 instanceof APIResponses) {
            APIResponses responses1 = (APIResponses) values1;
            APIResponses responses2 = (APIResponses) values2;
            responses1.defaultValue(mergeObjects(responses1.getDefaultValue(), responses2.getDefaultValue()));
        }
    }

    /**
     * Merges two Lists. Any values missing from List1 but present in List2 will be added. Depending on
     * the type of list, further processing and de-duping may be required.
     *
     * @param values1
     * @param values2
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List mergeLists(List values1, List values2) {
        if (Objects.equals(values1, values2)) {
            // Do not merge if lists identical, both null, or both the same reference
            return values1;
        }
        if (values2 == null) {
            return values1;
        }
        if ((values1 == null || values1.isEmpty())) {
            return values2;
        }

        if (values1.get(0) instanceof String) {
            return mergeStringLists(values1, values2);
        }

        if (values1.get(0) instanceof Tag) {
            return mergeTagLists(values1, values2);
        }

        if (values1.get(0) instanceof Server) {
            return mergeServerLists(values1, values2);
        }

        if (values1.get(0) instanceof SecurityRequirement) {
            return mergeSecurityRequirementLists(values1, values2);
        }

        if (values1.get(0) instanceof Parameter) {
            return mergeParameterLists(values1, values2);
        }

        List merged = new ArrayList<>(values1.size() + values2.size());
        merged.addAll(values1);
        merged.addAll(values2);
        return merged;
    }

    /**
     * Merge a list of strings. In all cases, string lists are really sets. So this is just
     * combining the two lists and then culling duplicates.
     *
     * @param values1
     * @param values2
     */
    private static List<String> mergeStringLists(List<String> values1, List<String> values2) {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(values1);
        set.addAll(values2);
        return new ArrayList<>(set);
    }

    /**
     * Merge two lists of Tags. Tags are a special case because they are named and you cannot
     * have two Tags with the same name. This will append any tags from values2 that don't
     * exist in values1. It will *merge* any tags found in values2 that already exist in
     * values1.
     *
     * @param values1
     * @param values2
     */
    private static List<Tag> mergeTagLists(List<Tag> values1, List<Tag> values2) {
        values1 = new ArrayList<>(values1);

        for (Tag value2 : values2) {
            Tag match = null;
            for (Tag value1 : values1) {
                if (value1.getName() != null && value1.getName().equals(value2.getName())) {
                    match = value1;
                    break;
                }
            }
            if (match == null) {
                values1.add(value2);
            } else {
                mergeObjects(match, value2);
            }
        }
        return values1;
    }

    /**
     * Merge two lists of Servers. Servers are a special case because they must be unique
     * by the 'url' property each must have.
     *
     * @param values1
     * @param values2
     */
    private static List<Server> mergeServerLists(List<Server> values1, List<Server> values2) {
        values1 = new ArrayList<>(values1);

        for (Server value2 : values2) {
            Server match = null;
            for (Server value1 : values1) {
                if (value1.getUrl() != null && value1.getUrl().equals(value2.getUrl())) {
                    match = value1;
                    break;
                }
            }
            if (match == null) {
                values1.add(value2);
            } else {
                mergeObjects(match, value2);
            }
        }
        return values1;
    }

    /**
     * Merge two lists of Security Requirements. Security Requirement lists are are a
     * special case because
     * values1.
     *
     * @param values1
     * @param values2
     */
    private static List<SecurityRequirement> mergeSecurityRequirementLists(List<SecurityRequirement> values1,
            List<SecurityRequirement> values2) {

        values1 = new ArrayList<>(values1);

        for (SecurityRequirement value2 : values2) {
            if (values1.contains(value2)) {
                continue;
            }
            values1.add(value2);
        }
        return values1;
    }

    /**
     * Merge two lists of Parameters. Parameters are a special case because they must be unique
     * by the name in 'in' each have
     *
     * @param values1
     * @param values2
     */
    private static List<Parameter> mergeParameterLists(List<Parameter> values1, List<Parameter> values2) {
        List<Parameter> mutableValues = new ArrayList<>(values1);

        values2.stream()
                .filter(v -> Objects.nonNull(v.getName()))
                .filter(v -> Objects.nonNull(v.getIn()))
                .forEach(value2 -> {
                    Optional<Parameter> match = mutableValues.stream()
                            .filter(value1 -> Objects.equals(value1.getName(), value2.getName()))
                            .filter(value1 -> Objects.equals(value1.getIn(), value2.getIn()))
                            .findFirst();

                    if (match.isPresent()) {
                        mergeObjects(match.get(), value2);
                    } else {
                        mutableValues.add(value2);
                    }
                });

        return mutableValues;
    }
}
