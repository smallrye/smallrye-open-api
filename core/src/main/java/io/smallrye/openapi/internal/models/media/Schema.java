package io.smallrye.openapi.internal.models.media;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.smallrye.openapi.model.BaseModel;
import io.smallrye.openapi.model.Extensions;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;

public class Schema extends AbstractSchema {

    private static final Map<String, MergeDirective> MERGE = Map.of("examples", MergeDirective.OVERRIDE_VALUE);

    @Override
    protected MergeDirective mergeDirective(String name) {
        return MERGE.getOrDefault(name, MergeDirective.MERGE_VALUES);
    }

    @Override
    public <T extends BaseModel<org.eclipse.microprofile.openapi.models.media.Schema>> void merge(T other) {
        // If either schema is a boolean, we don't merge and just return the other schema
        if (this.isBooleanSchema() || other.constructible().getBooleanSchema() != null) {
            return;
        }

        super.merge(other);
    }

    @SuppressWarnings("unchecked")
    List<SchemaType> getTypeList() {
        if (isExtension("type")) {
            return null; // NOSONAR
        }
        return super.getProperty("type", List.class);
    }

    void setTypeList(List<SchemaType> types) {
        // bypass additional processing in #setType(List<SchemaType>)
        super.setType(types);
    }

    @Override
    public void setType(List<SchemaType> types) {
        super.setType(types);
        Extensions.setPrivateExtension(this, "nullable", null);
        SchemaSupport.notifyTypeObservers(this, o -> SchemaSupport.setTypesRetainingNull(o, types));
    }

    @Override
    public Schema addType(SchemaType type) {
        super.addType(type);
        SchemaSupport.notifyTypeObservers(this, o -> o.addType(type));
        return this;
    }

    @Override
    public void removeType(SchemaType type) {
        super.removeType(type);
        SchemaSupport.notifyTypeObservers(this, o -> o.removeType(type));
    }

    @Override
    public Boolean getAdditionalPropertiesBoolean() {
        var schema = getAdditionalPropertiesSchema();
        return schema == null ? null : schema.getBooleanSchema();
    }

    @Override
    public void setAdditionalPropertiesBoolean(Boolean additionalProperties) {
        if (additionalProperties != null) {
            setAdditionalPropertiesSchema(new Schema().booleanSchema(additionalProperties));
        } else {
            setAdditionalPropertiesSchema(null);
        }
    }

    @Override
    public Boolean getBooleanSchema() {
        return Extensions.getPrivateExtension(this, "boolean-schema", Boolean.class);
    }

    @Override
    public void setBooleanSchema(Boolean booleanSchema) {
        Extensions.setPrivateExtension(this, "boolean-schema", booleanSchema);
    }

    @Override
    public Object get(String propertyName) {
        return getProperty(propertyName, Object.class);
    }

    @Override
    public org.eclipse.microprofile.openapi.models.media.Schema set(String propertyName, Object value) {
        setProperty(propertyName, value);
        return this;
    }

    @Override
    public Map<String, ?> getAll() {
        return super.getAllProperties();
    }

    @Override
    public void setAll(Map<String, ?> allProperties) {
        super.setAllProperties(allProperties);
    }

    @Override
    public <T> void setProperty(String propertyName, T value) {
        assertObjectSchema();
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
    protected boolean isExtension(String name) {
        String dialect = getSchemaDialect();

        if (dialect != null
                && !SchemaConstant.DIALECT_OAS31.equals(dialect)
                && !SchemaConstant.DIALECT_JSON_2020_12.equals(dialect)) {
            // Do not consider any properties from unknown dialects as extensions
            return false;
        }

        return !SchemaConstant.PROP_NAME.equals(name)
                && !SchemaConstant.PROP_TYPE.equals(name)
                && !SchemaConstant.PROP_REF.equals(name)
                && !SchemaConstant.PROPERTIES_DATA_TYPES.containsKey(name);
    }

    private boolean isBooleanSchema() {
        return Objects.nonNull(getBooleanSchema());
    }

    private void assertObjectSchema() throws UnsupportedOperationException {
        if (isBooleanSchema()) {
            throw new UnsupportedOperationException("Schema has a boolean value");
        }
    }

}
