package io.smallrye.openapi.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.Extensible;

public abstract class BaseExtensibleModel<C extends Extensible<C> & Constructible> extends BaseModel<C>
        implements Extensible<C> {

    private static final Set<String> INITIAL_SET = Collections.emptySet();

    private Set<String> extensionNames = INITIAL_SET;

    protected BaseExtensibleModel() {
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof BaseExtensibleModel) {
            return Objects.equals(extensionNames, ((BaseExtensibleModel<?>) obj).extensionNames);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + Objects.hash(extensionNames);
    }

    /**
     * Returns a read-only view of all properties, excluding private extensions.
     */
    @Override
    public Map<String, Object> getAllProperties() {
        Map<String, Object> properties = new LinkedHashMap<>(super.getAllProperties());
        properties.keySet().removeIf(Extensions::isPrivateExtension);
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public void setAllProperties(Map<String, ?> allProperties) {
        extensionNames.clear();
        super.setAllProperties(allProperties);
    }

    @Override
    protected <P> P getProperty(String name) {
        if (extensionNames.contains(name)) {
            // Do not return extension when accessed as a property
            return null;
        }
        return super.getProperty(name);
    }

    @Override
    protected <P> P getProperty(String name, Class<P> type) {
        if (extensionNames.contains(name)) {
            // Do not return extension when accessed as a property
            return null;
        }
        return super.getProperty(name, type);
    }

    @Override
    public <P> void setProperty(String name, P value) {
        if (extensionNames.contains(name)) {
            // Property replaces the extension entry.
            extensionNames.remove(name);
        }
        super.setProperty(name, value);
    }

    @Override
    protected <V> List<V> getListProperty(String name) {
        if (extensionNames.contains(name)) {
            // Do not return extension when accessed as a property
            return null; // NOSONAR
        }
        return super.getListProperty(name);
    }

    @Override
    protected <V> void setListProperty(String name, List<V> value) {
        if (extensionNames.contains(name)) {
            // Property replaces the extension entry.
            extensionNames.remove(name);
        }
        super.setListProperty(name, value);
    }

    @Override
    protected <V> void addListPropertyEntry(String name, V value) {
        if (extensionNames.contains(name)) {
            // Property replaces the extension entry.
            extensionNames.remove(name);
        }
        super.addListPropertyEntry(name, value);
    }

    @Override
    protected <V> void removeListPropertyEntry(String name, V value) {
        if (extensionNames.contains(name)) {
            // Do not remove extension when removed as a property
            return;
        }
        super.removeListPropertyEntry(name, value);
    }

    @Override
    protected <V> Map<String, V> getMapProperty(String name) {
        if (extensionNames.contains(name)) {
            // Do not return extension when accessed as a property
            return null; // NOSONAR
        }
        return super.getMapProperty(name);
    }

    @Override
    protected <V> void setMapProperty(String name, Map<String, V> value) {
        if (extensionNames.contains(name)) {
            // Property replaces the extension entry.
            extensionNames.remove(name);
        }
        super.setMapProperty(name, value);
    }

    @Override
    protected <V> void putMapPropertyEntry(String name, String key, V value) {
        if (extensionNames.contains(name)) {
            // Property replaces the extension entry.
            extensionNames.remove(name);
        }
        super.putMapPropertyEntry(name, key, value);
    }

    @Override
    protected void removeMapPropertyEntry(String name, String key) {
        if (extensionNames.contains(name)) {
            // Do not remove extension when removed as a property
            return;
        }
        super.removeMapPropertyEntry(name, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getExtensions() {
        return getExtensions(false);
    }

    /**
     * Return all set extensions, including those that are private to this
     * implementation.
     */
    public Map<String, Object> getAllExtensions() {
        return getExtensions(true);
    }

    private Map<String, Object> getExtensions(boolean includePrivate) {
        if (extensionNames.isEmpty()) {
            return extensionNames == INITIAL_SET ? null : Collections.emptyMap();
        }

        if (!includePrivate && extensionNames.stream().allMatch(Extensions::isPrivateExtension)) {
            return null; // NOSONAR
        }

        Map<String, Object> ext = new LinkedHashMap<>(extensionNames.size());

        for (String name : extensionNames) {
            if (includePrivate || !Extensions.isPrivateExtension(name)) {
                ext.put(name, super.getProperty(name));
            }
        }

        return Collections.unmodifiableMap(ext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public C addExtension(String name, Object value) {
        if (value != null) {
            if (extensionNames == INITIAL_SET) {
                extensionNames = new LinkedHashSet<>(1);
            }
            extensionNames.add(name);
        }
        // Extension replaces the property entry (if present)
        super.setProperty(name, value);
        return (C) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeExtension(String name) {
        if (extensionNames.remove(name)) {
            // Only remove the property if it was one of the existing extensions
            super.setProperty(name, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExtensions(Map<String, Object> extensions) {
        for (String name : Set.copyOf(extensionNames)) {
            if (!Extensions.isPrivateExtension(name)) {
                removeExtension(name);
            }
        }

        if (extensions == null || extensions.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> extension : extensions.entrySet()) {
            addExtension(extension.getKey(), extension.getValue());
        }
    }

    public Object getExtension(String name) {
        if (extensionNames.contains(name)) {
            return super.getProperty(name);
        }

        return null;
    }

    @Override
    protected boolean isExtension(String name) {
        return extensionNames.contains(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends BaseModel<C>> void merge(T other) {
        for (Entry<String, Object> property : other.properties.entrySet()) {
            String name = property.getKey();

            if (other.isExtension(name) && !isExtension(name)) {
                addExtension(name, property.getValue());
            }
        }

        super.merge(other);
    }
}
