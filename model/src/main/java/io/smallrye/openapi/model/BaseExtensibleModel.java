package io.smallrye.openapi.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Constructible;
import org.eclipse.microprofile.openapi.models.Extensible;

public abstract class BaseExtensibleModel<C extends Extensible<C> & Constructible> extends BaseModel<C>
        implements Extensible<C> {

    private static final Set<String> INITIAL_SET = Collections.emptySet();

    private Set<String> extensionNames = INITIAL_SET;

    protected BaseExtensibleModel() {
    }

    private void assertNotExtension(String name, String messageTemplate) {
        if (isExtension(name) || extensionNames.contains(name)) {
            throw new IllegalArgumentException(String.format(messageTemplate, name));
        }
    }

    private <P> boolean maybeSetExtension(String name, P value) {
        if (isExtension(name)) {
            addExtension(name, value);
            return true;
        } else {
            extensionNames.remove(name);
            return false;
        }
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

    @Override
    void setUnmodifiable() {
        super.setUnmodifiable();
        extensionNames = Collections.unmodifiableSet(extensionNames);
    }

    @Override
    @SuppressWarnings("unchecked")
    public C filter(OASFilter filter, Map<Object, Object> stack) {
        if (filter instanceof Extensions.ExtensionRemovalFilter) {
            ((Extensions.ExtensionRemovalFilter) filter).filterExtensible((C) this);
        }
        return super.filter(filter, stack);
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
    public <P> void setProperty(String name, P value) {
        if (!maybeSetExtension(name, value)) {
            super.setProperty(name, value);
        }
    }

    @Override
    protected <V> void setListProperty(String name, List<V> value) {
        if (!maybeSetExtension(name, value)) {
            super.setListProperty(name, value);
        }
    }

    @Override
    protected <V> void addListPropertyEntry(String name, V value) {
        assertNotExtension(name, "Property %s is an extension and cannot be modified as a List");
        super.addListPropertyEntry(name, value);
    }

    @Override
    protected <V> void removeListPropertyEntry(String name, V value) {
        assertNotExtension(name, "Property %s is an extension and cannot be modified as a List");
        super.removeListPropertyEntry(name, value);
    }

    @Override
    protected <V> void setMapProperty(String name, Map<String, V> value) {
        if (!maybeSetExtension(name, value)) {
            super.setMapProperty(name, value);
        }
    }

    @Override
    protected <V> void putMapPropertyEntry(String name, String key, V value) {
        assertNotExtension(name, "Property %s is an extension and cannot be modified as a Map");
        super.putMapPropertyEntry(name, key, value);
    }

    @Override
    protected void removeMapPropertyEntry(String name, String key) {
        assertNotExtension(name, "Property %s is an extension and cannot be modified as a Map");
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

    @Override
    public boolean hasExtension(String name) {
        return extensionNames.contains(name);
    }

    @Override
    public Object getExtension(String name) {
        if (hasExtension(name)) {
            return super.getProperty(name);
        }

        return null;
    }

    @Override
    protected boolean isExtension(String name) {
        return name.startsWith("x-");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends BaseModel<C>> void merge(T other) {
        for (Entry<String, Object> property : other.getModelProperties().entrySet()) {
            String name = property.getKey();

            if (other.isExtension(name) && !isExtension(name)) {
                addExtension(name, property.getValue());
            }
        }

        super.merge(other);
    }
}
