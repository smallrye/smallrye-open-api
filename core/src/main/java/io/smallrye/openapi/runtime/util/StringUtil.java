package io.smallrye.openapi.runtime.util;

public class StringUtil {
    static final String EMPTY_STRING = "";

    public static boolean isNotEmpty(String stringVal) {
        return !(stringVal == null || EMPTY_STRING.equals(stringVal.trim()));
    }
}
