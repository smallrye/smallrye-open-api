package io.smallrye.openapi.api.models.media;

import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_ADDITIONAL_PROPERTIES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_ALL_OF;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_ANY_OF;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_COMMENT;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_CONST;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_CONTAINS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_CONTENT_ENCODING;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_CONTENT_MEDIA_TYPE;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_CONTENT_SCHEMA;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_DEFAULT;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_DEPENDENT_REQUIRED;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_DEPENDENT_SCHEMAS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_DEPRECATED;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_DESCRIPTION;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_DISCRIMINATOR;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_ELSE;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_ENUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_EXAMPLE;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_EXAMPLES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_EXCLUSIVE_MAXIMUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_EXCLUSIVE_MINIMUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_EXTERNAL_DOCS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_FORMAT;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_IF;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_ITEMS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MAXIMUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MAX_CONTAINS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MAX_ITEMS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MAX_LENGTH;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MAX_PROPERTIES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MINIMUM;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MIN_CONTAINS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MIN_ITEMS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MIN_LENGTH;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MIN_PROPERTIES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_MULTIPLE_OF;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_NOT;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_ONE_OF;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_PATTERN;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_PATTERN_PROPERTIES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_PREFIX_ITEMS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_PROPERTIES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_PROPERTY_NAMES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_READ_ONLY;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_REF;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_REQUIRED;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_SCHEMA_DIALECT;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_THEN;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_TYPE;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_UNEVALUATED_ITEMS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_UNEVALUATED_PROPERTIES;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_UNIQUE_ITEMS;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_WRITE_ONLY;
import static io.smallrye.openapi.runtime.io.schema.SchemaConstant.PROP_XML;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;

import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.api.models.MapBasedModelImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Schema} OpenAPI model interface.
 */
public class SchemaImpl extends MapBasedModelImpl implements Schema, ModelImpl {

    private static final Set<String> NON_MERGABLE_PROPERTIES = Collections.singleton(SchemaConstant.PROP_EXAMPLES);

    // Non-standard
    private String name;
    private int modCount;
    private List<Schema> typeObservers;
    // Value set via setNullable, unless overwritten by call to setType(List)
    private Boolean nullable = null;

    /**
     * The boolean value of this schema. {@code null} in most cases where the schema is an object
     */
    private Boolean booleanValue;

    @Override
    protected Set<String> getNonMergableCollections() {
        return NON_MERGABLE_PROPERTIES;
    }

    @Override
    public MapBasedModelImpl mergeFrom(MapBasedModelImpl other) {
        SchemaImpl otherSchema = null;

        // If either schema is a boolean, we don't merge and just return the other schema
        if (this.isBooleanSchema()) {
            return other;
        }
        if (other instanceof SchemaImpl && ((SchemaImpl) other).isBooleanSchema()) {
            return otherSchema;
        }

        // Otherwise we merge the trees
        return super.mergeFrom(other);
    }

    public static boolean isNamed(Schema schema) {
        return schema instanceof SchemaImpl && ((SchemaImpl) schema).name != null;
    }

    public static int getModCount(Schema schema) {
        return schema instanceof SchemaImpl ? ((SchemaImpl) schema).modCount : -1;
    }

    public static void addTypeObserver(Schema observable, Schema observer) {
        if (observable instanceof SchemaImpl) {
            SchemaImpl obs = (SchemaImpl) observable;
            obs.typeObservers = ModelUtil.add(observer, obs.typeObservers, ArrayList<Schema>::new);
        }

        setTypeRetainingNull(observer, observable.getType());
    }

    public static SchemaImpl copyOf(Schema other) {
        if (other == null) {
            return new SchemaImpl();
        }
        if (other instanceof SchemaImpl) {
            SchemaImpl otherImpl = (SchemaImpl) other;
            SchemaImpl result = new SchemaImpl();
            result.data.putAll(copyOf(otherImpl.data));
            return result;
        }
        throw new UnsupportedOperationException("Can't copy a different impl");
    }

    public static void clear(Schema schema) {
        SchemaImpl impl = (SchemaImpl) schema;
        impl.data.clear();
        impl.booleanValue = null;
    }

    private static <K, V> Map<K, V> copyOf(Map<K, V> map) {
        Map<K, V> clone = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            K key = entry.getKey();
            V val = entry.getValue();
            clone.put(key, copyOf(val));
        }
        return clone;
    }

    private static <T> List<T> copyOf(List<T> list) {
        List<T> clone = new ArrayList<>();
        for (T value : list) {
            clone.add(copyOf(value));
        }
        return clone;
    }

    private static <T> T copyOf(T value) {
        if (value instanceof Map) {
            return (T) copyOf((Map<?, ?>) value);
        } else if (value instanceof List) {
            return (T) copyOf((List<?>) value);
        } else if (value instanceof Schema) {
            return (T) copyOf((Schema) value);
        } else if (value instanceof XML) {
            return (T) MergeUtil.mergeObjects(new XMLImpl(), (XML) value);
        } else if (value instanceof ExternalDocumentation) {
            return (T) MergeUtil.mergeObjects(new ExternalDocumentationImpl(), (ExternalDocumentation) value);
        } else if (value instanceof Discriminator) {
            return (T) MergeUtil.mergeObjects(new DiscriminatorImpl(), (Discriminator) value);
        } else {
            return value;
        }
    }

    /**
     * Create an empty named schema
     *
     * @param name the name
     */
    public SchemaImpl(String name) {
        super();
        this.name = name;
    }

    /**
     * Create an empty schema
     */
    public SchemaImpl() {
        this((String) null);
    }

    public String getName() {
        return name;
    }

    private void incrementModCount() {
        modCount++;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#getRef()
     */
    @Override
    public String getRef() {
        return getProperty(PROP_REF, String.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = ReferenceType.SCHEMA.referenceOf(ref);
        }
        setProperty(PROP_REF, ref);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDiscriminator()
     */
    @Override
    public Discriminator getDiscriminator() {
        return getProperty(PROP_DISCRIMINATOR, Discriminator.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDiscriminator(org.eclipse.microprofile.openapi.models.media.Discriminator)
     */
    @Override
    public void setDiscriminator(Discriminator discriminator) {
        setProperty(PROP_DISCRIMINATOR, discriminator);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getTitle()
     */
    @Override
    public String getTitle() {
        return getProperty(SchemaConstant.PROP_TITLE, String.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        setProperty(SchemaConstant.PROP_TITLE, title);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return getProperty(PROP_DEFAULT, Object.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDefaultValue(java.lang.Object)
     */
    @Override
    public void setDefaultValue(Object defaultValue) {
        setProperty(SchemaConstant.PROP_DEFAULT, defaultValue);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getEnumeration()
     */
    @Override
    public List<Object> getEnumeration() {
        return getListProperty(PROP_ENUM);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setEnumeration(java.util.List)
     */
    @Override
    public void setEnumeration(List<Object> enumeration) {
        setListProperty(PROP_ENUM, enumeration);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addEnumeration(java.lang.Object)
     */
    @Override
    public Schema addEnumeration(Object enumeration) {
        addToListProperty(PROP_ENUM, enumeration);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeEnumeration(Object)
     */
    @Override
    public void removeEnumeration(Object enumeration) {
        removeFromListProperty(PROP_ENUM, enumeration);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMultipleOf()
     */
    @Override
    public BigDecimal getMultipleOf() {
        return getProperty(PROP_MULTIPLE_OF, BigDecimal.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMultipleOf(java.math.BigDecimal)
     */
    @Override
    public void setMultipleOf(BigDecimal multipleOf) {
        setProperty(PROP_MULTIPLE_OF, multipleOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaximum()
     */
    @Override
    public BigDecimal getMaximum() {
        return getProperty(PROP_MAXIMUM, BigDecimal.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaximum(java.math.BigDecimal)
     */
    @Override
    public void setMaximum(BigDecimal maximum) {
        setProperty(PROP_MAXIMUM, maximum);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExclusiveMaximum()
     */
    @Override
    public BigDecimal getExclusiveMaximum() {
        return getProperty(PROP_EXCLUSIVE_MAXIMUM, BigDecimal.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExclusiveMaximum(java.math.BigDecimal)
     */
    @Override
    public void setExclusiveMaximum(BigDecimal exclusiveMaximum) {
        setProperty(PROP_EXCLUSIVE_MAXIMUM, exclusiveMaximum);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinimum()
     */
    @Override
    public BigDecimal getMinimum() {
        return getProperty(PROP_MINIMUM, BigDecimal.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinimum(java.math.BigDecimal)
     */
    @Override
    public void setMinimum(BigDecimal minimum) {
        setProperty(PROP_MINIMUM, minimum);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExclusiveMinimum()
     */
    @Override
    public BigDecimal getExclusiveMinimum() {
        return getProperty(PROP_EXCLUSIVE_MINIMUM, BigDecimal.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExclusiveMinimum(java.math.BigDecimal)
     */
    @Override
    public void setExclusiveMinimum(BigDecimal exclusiveMinimum) {
        setProperty(PROP_EXCLUSIVE_MINIMUM, exclusiveMinimum);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxLength()
     */
    @Override
    public Integer getMaxLength() {
        return getProperty(PROP_MAX_LENGTH, Integer.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxLength(java.lang.Integer)
     */
    @Override
    public void setMaxLength(Integer maxLength) {
        setProperty(PROP_MAX_LENGTH, maxLength);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinLength()
     */
    @Override
    public Integer getMinLength() {
        return getProperty(PROP_MIN_LENGTH, Integer.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinLength(java.lang.Integer)
     */
    @Override
    public void setMinLength(Integer minLength) {
        setProperty(PROP_MIN_LENGTH, minLength);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getPattern()
     */
    @Override
    public String getPattern() {
        return getProperty(PROP_PATTERN, String.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setPattern(java.lang.String)
     */
    @Override
    public void setPattern(String pattern) {
        setProperty(PROP_PATTERN, pattern);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxItems()
     */
    @Override
    public Integer getMaxItems() {
        return getProperty(PROP_MAX_ITEMS, Integer.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxItems(java.lang.Integer)
     */
    @Override
    public void setMaxItems(Integer maxItems) {
        setProperty(PROP_MAX_ITEMS, maxItems);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinItems()
     */
    @Override
    public Integer getMinItems() {
        return getProperty(PROP_MIN_ITEMS, Integer.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinItems(java.lang.Integer)
     */
    @Override
    public void setMinItems(Integer minItems) {
        setProperty(PROP_MIN_ITEMS, minItems);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getUniqueItems()
     */
    @Override
    public Boolean getUniqueItems() {
        return getProperty(PROP_UNIQUE_ITEMS, Boolean.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setUniqueItems(java.lang.Boolean)
     */
    @Override
    public void setUniqueItems(Boolean uniqueItems) {
        setProperty(PROP_UNIQUE_ITEMS, uniqueItems);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxProperties()
     */
    @Override
    public Integer getMaxProperties() {
        return getProperty(PROP_MAX_PROPERTIES, Integer.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxProperties(java.lang.Integer)
     */
    @Override
    public void setMaxProperties(Integer maxProperties) {
        setProperty(PROP_MAX_PROPERTIES, maxProperties);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinProperties()
     */
    @Override
    public Integer getMinProperties() {
        return getProperty(PROP_MIN_PROPERTIES, Integer.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinProperties(java.lang.Integer)
     */
    @Override
    public void setMinProperties(Integer minProperties) {
        setProperty(PROP_MIN_PROPERTIES, minProperties);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getRequired()
     */
    @Override
    public List<String> getRequired() {
        return getListProperty(PROP_REQUIRED);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setRequired(java.util.List)
     */
    @Override
    public void setRequired(List<String> required) {
        setListProperty(PROP_REQUIRED, required);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addRequired(java.lang.String)
     */
    @Override
    public Schema addRequired(String required) {
        addToListProperty(PROP_REQUIRED, required);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeRequired(String)
     */
    @Override
    public void removeRequired(String required) {
        removeFromListProperty(PROP_REQUIRED, required);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getType()
     */
    @Override
    public List<SchemaType> getType() {
        List<SchemaType> resultList = getListProperty(PROP_TYPE);
        if (resultList != null) {
            return resultList;
        }

        SchemaType result = getProperty(PROP_TYPE, SchemaType.class);
        if (result != null) {
            return Collections.singletonList(result);
        }

        return null;
    }

    @Override
    public void setType(List<SchemaType> types) {
        nullable = null;
        setListProperty(PROP_TYPE, types);

        if (typeObservers != null) {
            typeObservers.forEach(o -> setTypeRetainingNull(o, types));
        }
    }

    private static void setTypeRetainingNull(Schema target, List<SchemaType> types) {
        // Set types on the observer, but retain null if it was set on the observer
        List<SchemaType> oldTypes = target.getType();
        if (oldTypes != null && types != null
                && oldTypes.contains(SchemaType.NULL)
                && !types.contains(SchemaType.NULL)) {
            types = new ArrayList<SchemaType>(types);
            types.add(SchemaType.NULL);
        }
        target.setType(types);
    }

    @Override
    public Schema addType(SchemaType type) {
        addToListProperty(PROP_TYPE, type);

        if (typeObservers != null) {
            typeObservers.forEach(o -> o.addType(type));
        }
        return this;
    }

    @Override
    public void removeType(SchemaType type) {
        removeFromListProperty(PROP_TYPE, type);

        if (typeObservers != null) {
            typeObservers.forEach(o -> o.removeType(type));
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setType(org.eclipse.microprofile.openapi.models.media.Schema.SchemaType)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void setType(SchemaType type) {
        incrementModCount();
        List<SchemaType> currentValue = getListProperty(PROP_TYPE);
        if (currentValue != null && currentValue.contains(SchemaType.NULL)) {
            if (type == null) {
                setListProperty(PROP_TYPE, Arrays.asList(SchemaType.NULL));
            } else {
                setListProperty(PROP_TYPE, Arrays.asList(type, SchemaType.NULL));
            }
        } else {
            if (type == null) {
                setListProperty(PROP_TYPE, null);
            } else {
                setListProperty(PROP_TYPE, Collections.singletonList(type));
            }
        }

        if (typeObservers != null) {
            typeObservers.forEach(o -> o.setType(type));
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getNot()
     */
    @Override
    public Schema getNot() {
        return getProperty(PROP_NOT, Schema.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setNot(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setNot(Schema not) {
        setProperty(PROP_NOT, not);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getProperties()
     */
    @Override
    public Map<String, Schema> getProperties() {
        return getMapProperty(PROP_PROPERTIES);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setProperties(java.util.Map)
     */
    @Override
    public void setProperties(Map<String, Schema> properties) {
        setMapProperty(PROP_PROPERTIES, properties);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addProperty(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addProperty(String key, Schema propertySchema) {
        addToMapProperty(PROP_PROPERTIES, key, propertySchema);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeProperty(java.lang.String)
     */
    @Override
    public void removeProperty(String key) {
        removeFromMapProperty(PROP_PROPERTIES, key);
    }

    @Override
    public Schema getAdditionalPropertiesSchema() {
        return getProperty(PROP_ADDITIONAL_PROPERTIES, Schema.class);
    }

    @Override
    public Boolean getAdditionalPropertiesBoolean() {
        Schema additionalPropertiesSchema = getAdditionalPropertiesSchema();
        return additionalPropertiesSchema == null ? null : additionalPropertiesSchema.getBooleanSchema();
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAdditionalPropertiesSchema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setAdditionalPropertiesSchema(Schema additionalProperties) {
        setProperty(PROP_ADDITIONAL_PROPERTIES, additionalProperties);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAdditionalPropertiesBoolean(java.lang.Boolean)
     */
    @Override
    public void setAdditionalPropertiesBoolean(Boolean additionalProperties) {
        if (additionalProperties != null) {
            setAdditionalPropertiesSchema(new SchemaImpl().booleanSchema(additionalProperties));
        } else {
            setAdditionalPropertiesSchema(null);
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDescription()
     */
    @Override
    public String getDescription() {
        return getProperty(PROP_DESCRIPTION, String.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        setProperty(PROP_DESCRIPTION, description);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getFormat()
     */
    @Override
    public String getFormat() {
        return getProperty(PROP_FORMAT, String.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setFormat(java.lang.String)
     */
    @Override
    public void setFormat(String format) {
        setProperty(PROP_FORMAT, format);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getNullable()
     */
    @Override
    public Boolean getNullable() {
        List<SchemaType> types = getType();
        if (types != null) {
            boolean nullPermitted = types.contains(SchemaType.NULL);
            // Retain old tri-state behaviour of getNullable
            // If setNullable has not been called and null is not permitted, return null rather than false
            if (!nullPermitted && nullable == null) {
                return null;
            } else {
                return nullPermitted;
            }
        } else {
            // If types is unset, return any value passed to setNullable
            return nullable;
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setNullable(java.lang.Boolean)
     */
    @Override
    public void setNullable(Boolean nullable) {
        incrementModCount();
        this.nullable = nullable;
        if (nullable == Boolean.TRUE) {
            List<SchemaType> types = getType();
            if (types == null || !types.contains(SchemaType.NULL)) {
                addType(SchemaType.NULL);
            }
        } else {
            removeType(SchemaType.NULL);
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getReadOnly()
     */
    @Override
    public Boolean getReadOnly() {
        return getProperty(PROP_READ_ONLY, Boolean.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setReadOnly(java.lang.Boolean)
     */
    @Override
    public void setReadOnly(Boolean readOnly) {
        setProperty(PROP_READ_ONLY, readOnly);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getWriteOnly()
     */
    @Override
    public Boolean getWriteOnly() {
        return getProperty(PROP_WRITE_ONLY, Boolean.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setWriteOnly(java.lang.Boolean)
     */
    @Override
    public void setWriteOnly(Boolean writeOnly) {
        setProperty(PROP_WRITE_ONLY, writeOnly);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExample()
     */
    @Override
    public Object getExample() {
        return getProperty(PROP_EXAMPLE, Object.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExample(java.lang.Object)
     */
    @Override
    public void setExample(Object example) {
        setProperty(PROP_EXAMPLE, example);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return getProperty(PROP_EXTERNAL_DOCS, ExternalDocumentation.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExternalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        setProperty(PROP_EXTERNAL_DOCS, externalDocs);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return getProperty(PROP_DEPRECATED, Boolean.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDeprecated(java.lang.Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        setProperty(PROP_DEPRECATED, deprecated);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getXml()
     */
    @Override
    public XML getXml() {
        return getProperty(PROP_XML, XML.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setXml(org.eclipse.microprofile.openapi.models.media.XML)
     */
    @Override
    public void setXml(XML xml) {
        setProperty(PROP_XML, xml);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getItems()
     */
    @Override
    public Schema getItems() {
        return getProperty(PROP_ITEMS, Schema.class);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setItems(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setItems(Schema items) {
        setProperty(PROP_ITEMS, items);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getAllOf()
     */
    @Override
    public List<Schema> getAllOf() {
        return getListProperty(PROP_ALL_OF);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAllOf(java.util.List)
     */
    @Override
    public void setAllOf(List<Schema> allOf) {
        setListProperty(PROP_ALL_OF, allOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addAllOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addAllOf(Schema allOf) {
        addToListProperty(PROP_ALL_OF, allOf);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeAllOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void removeAllOf(Schema allOf) {
        removeFromListProperty(PROP_ALL_OF, allOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getAnyOf()
     */
    @Override
    public List<Schema> getAnyOf() {
        return getListProperty(PROP_ANY_OF);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAnyOf(java.util.List)
     */
    @Override
    public void setAnyOf(List<Schema> anyOf) {
        setListProperty(PROP_ANY_OF, anyOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addAnyOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addAnyOf(Schema anyOf) {
        addToListProperty(PROP_ANY_OF, anyOf);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeAnyOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void removeAnyOf(Schema anyOf) {
        removeFromListProperty(PROP_ANY_OF, anyOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getOneOf()
     */
    @Override
    public List<Schema> getOneOf() {
        return getListProperty(PROP_ONE_OF);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setOneOf(java.util.List)
     */
    @Override
    public void setOneOf(List<Schema> oneOf) {
        setListProperty(PROP_ONE_OF, oneOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addOneOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addOneOf(Schema oneOf) {
        addToListProperty(PROP_ONE_OF, oneOf);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeOneOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void removeOneOf(Schema oneOf) {
        removeFromListProperty(PROP_ONE_OF, oneOf);
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> extensions = new HashMap<>();
        data.forEach((k, v) -> {
            if (k.startsWith("x-")) {
                extensions.put(k, v);
            }
        });
        return extensions;
    }

    @Override
    public Schema addExtension(String name, Object value) {
        setProperty(name, value);
        return this;
    }

    @Override
    public void removeExtension(String name) {
        setProperty(name, null);
    }

    @Override
    public void setExtensions(Map<String, Object> extensions) {
        assertObjectSchema();

        // Remove all extension fields
        data.keySet().removeIf(k -> k.startsWith("x-"));

        // Add all the new extensions
        if (extensions != null) {
            extensions.forEach((k, v) -> {
                k = k.startsWith("x-") ? k : "x-" + k;
                setProperty(k, v);
            });
        }
    }

    @Override
    public String getSchemaDialect() {
        return getProperty(PROP_SCHEMA_DIALECT, String.class);
    }

    @Override
    public void setSchemaDialect(String schemaDialect) {
        setProperty(PROP_SCHEMA_DIALECT, schemaDialect);
    }

    @Override
    public String getComment() {
        return getProperty(PROP_COMMENT, String.class);
    }

    @Override
    public void setComment(String comment) {
        setProperty(PROP_COMMENT, comment);
    }

    @Override
    public Schema getIfSchema() {
        return getProperty(PROP_IF, Schema.class);
    }

    @Override
    public void setIfSchema(Schema ifSchema) {
        setProperty(PROP_IF, ifSchema);
    }

    @Override
    public Schema getThenSchema() {
        return getProperty(PROP_THEN, Schema.class);
    }

    @Override
    public void setThenSchema(Schema thenSchema) {
        setProperty(PROP_THEN, thenSchema);
    }

    @Override
    public Schema getElseSchema() {
        return getProperty(PROP_ELSE, Schema.class);
    }

    @Override
    public void setElseSchema(Schema elseSchema) {
        setProperty(PROP_ELSE, elseSchema);
    }

    @Override
    public Map<String, Schema> getDependentSchemas() {
        return getMapProperty(PROP_DEPENDENT_SCHEMAS);
    }

    @Override
    public void setDependentSchemas(Map<String, Schema> dependentSchemas) {
        setMapProperty(PROP_DEPENDENT_SCHEMAS, dependentSchemas);
    }

    @Override
    public Schema addDependentSchema(String propertyName, Schema schema) {
        addToMapProperty(PROP_DEPENDENT_SCHEMAS, propertyName, schema);
        return this;
    }

    @Override
    public void removeDependentSchema(String propertyName) {
        removeFromMapProperty(PROP_DEPENDENT_SCHEMAS, propertyName);
    }

    @Override
    public List<Schema> getPrefixItems() {
        return getListProperty(PROP_PREFIX_ITEMS);
    }

    @Override
    public void setPrefixItems(List<Schema> prefixItems) {
        setListProperty(PROP_PREFIX_ITEMS, prefixItems);
    }

    @Override
    public Schema addPrefixItem(Schema prefixItem) {
        addToListProperty(PROP_PREFIX_ITEMS, prefixItem);
        return this;
    }

    @Override
    public void removePrefixItem(Schema prefixItem) {
        removeFromListProperty(PROP_PREFIX_ITEMS, prefixItem);
    }

    @Override
    public Schema getContains() {
        return getProperty(PROP_CONTAINS, Schema.class);
    }

    @Override
    public void setContains(Schema contains) {
        setProperty(PROP_CONTAINS, contains);
    }

    @Override
    public Map<String, Schema> getPatternProperties() {
        return getMapProperty(PROP_PATTERN_PROPERTIES);
    }

    @Override
    public void setPatternProperties(Map<String, Schema> patternProperties) {
        setMapProperty(PROP_PATTERN_PROPERTIES, patternProperties);
    }

    @Override
    public Schema addPatternProperty(String regularExpression, Schema schema) {
        addToMapProperty(PROP_PATTERN_PROPERTIES, regularExpression, schema);
        return this;
    }

    @Override
    public void removePatternProperty(String regularExpression) {
        removeFromMapProperty(PROP_PATTERN_PROPERTIES, regularExpression);
    }

    @Override
    public Schema getPropertyNames() {
        return getProperty(PROP_PROPERTY_NAMES, Schema.class);
    }

    @Override
    public void setPropertyNames(Schema propertyNameSchema) {
        setProperty(PROP_PROPERTY_NAMES, propertyNameSchema);
    }

    @Override
    public Schema getUnevaluatedItems() {
        return getProperty(PROP_UNEVALUATED_ITEMS, Schema.class);
    }

    @Override
    public void setUnevaluatedItems(Schema unevaluatedItems) {
        setProperty(PROP_UNEVALUATED_ITEMS, unevaluatedItems);
    }

    @Override
    public Schema getUnevaluatedProperties() {
        return getProperty(PROP_UNEVALUATED_PROPERTIES, Schema.class);
    }

    @Override
    public void setUnevaluatedProperties(Schema unevaluatedProperties) {
        setProperty(PROP_UNEVALUATED_PROPERTIES, unevaluatedProperties);
    }

    @Override
    public Object getConstValue() {
        return getProperty(PROP_CONST, Object.class);
    }

    @Override
    public void setConstValue(Object constValue) {
        setProperty(PROP_CONST, constValue);
    }

    @Override
    public Integer getMaxContains() {
        return getProperty(PROP_MAX_CONTAINS, Integer.class);
    }

    @Override
    public void setMaxContains(Integer maxContains) {
        setProperty(PROP_MAX_CONTAINS, maxContains);
    }

    @Override
    public Integer getMinContains() {
        return getProperty(PROP_MIN_CONTAINS, Integer.class);
    }

    @Override
    public void setMinContains(Integer minContains) {
        setProperty(PROP_MIN_CONTAINS, minContains);
    }

    @Override
    public Map<String, List<String>> getDependentRequired() {
        return getMapProperty(PROP_DEPENDENT_REQUIRED);
    }

    @Override
    public void setDependentRequired(Map<String, List<String>> dependentRequired) {
        setMapProperty(PROP_DEPENDENT_REQUIRED, dependentRequired);
    }

    @Override
    public Schema addDependentRequired(String propertyName, List<String> additionalRequiredPropertyNames) {
        addToMapProperty(PROP_DEPENDENT_REQUIRED, propertyName, additionalRequiredPropertyNames);
        return this;
    }

    @Override
    public void removeDependentRequired(String propertyName) {
        removeFromMapProperty(PROP_DEPENDENT_REQUIRED, propertyName);
    }

    @Override
    public String getContentEncoding() {
        return getProperty(PROP_CONTENT_ENCODING, String.class);
    }

    @Override
    public void setContentEncoding(String contentEncoding) {
        setProperty(PROP_CONTENT_ENCODING, contentEncoding);
    }

    @Override
    public String getContentMediaType() {
        return getProperty(PROP_CONTENT_MEDIA_TYPE, String.class);
    }

    @Override
    public void setContentMediaType(String contentMediaType) {
        setProperty(PROP_CONTENT_MEDIA_TYPE, contentMediaType);
    }

    @Override
    public Schema getContentSchema() {
        return getProperty(PROP_CONTENT_SCHEMA, Schema.class);
    }

    @Override
    public void setContentSchema(Schema contentSchema) {
        setProperty(PROP_CONTENT_SCHEMA, contentSchema);
    }

    @Override
    public Boolean getBooleanSchema() {
        return booleanValue;
    }

    @Override
    public void setBooleanSchema(Boolean booleanSchema) {
        incrementModCount();
        booleanValue = booleanSchema;
    }

    @Override
    public List<Object> getExamples() {
        return getListProperty(PROP_EXAMPLES);
    }

    @Override
    public void setExamples(List<Object> examples) {
        setListProperty(PROP_EXAMPLES, examples);
    }

    @Override
    public Schema addExample(Object example) {
        addToListProperty(PROP_EXAMPLES, example);
        return this;
    }

    @Override
    public void removeExample(Object example) {
        removeFromListProperty(PROP_EXAMPLES, example);
    }

    private boolean isBooleanSchema() {
        return booleanValue != null;
    }

    /**
     * Asserts that the schema is not a boolean schema
     *
     * @throws UnsupportedOperationException if this schema is a boolean schema
     */
    private void assertObjectSchema() throws UnsupportedOperationException {
        if (isBooleanSchema()) {
            throw new UnsupportedOperationException("Schema has a boolean value");
        }
    }

    @Override
    protected <T> void setProperty(String propertyName, T value) {
        assertObjectSchema();
        incrementModCount();
        super.setProperty(propertyName, value);
    }

    @Override
    protected <T> T getProperty(String propertyName, Class<T> clazz) {
        if (isBooleanSchema()) {
            return null;
        }
        return super.getProperty(propertyName, clazz);
    }

    @Override
    protected <T> List<T> getListProperty(String propertyName) {
        if (isBooleanSchema()) {
            return null;
        }
        return super.getListProperty(propertyName);
    }

    @Override
    protected <T> void setListProperty(String propertyName, List<T> value) {
        assertObjectSchema();
        incrementModCount();
        super.setListProperty(propertyName, value);
    }

    @Override
    protected <T> void addToListProperty(String propertyName, T value) {
        assertObjectSchema();
        incrementModCount();
        super.addToListProperty(propertyName, value);
    }

    @Override
    protected <T> void removeFromListProperty(String propertyName, T toRemove) {
        if (!isBooleanSchema()) {
            incrementModCount();
            super.removeFromListProperty(propertyName, toRemove);
        }
    }

    @Override
    protected <T> void setMapProperty(String propertyName, Map<String, T> value) {
        assertObjectSchema();
        incrementModCount();
        super.setMapProperty(propertyName, value);
    }

    @Override
    protected <T> Map<String, T> getMapProperty(String propertyName) {
        if (isBooleanSchema()) {
            return null;
        }
        return super.getMapProperty(propertyName);
    }

    @Override
    protected <T> void addToMapProperty(String propertyName, String key, T value) {
        assertObjectSchema();
        incrementModCount();
        super.addToMapProperty(propertyName, key, value);
    }

    @Override
    protected <T> void removeFromMapProperty(String propertyName, String key) {
        if (!isBooleanSchema()) {
            incrementModCount();
            super.removeFromMapProperty(propertyName, key);
        }
    }

    @Override
    public Object get(String propertyName) {
        return getProperty(propertyName, Object.class);
    }

    @Override
    public Schema set(String propertyName, Object value) {
        setProperty(propertyName, value);
        return this;
    }

    @Override
    public Map<String, ?> getAll() {
        return Collections.unmodifiableMap(data);
    }

    @Override
    public void setAll(Map<String, ?> allProperties) {
        incrementModCount();
        data.clear();
        if (allProperties != null) {
            allProperties.forEach(this::setProperty);
        }
    }

}
