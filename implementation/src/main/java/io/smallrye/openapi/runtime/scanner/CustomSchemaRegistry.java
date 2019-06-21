package io.smallrye.openapi.runtime.scanner;

/**
 * A simple registry that allows users to provide a custom schema for some types.
 *
 * @author michael.schnell@fuin.org
 */
public interface CustomSchemaRegistry {

    /**
     * Registers types with a custom schema.
     * 
     * @param registry Schema registry to add the custom type/schema combinations to.
     */
    public void registerCustomSchemas(SchemaRegistry registry);

}
