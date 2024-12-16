package io.smallrye.openapi.model;

public interface PropertyMetadata {

    DataType getPropertyType(String name);

    OpenApiVersion getMinVersion(String name);
}
