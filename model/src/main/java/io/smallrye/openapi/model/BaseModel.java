package io.smallrye.openapi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.Reference;

public abstract class BaseModel<C extends Constructible> {

    protected enum MergeDirective {
        PRESERVE_VALUE,
        OVERRIDE_VALUE,
        MERGE_VALUES
    }

    protected final Map<String, Object> properties = new LinkedHashMap<>(2);
    private int modCount;
    private int hash = 0;

    public static <O extends Constructible> O deepCopy(O other, Class<O> type) {
        return BaseModelSupport.deepCopy(other, type);
    }

    public static <C extends Constructible, T extends BaseModel<C>> T merge(T object1, T object2) {
        return BaseModelSupport.mergeObjects(object1, object2);
    }

    protected BaseModel() {
    }

    protected void incrementModCount() {
        modCount++;
    }

    public int getModCount() {
        return modCount;
    }

    @Override
    public String toString() {
        return String.valueOf(properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof BaseModel) {
            return Objects.equals(properties, ((BaseModel<?>) obj).properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }

        final int result = BaseModelSupport.hash(new IdentityHashMap<>(), this);

        hash = result;
        return result;
    }

    /**
     * Access a BaseModel as its standard interface C
     */
    @SuppressWarnings("unchecked")
    public C constructible() {
        return (C) this;
    }

    public C filter(OASFilter filter, Map<Object, Object> stack) {
        this.modCount += BaseModelSupport.filter(filter, stack, properties);
        return filter(filter);
    }

    /**
     * Apply the provided filter to this instance. This should be overridden by subclasses
     * that have a relevant filter method defined in {@link OASFilter}.
     *
     * Note, this method may return a different instance or null, depending on the return
     * from the filter.
     *
     * @param filter the {@link OASFilter} filter to apply to the instance
     * @return
     */
    @SuppressWarnings("unchecked")
    protected C filter(OASFilter filter) {
        return (C) this;
    }

    protected boolean isExtension(String name) {
        return false;
    }

    /**
     * Merge all properties from another {@link BaseModel} object into this one.
     *
     * @param other the other {@link BaseModel} object
     */
    public <T extends BaseModel<C>> void merge(T other) {
        for (Entry<String, Object> entry : other.properties.entrySet()) {
            String name = entry.getKey();
            MergeDirective mergeDirective = mergeDirective(name);

            Object newValue = entry.getValue();
            Object oldValue = properties.get(entry.getKey());

            if (oldValue == other || newValue == this) {
                ModelLogging.logger.cylicReferenceAvoided(name, getClass().getName());
                return;
            }

            if (oldValue != null && mergeDirective == MergeDirective.PRESERVE_VALUE) {
                // Leave the old value as-is if it is present
            } else if (oldValue == null || oldValue.getClass() != newValue.getClass()
                    || mergeDirective == MergeDirective.OVERRIDE_VALUE) {
                properties.put(name, newValue);
            } else if (oldValue instanceof BaseModel) {
                @SuppressWarnings("unchecked")
                T oldModel = (T) oldValue;
                @SuppressWarnings("unchecked")
                T newModel = (T) newValue;

                oldModel.merge(newModel);
            } else if (oldValue instanceof Map) {
                properties.put(name, BaseModelSupport.mergeObjects(oldValue, newValue));
            } else if (oldValue instanceof List) {
                properties.put(name, BaseModelSupport.mergeObjects(oldValue, newValue));
            } else {
                properties.put(name, newValue);
            }
        }
    }

    /**
     * Determine how the property indicated by name should be merged.
     *
     * @param name property name
     */
    protected MergeDirective mergeDirective(String name) {
        return MergeDirective.MERGE_VALUES;
    }

    /**
     * Returns a read-only view of all properties.
     */
    public Map<String, Object> getAllProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public void setAllProperties(Map<String, ?> allProperties) {
        properties.clear();

        if (allProperties != null) {
            allProperties.forEach(this::setProperty);
        }
    }

    /**
     * Returns a read-only copy of all properties having type T, excluding extensions.
     */
    @SuppressWarnings("unchecked")
    protected <T> Map<String, T> getProperties(Class<T> type) {
        Map<String, T> result = new LinkedHashMap<>(properties.size());

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (type.isInstance(entry.getValue()) && !isExtension(entry.getKey())) {
                result.put(entry.getKey(), (T) entry.getValue());
            }
        }

        return Collections.unmodifiableMap(result);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getProperty(String name) {
        return (T) properties.get(name);
    }

    protected <T> T getProperty(String name, Class<T> type) {
        Object value = getProperty(name);

        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            return null;
        }
    }

    public <T> void setProperty(String name, T value) {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
        incrementModCount();
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> getListProperty(String name) {
        Object value = getProperty(name);

        if (value instanceof List) {
            return Collections.unmodifiableList((List<T>) value);
        } else {
            return null; // NOSONAR
        }
    }

    protected <T> void setListProperty(String name, List<T> value) {
        value = (value != null) ? new ArrayList<>(value) : null;
        setProperty(name, value);
    }

    @SuppressWarnings("unchecked")
    protected <T> void addListPropertyEntry(String name, T value) {
        if (value != null) {
            List<T> list = getProperty(name, List.class);
            if (list == null) {
                list = new ArrayList<>(2);
            }
            list.add(value);
            setProperty(name, list);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> void removeListPropertyEntry(String name, T value) {
        List<T> list = getProperty(name, List.class);
        if (list != null) {
            list.remove(value);
        }
        incrementModCount();
    }

    @SuppressWarnings("unchecked")
    protected <T> Map<String, T> getMapProperty(String name) {
        Object value = getProperty(name);

        if (value instanceof Map) {
            return Collections.unmodifiableMap((Map<String, T>) value);
        } else {
            return null; // NOSONAR
        }
    }

    protected <T> void setMapProperty(String name, Map<String, T> value) {
        value = (value != null) ? new LinkedHashMap<>(value) : null;
        setProperty(name, value);
    }

    protected <V> void putMapPropertyEntry(String name, String key, V value) {
        if (value != null) {
            @SuppressWarnings("unchecked")
            Map<String, V> map = getProperty(name, Map.class);
            if (map == null) {
                map = new LinkedHashMap<>(2);
            }
            map.put(key, value);
            setProperty(name, map);
        }
    }

    protected <V> void removeMapPropertyEntry(String name, String key) {
        @SuppressWarnings("unchecked")
        Map<String, V> map = getProperty(name, Map.class);
        if (map != null) {
            map.remove(key);
        }
        incrementModCount();
    }

    /// Properties used by many models, declare them centrally here.

    public String getRef() {
        return getProperty("$ref", String.class);
    }

    public void setRef(String ref) {
        if (ref != null && !ref.contains("/") && this instanceof Reference) {
            ref = ReferenceType.fromModel((Reference<?>) this).referenceOf(ref);
        }
        setProperty("$ref", ref);
    }

    public String getName() {
        return getProperty("name");
    }

    public void setName(String newValue) {
        setProperty("name", newValue);
    }

    public String getSummary() {
        return getProperty("summary", String.class);
    }

    public void setSummary(String summary) {
        setProperty("summary", summary);
    }

    public String getDescription() {
        return getProperty("description", String.class);
    }

    public void setDescription(String description) {
        setProperty("description", description);
    }

    public org.eclipse.microprofile.openapi.models.ExternalDocumentation getExternalDocs() {
        return getProperty("externalDocs");
    }

    public void setExternalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation newValue) {
        setProperty("externalDocs", newValue);
    }

}
