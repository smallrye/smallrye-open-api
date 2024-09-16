package io.smallrye.openapi.api.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * Base implementation for extensible model objects which wrap a map of properties
 */
public abstract class MapBasedModelImpl implements ModelImpl {

    protected final LinkedHashMap<String, Object> data = new LinkedHashMap<>();

    protected MapBasedModelImpl() {
    }

    /**
     * Merge all properties from another map-based model object into this one
     * <p>
     * Usually this method will return {@code this}, but it may return {@code other} or a new object.
     *
     * @param other the other map-based model object
     * @return the merged object
     */
    public MapBasedModelImpl mergeFrom(MapBasedModelImpl other) {
        mergeMap(data, other.data, getNonMergableCollections());
        return this;
    }

    public Map<String, Object> getDataMap() {
        return data;
    }

    private static <T> void mergeMap(Map<String, T> into, Map<String, T> from, Set<String> nonMergableNames) {
        for (Entry<String, T> entry : from.entrySet()) {
            String name = entry.getKey();
            T value = entry.getValue();
            T oldValue = into.get(entry.getKey());
            if (oldValue == null || oldValue.getClass() != value.getClass()) {
                into.put(name, value);
            } else {
                if (oldValue instanceof Map && !nonMergableNames.contains(name)) {
                    mergeMap((Map<String, Object>) oldValue, (Map<String, Object>) value, nonMergableNames);
                } else if (oldValue instanceof List && !nonMergableNames.contains(name)) {
                    mergeList((List<Object>) oldValue, (List<Object>) value);
                } else if (oldValue instanceof ModelImpl) {
                    into.put(name, MergeUtil.mergeObjects(oldValue, value));
                } else {
                    into.put(name, value);
                }
            }
        }
    }

    private static <T> void mergeList(List<T> oldValue, List<T> value) {
        Set<T> contents = new HashSet<>(oldValue);
        for (T element : value) {
            if (!contents.contains(element)) {
                oldValue.add(element);
            }
        }
    }

    protected <T> void setProperty(String propertyName, T value) {
        if (value == null) {
            data.remove(propertyName);
        } else {
            data.put(propertyName, value);
        }
    }

    protected <T> T getProperty(String propertyName, Class<T> type) {
        Object result = data.get(propertyName);
        if (type.isInstance(result)) {
            return type.cast(result);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> getListProperty(String propertyName) {
        Object result = data.get(propertyName);
        if (result instanceof List) {
            return Collections.unmodifiableList((List<T>) result);
        } else {
            return null;
        }
    }

    protected <T> void setListProperty(String propertyName, List<T> value) {
        value = ModelUtil.replace(value, ArrayList::new);
        if (value == null) {
            data.remove(propertyName);
        } else {
            data.put(propertyName, value);
        }
    }

    protected <T> void addToListProperty(String propertyName, T value) {
        if (value != null) {
            Object existing = data.get(propertyName);
            List<T> list;
            if (existing instanceof List) {
                list = (List<T>) existing;
            } else {
                list = new ArrayList<>();
                data.put(propertyName, list);
            }
            list.add(value);
        }
    }

    protected <T> void removeFromListProperty(String propertyName, T toRemove) {
        Object existing = data.get(propertyName);
        if (existing instanceof List) {
            List<T> list = (List<T>) existing;
            ModelUtil.remove(list, toRemove);
        }
    }

    protected <T> void setMapProperty(String propertyName, Map<String, T> value) {
        value = ModelUtil.replace(value, HashMap::new);
        if (value == null) {
            data.remove(propertyName);
        } else {
            data.put(propertyName, value);
        }
    }

    protected <T> Map<String, T> getMapProperty(String propertyName) {
        Object result = data.get(propertyName);
        if (result instanceof Map) {
            return Collections.unmodifiableMap((Map<String, T>) data.get(propertyName));
        } else {
            return null;
        }
    }

    protected <T> void addToMapProperty(String propertyName, String key, T value) {
        if (value != null) {
            Object existing = data.get(propertyName);
            Map<String, T> map;
            if (existing instanceof Map) {
                map = (Map<String, T>) existing;
            } else {
                map = new HashMap<>();
                data.put(propertyName, map);
            }
            map.put(key, value);
        }
    }

    protected <T> void removeFromMapProperty(String propertyName, String key) {
        Object existing = data.get(propertyName);
        if (existing instanceof Map) {
            Map<String, T> map = (Map<String, T>) existing;
            ModelUtil.remove(map, key);
        }
    }

    /**
     * Return a list of properties whose values should not be merged even if they're collections
     *
     * @return a list of properties which should be overwritten rather than merged
     */
    protected Set<String> getNonMergableCollections() {
        return Collections.emptySet();
    }

}
