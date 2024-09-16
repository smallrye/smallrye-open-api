package io.smallrye.openapi.runtime.io.schema;

/**
 * Represents a desired data type that we might want to convert a JSON node to
 */
public class DataType {
    public static enum Type {
        OBJECT,
        LIST,
        MAP
    }

    public DataType.Type type;
    public DataType content;
    public Class<?> clazz;

    private DataType(DataType.Type type, DataType content, Class<?> clazz) {
        super();
        this.type = type;
        this.content = content;
        this.clazz = clazz;
    }

    public static DataType type(Class<?> clazz) {
        return new DataType(Type.OBJECT, null, clazz);
    }

    public static DataType listOf(DataType content) {
        return new DataType(Type.LIST, content, null);
    }

    public static DataType mapOf(DataType content) {
        return new DataType(Type.MAP, content, null);
    }
}