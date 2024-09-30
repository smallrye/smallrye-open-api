package io.smallrye.openapi.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.tags.Tag;

class BaseModelSupport {

    private BaseModelSupport() {
    }

    //// Support methods for BaseModel#hash

    static int hash(Map<Object, Object> stack, BaseModel<?> model) {
        int result = 0;

        if (!stack.containsKey(model)) {
            stack.put(model, model);

            for (Map.Entry<String, Object> e : model.properties.entrySet()) {
                result = 31 * result + (e == null ? 0 : hash(stack, e));
            }

            stack.remove(model);
        }

        return result;
    }

    private static int hash(Map<Object, Object> stack, Map.Entry<String, Object> entry) {
        int result = entry.getKey().hashCode();
        result = 31 * result + hash(stack, entry.getValue());
        return result;
    }

    private static int hash(Map<Object, Object> stack, Object value) {
        int result = 0;

        if (value instanceof BaseModel) {
            result = hash(stack, (BaseModel<?>) value);
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                result = 31 * result + (e == null ? 0 : hash(stack, e));
            }
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (Object e : list) {
                result = 31 * result + (e == null ? 0 : hash(stack, e));
            }
        } else {
            result = Objects.hash(value);
        }

        return result;
    }

    //// Support methods for BaseModel#filter

    static int filter(OASFilter filter, Map<Object, Object> stack, Map<String, Object> map) {
        Iterator<Map.Entry<String, Object>> cursor = map.entrySet().iterator();
        int modCount = 0;

        while (cursor.hasNext()) {
            Map.Entry<String, Object> entry = cursor.next();
            Object value = entry.getValue();

            if (stack.containsKey(value)) {
                ModelLogging.logger.cylicReferenceDetected();
            } else if (value instanceof BaseModel) {
                stack.put(value, value);

                Object replacement = ((BaseModel<?>) value).filter(filter, stack);

                if (replacement == null) {
                    cursor.remove();
                    modCount++;
                } else if (replacement != value) {
                    entry.setValue(replacement);
                    modCount++;
                }

                stack.remove(value);
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> listValue = (List<Object>) value;
                modCount += filter(filter, stack, listValue);
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                modCount += filter(filter, stack, mapValue);
            }
        }

        return modCount;
    }

    private static int filter(OASFilter filter, Map<Object, Object> stack, List<Object> list) {
        ListIterator<Object> cursor = list.listIterator();
        int modCount = 0;

        while (cursor.hasNext()) {
            Object value = cursor.next();

            if (stack.containsKey(value)) {
                ModelLogging.logger.cylicReferenceDetected();
            } else if (value instanceof BaseModel) {
                stack.put(value, value);

                Object replacement = ((BaseModel<?>) value).filter(filter, stack);

                if (replacement == null) {
                    cursor.remove();
                    modCount++;
                } else if (replacement != value) {
                    cursor.set(replacement);
                    modCount++;
                }

                stack.remove(value);
            }
        }

        return modCount;
    }

    //// Support methods for BaseModel#deepCopy

    @SuppressWarnings("unchecked")
    static <O extends Constructible> O deepCopy(O other, Class<O> type) {
        BaseModel<O> result = (BaseModel<O>) OASFactory.createObject(type);

        if (other instanceof BaseModel) {
            BaseModel<O> otherImpl = (BaseModel<O>) other;
            result.properties.putAll(BaseModelSupport.deepCopy(otherImpl.properties));
        } else if (other != null) {
            // Maybe support non-BaseModel implementations in the future
            throw new UnsupportedOperationException("Only BaseModel types may be copied: " + other.getClass());
        }

        return (O) result;
    }

    private static <K, V> Map<K, V> deepCopy(Map<K, V> map) {
        Map<K, V> clone = new LinkedHashMap<>(map.size());

        for (Map.Entry<K, V> entry : map.entrySet()) {
            clone.put(entry.getKey(), deepCopy(entry.getValue()));
        }

        return clone;
    }

    private static <T> List<T> deepCopy(List<T> list) {
        List<T> clone = new ArrayList<>(list.size());

        for (T value : list) {
            clone.add(deepCopy(value));
        }

        return clone;
    }

    @SuppressWarnings("unchecked")
    private static <T, N extends Constructible> T deepCopy(T value) {
        if (value instanceof Map) {
            return (T) deepCopy((Map<?, ?>) value);
        } else if (value instanceof List) {
            return (T) deepCopy((List<?>) value);
        } else if (value instanceof BaseModel) {
            N nested = (N) value;
            Class<N> nestedType = (Class<N>) nested.getClass();
            return (T) deepCopy(nested, findConstructible(nestedType));
        } else if (value instanceof Constructible) {
            // Maybe support non-BaseModel implementations in the future
            return value;
        } else {
            return value;
        }
    }

    @SuppressWarnings("unchecked")
    private static <C extends Constructible> Class<C> findConstructible(Class<?> type) {
        for (Class<?> i : type.getInterfaces()) {
            if (Constructible.class.equals(i)) {
                return (Class<C>) type;
            }

            Class<C> result = findConstructible(i);

            if (result != null) {
                return result;
            }
        }

        Class<?> parent = type.getSuperclass();

        if (parent == null && type.isInterface()) {
            return null;
        }

        if (parent == null || Object.class.equals(parent)) {
            throw new IllegalStateException("Failed to find direct Constructible interface: " + type);
        }

        return findConstructible(parent);
    }

    //// Support methods for BaseModel#merge

    public static <C extends Constructible, T> T mergeObjects(T object1, T object2) {
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

        if (object1 instanceof BaseModel) {
            @SuppressWarnings("unchecked")
            BaseModel<C> model1 = (BaseModel<C>) object1;
            @SuppressWarnings("unchecked")
            BaseModel<C> model2 = (BaseModel<C>) object2;

            model1.merge(model2);

            return object1;
        }

        if (object1 instanceof List) {
            @SuppressWarnings("unchecked")
            T result = (T) mergeLists((List<?>) object1, (List<?>) object2);
            return result;
        }

        if (object1 instanceof Map) {
            @SuppressWarnings("unchecked")
            T result = (T) mergeMaps((Map<?, ?>) object1, (Map<?, ?>) object2);
            return result;
        }

        return object1;
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

        Map<Object, Object> result = new LinkedHashMap<>(values1);
        Set<Map.Entry<String, Object>> entrySet = values2.entrySet();

        for (Map.Entry<String, Object> entry : entrySet) {
            Object key = entry.getKey();
            Object pval2 = entry.getValue();
            Object value;

            if (result.containsKey(key)) {
                Object pval1 = result.get(key);

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

            result.put(key, value);
        }

        return result;
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

        Object firstEntry = values1.get(0);

        if (Set.of(String.class, SchemaType.class).contains(firstEntry.getClass())) {
            return mergeListsWithUnique(values1, values2);
        }

        if (firstEntry instanceof Tag) {
            return mergeTagLists(values1, values2);
        }

        if (firstEntry instanceof Server) {
            return mergeServerLists(values1, values2);
        }

        if (firstEntry instanceof SecurityRequirement) {
            return mergeSecurityRequirementLists(values1, values2);
        }

        if (firstEntry instanceof Parameter) {
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
    private static <T> List<T> mergeListsWithUnique(List<T> values1, List<T> values2) {
        Set<T> set = new LinkedHashSet<>();
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
