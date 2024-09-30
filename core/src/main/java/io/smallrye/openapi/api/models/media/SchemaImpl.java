package io.smallrye.openapi.api.models.media;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.models.media.Discriminator;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.XML;

import io.smallrye.openapi.api.models.ExtensibleImpl;
import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.api.models.ModelImpl;
import io.smallrye.openapi.api.util.MergeUtil;
import io.smallrye.openapi.runtime.io.ReferenceType;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 * An implementation of the {@link Schema} OpenAPI model interface.
 */
public class SchemaImpl extends ExtensibleImpl<Schema> implements Schema, ModelImpl {

    private String ref;
    private String format;
    private final String name;
    private String title;
    private String description;
    private Object defaultValue;
    private BigDecimal multipleOf;
    private BigDecimal maximum;
    private Boolean exclusiveMaximum;
    private BigDecimal minimum;
    private Boolean exclusiveMinimum;
    private Integer maxLength;
    private Integer minLength;
    private String pattern;
    private Integer maxItems;
    private Integer minItems;
    private Boolean uniqueItems;
    private Integer maxProperties;
    private Integer minProperties;
    private List<String> required;
    private List<Object> enumeration;
    private SchemaType type;
    private Schema items;
    private List<Schema> allOf;
    private Map<String, Schema> properties;
    private Schema additionalPropertiesSchema;
    private Boolean additionalPropertiesBoolean;
    private Boolean readOnly;
    private XML xml;
    private ExternalDocumentation externalDocs;
    private Object example;
    private List<Schema> oneOf;
    private List<Schema> anyOf;
    private Schema not;
    private Discriminator discriminator;
    private Boolean nullable;
    private Boolean writeOnly;
    private Boolean deprecated;

    // Non-standard
    private int modCount;
    private List<Schema> typeObservers;

    public static boolean isNamed(Schema schema) {
        return schema instanceof SchemaImpl && ((SchemaImpl) schema).name != null;
    }

    public static int getModCount(Schema schema) {
        return schema instanceof SchemaImpl ? ((SchemaImpl) schema).modCount : -1;
    }

    public static void addTypeObserver(Schema observable, Schema observer) {
        if (observable instanceof SchemaImpl) {
            SchemaImpl obs = (SchemaImpl) observable;
            obs.typeObservers = ModelUtil.add(observer, obs.typeObservers);
        }

        observer.setType(observable.getType());
    }

    public static SchemaImpl copyOf(Schema other) {
        SchemaImpl clone = (SchemaImpl) MergeUtil.mergeObjects(new SchemaImpl(), other);
        clone.required = copy(clone.required, () -> new ArrayList<>(clone.required));
        clone.enumeration = copy(clone.enumeration, () -> new ArrayList<>(clone.enumeration));
        clone.items = copy(clone.items, () -> copyOf(clone.items));

        clone.allOf = copy(clone.allOf, () -> clone.allOf
                .stream()
                .map(SchemaImpl::copyOf)
                .collect(Collectors.toList()));

        clone.properties = copy(clone.properties, () -> {
            Map<String, Schema> copiedProperties = new LinkedHashMap<>(clone.properties.size());
            clone.properties.forEach((k, v) -> copiedProperties.put(k, copyOf(v)));
            return copiedProperties;
        });

        clone.additionalPropertiesSchema = copy(clone.additionalPropertiesSchema,
                () -> copyOf(clone.additionalPropertiesSchema));

        clone.xml = copy(clone.xml, () -> MergeUtil.mergeObjects(new XMLImpl(), clone.xml));
        clone.externalDocs = copy(clone.externalDocs,
                () -> MergeUtil.mergeObjects(new ExternalDocumentationImpl(), clone.externalDocs));

        clone.oneOf = copy(clone.oneOf, () -> clone.oneOf
                .stream()
                .map(SchemaImpl::copyOf)
                .collect(Collectors.toList()));

        clone.anyOf = copy(clone.anyOf, () -> clone.anyOf
                .stream()
                .map(SchemaImpl::copyOf)
                .collect(Collectors.toList()));

        clone.not = copy(clone.not, () -> copyOf(clone.not));

        return clone;
    }

    private static <T> T copy(T property, Supplier<T> copySupplier) {
        if (property != null) {
            return copySupplier.get();
        }
        return null;
    }

    public SchemaImpl(String name) {
        this.name = name;
    }

    public SchemaImpl() {
        this(null);
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
        return this.ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.Reference#setRef(java.lang.String)
     */
    @Override
    public void setRef(String ref) {
        if (ref != null && !ref.contains("/")) {
            ref = ReferenceType.SCHEMA.referenceOf(ref);
        }
        incrementModCount();
        this.ref = ref;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDiscriminator()
     */
    @Override
    public Discriminator getDiscriminator() {
        return this.discriminator;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDiscriminator(org.eclipse.microprofile.openapi.models.media.Discriminator)
     */
    @Override
    public void setDiscriminator(Discriminator discriminator) {
        incrementModCount();
        this.discriminator = discriminator;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getTitle()
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        incrementModCount();
        this.title = title;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDefaultValue(java.lang.Object)
     */
    @Override
    public void setDefaultValue(Object defaultValue) {
        incrementModCount();
        this.defaultValue = defaultValue;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getEnumeration()
     */
    @Override
    public List<Object> getEnumeration() {
        return ModelUtil.unmodifiableList(this.enumeration);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setEnumeration(java.util.List)
     */
    @Override
    public void setEnumeration(List<Object> enumeration) {
        incrementModCount();
        this.enumeration = ModelUtil.replace(enumeration, ArrayList<Object>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addEnumeration(java.lang.Object)
     */
    @Override
    public Schema addEnumeration(Object enumeration) {
        incrementModCount();
        this.enumeration = ModelUtil.add(enumeration, this.enumeration, ArrayList<Object>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeEnumeration(Object)
     */
    @Override
    public void removeEnumeration(Object enumeration) {
        incrementModCount();
        ModelUtil.remove(this.enumeration, enumeration);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMultipleOf()
     */
    @Override
    public BigDecimal getMultipleOf() {
        return this.multipleOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMultipleOf(java.math.BigDecimal)
     */
    @Override
    public void setMultipleOf(BigDecimal multipleOf) {
        incrementModCount();
        this.multipleOf = multipleOf;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaximum()
     */
    @Override
    public BigDecimal getMaximum() {
        return this.maximum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaximum(java.math.BigDecimal)
     */
    @Override
    public void setMaximum(BigDecimal maximum) {
        incrementModCount();
        this.maximum = maximum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExclusiveMaximum()
     */
    @Override
    public Boolean getExclusiveMaximum() {
        return this.exclusiveMaximum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExclusiveMaximum(java.lang.Boolean)
     */
    @Override
    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        incrementModCount();
        this.exclusiveMaximum = exclusiveMaximum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinimum()
     */
    @Override
    public BigDecimal getMinimum() {
        return this.minimum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinimum(java.math.BigDecimal)
     */
    @Override
    public void setMinimum(BigDecimal minimum) {
        incrementModCount();
        this.minimum = minimum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExclusiveMinimum()
     */
    @Override
    public Boolean getExclusiveMinimum() {
        return this.exclusiveMinimum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExclusiveMinimum(java.lang.Boolean)
     */
    @Override
    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        incrementModCount();
        this.exclusiveMinimum = exclusiveMinimum;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxLength()
     */
    @Override
    public Integer getMaxLength() {
        return this.maxLength;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxLength(java.lang.Integer)
     */
    @Override
    public void setMaxLength(Integer maxLength) {
        incrementModCount();
        this.maxLength = maxLength;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinLength()
     */
    @Override
    public Integer getMinLength() {
        return this.minLength;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinLength(java.lang.Integer)
     */
    @Override
    public void setMinLength(Integer minLength) {
        incrementModCount();
        this.minLength = minLength;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getPattern()
     */
    @Override
    public String getPattern() {
        return this.pattern;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setPattern(java.lang.String)
     */
    @Override
    public void setPattern(String pattern) {
        incrementModCount();
        this.pattern = pattern;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxItems()
     */
    @Override
    public Integer getMaxItems() {
        return this.maxItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxItems(java.lang.Integer)
     */
    @Override
    public void setMaxItems(Integer maxItems) {
        incrementModCount();
        this.maxItems = maxItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinItems()
     */
    @Override
    public Integer getMinItems() {
        return this.minItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinItems(java.lang.Integer)
     */
    @Override
    public void setMinItems(Integer minItems) {
        incrementModCount();
        this.minItems = minItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getUniqueItems()
     */
    @Override
    public Boolean getUniqueItems() {
        return this.uniqueItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setUniqueItems(java.lang.Boolean)
     */
    @Override
    public void setUniqueItems(Boolean uniqueItems) {
        incrementModCount();
        this.uniqueItems = uniqueItems;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMaxProperties()
     */
    @Override
    public Integer getMaxProperties() {
        return this.maxProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMaxProperties(java.lang.Integer)
     */
    @Override
    public void setMaxProperties(Integer maxProperties) {
        incrementModCount();
        this.maxProperties = maxProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getMinProperties()
     */
    @Override
    public Integer getMinProperties() {
        return this.minProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setMinProperties(java.lang.Integer)
     */
    @Override
    public void setMinProperties(Integer minProperties) {
        incrementModCount();
        this.minProperties = minProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getRequired()
     */
    @Override
    public List<String> getRequired() {
        return ModelUtil.unmodifiableList(this.required);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setRequired(java.util.List)
     */
    @Override
    public void setRequired(List<String> required) {
        incrementModCount();
        this.required = ModelUtil.replace(required, ArrayList<String>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addRequired(java.lang.String)
     */
    @Override
    public Schema addRequired(String required) {
        incrementModCount();
        this.required = ModelUtil.add(required, this.required, ArrayList<String>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeRequired(String)
     */
    @Override
    public void removeRequired(String required) {
        incrementModCount();
        ModelUtil.remove(this.required, required);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getType()
     */
    @Override
    public SchemaType getType() {
        return this.type;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setType(org.eclipse.microprofile.openapi.models.media.Schema.SchemaType)
     */
    @Override
    public void setType(SchemaType type) {
        incrementModCount();
        this.type = type;

        if (typeObservers != null) {
            typeObservers.forEach(o -> o.setType(type));
        }
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getNot()
     */
    @Override
    public Schema getNot() {
        return this.not;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setNot(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setNot(Schema not) {
        incrementModCount();
        this.not = not;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getProperties()
     */
    @Override
    public Map<String, Schema> getProperties() {
        return ModelUtil.unmodifiableMap(this.properties);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setProperties(java.util.Map)
     */
    @Override
    public void setProperties(Map<String, Schema> properties) {
        incrementModCount();
        this.properties = ModelUtil.replace(properties, LinkedHashMap<String, Schema>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addProperty(java.lang.String,
     *      org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addProperty(String key, Schema propertySchema) {
        incrementModCount();
        this.properties = ModelUtil.add(key, propertySchema, this.properties, LinkedHashMap<String, Schema>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeProperty(java.lang.String)
     */
    @Override
    public void removeProperty(String key) {
        incrementModCount();
        ModelUtil.remove(this.properties, key);
    }

    @Override
    public Schema getAdditionalPropertiesSchema() {
        return this.additionalPropertiesSchema;
    }

    @Override
    public Boolean getAdditionalPropertiesBoolean() {
        return this.additionalPropertiesBoolean;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAdditionalPropertiesSchema(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setAdditionalPropertiesSchema(Schema additionalProperties) {
        incrementModCount();
        this.additionalPropertiesBoolean = null;
        this.additionalPropertiesSchema = additionalProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAdditionalPropertiesBoolean(java.lang.Boolean)
     */
    @Override
    public void setAdditionalPropertiesBoolean(Boolean additionalProperties) {
        incrementModCount();
        this.additionalPropertiesSchema = null;
        this.additionalPropertiesBoolean = additionalProperties;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        incrementModCount();
        this.description = description;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getFormat()
     */
    @Override
    public String getFormat() {
        return this.format;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setFormat(java.lang.String)
     */
    @Override
    public void setFormat(String format) {
        incrementModCount();
        this.format = format;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getNullable()
     */
    @Override
    public Boolean getNullable() {
        return this.nullable;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setNullable(java.lang.Boolean)
     */
    @Override
    public void setNullable(Boolean nullable) {
        incrementModCount();
        this.nullable = nullable;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getReadOnly()
     */
    @Override
    public Boolean getReadOnly() {
        return this.readOnly;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setReadOnly(java.lang.Boolean)
     */
    @Override
    public void setReadOnly(Boolean readOnly) {
        incrementModCount();
        this.readOnly = readOnly;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getWriteOnly()
     */
    @Override
    public Boolean getWriteOnly() {
        return this.writeOnly;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setWriteOnly(java.lang.Boolean)
     */
    @Override
    public void setWriteOnly(Boolean writeOnly) {
        incrementModCount();
        this.writeOnly = writeOnly;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExample()
     */
    @Override
    public Object getExample() {
        return this.example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExample(java.lang.Object)
     */
    @Override
    public void setExample(Object example) {
        incrementModCount();
        this.example = example;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getExternalDocs()
     */
    @Override
    public ExternalDocumentation getExternalDocs() {
        return this.externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setExternalDocs(org.eclipse.microprofile.openapi.models.ExternalDocumentation)
     */
    @Override
    public void setExternalDocs(ExternalDocumentation externalDocs) {
        incrementModCount();
        this.externalDocs = externalDocs;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getDeprecated()
     */
    @Override
    public Boolean getDeprecated() {
        return this.deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setDeprecated(java.lang.Boolean)
     */
    @Override
    public void setDeprecated(Boolean deprecated) {
        incrementModCount();
        this.deprecated = deprecated;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getXml()
     */
    @Override
    public XML getXml() {
        return this.xml;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setXml(org.eclipse.microprofile.openapi.models.media.XML)
     */
    @Override
    public void setXml(XML xml) {
        incrementModCount();
        this.xml = xml;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getItems()
     */
    @Override
    public Schema getItems() {
        return this.items;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setItems(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void setItems(Schema items) {
        incrementModCount();
        this.items = items;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getAllOf()
     */
    @Override
    public List<Schema> getAllOf() {
        return ModelUtil.unmodifiableList(this.allOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAllOf(java.util.List)
     */
    @Override
    public void setAllOf(List<Schema> allOf) {
        incrementModCount();
        this.allOf = ModelUtil.replace(allOf, ArrayList<Schema>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addAllOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addAllOf(Schema allOf) {
        incrementModCount();
        this.allOf = ModelUtil.add(allOf, this.allOf, ArrayList<Schema>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeAllOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void removeAllOf(Schema allOf) {
        incrementModCount();
        ModelUtil.remove(this.allOf, allOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getAnyOf()
     */
    @Override
    public List<Schema> getAnyOf() {
        return ModelUtil.unmodifiableList(this.anyOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setAnyOf(java.util.List)
     */
    @Override
    public void setAnyOf(List<Schema> anyOf) {
        incrementModCount();
        this.anyOf = ModelUtil.replace(anyOf, ArrayList<Schema>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addAnyOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addAnyOf(Schema anyOf) {
        incrementModCount();
        this.anyOf = ModelUtil.add(anyOf, this.anyOf, ArrayList<Schema>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeAnyOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void removeAnyOf(Schema anyOf) {
        incrementModCount();
        ModelUtil.remove(this.anyOf, anyOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#getOneOf()
     */
    @Override
    public List<Schema> getOneOf() {
        return ModelUtil.unmodifiableList(this.oneOf);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#setOneOf(java.util.List)
     */
    @Override
    public void setOneOf(List<Schema> oneOf) {
        incrementModCount();
        this.oneOf = ModelUtil.replace(oneOf, ArrayList<Schema>::new);
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#addOneOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public Schema addOneOf(Schema oneOf) {
        incrementModCount();
        this.oneOf = ModelUtil.add(oneOf, this.oneOf, ArrayList<Schema>::new);
        return this;
    }

    /**
     * @see org.eclipse.microprofile.openapi.models.media.Schema#removeOneOf(org.eclipse.microprofile.openapi.models.media.Schema)
     */
    @Override
    public void removeOneOf(Schema oneOf) {
        incrementModCount();
        ModelUtil.remove(this.oneOf, oneOf);
    }

}
