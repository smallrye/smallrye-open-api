package io.smallrye.openapi.model;

/**
 * Represents a desired data type that we might want to convert a JSON node to
 */
public class DataType {

    public enum Type {
        OBJECT,
        LIST,
        MAP
    }

    public final DataType.Type type;
    public final DataType content;
    public final Class<?> clazz;

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
