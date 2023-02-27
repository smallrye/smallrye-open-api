package io.smallrye.openapi.runtime.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectWriter {

    private ObjectWriter() {
    }

    /**
     * Writes an array of strings to the parent node.
     *
     * @param parent the parent json node
     * @param models list of Strings
     * @param propertyName the name of the node
     */
    public static void writeStringArray(ObjectNode parent, List<String> models, String propertyName) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(propertyName);
        for (String model : models) {
            node.add(model);
        }
    }

    /**
     * Writes an array of objects to the parent node.
     *
     * @param parent the parent json node
     * @param models list of objects
     * @param propertyName the name of the node
     */
    public static void writeObjectArray(ObjectNode parent, List<Object> models, String propertyName) {
        if (models == null) {
            return;
        }
        ArrayNode node = parent.putArray(propertyName);
        for (Object model : models) {
            addObject(node, model);
        }
    }

    /**
     * Writes a map of strings to the parent node.
     *
     * @param parent the parent json node
     * @param models map of strings
     * @param propertyName name of the node
     */
    public static void writeStringMap(ObjectNode parent, Map<String, String> models, String propertyName) {
        if (models == null) {
            return;
        }
        ObjectNode node = parent.putObject(propertyName);
        for (Map.Entry<String, String> entry : models.entrySet()) {
            node.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Write an object to json
     *
     * @param node the json node
     * @param key key
     * @param value value
     */
    public static void writeObject(ObjectNode node, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            node.put(key, (String) value);
        } else if (value instanceof JsonNode) {
            node.set(key, (JsonNode) value);
        } else if (value instanceof BigDecimal) {
            node.put(key, (BigDecimal) value);
        } else if (value instanceof BigInteger) {
            node.put(key, new BigDecimal((BigInteger) value));
        } else if (value instanceof Boolean) {
            node.put(key, (Boolean) value);
        } else if (value instanceof Double) {
            node.put(key, (Double) value);
        } else if (value instanceof Float) {
            node.put(key, (Float) value);
        } else if (value instanceof Integer) {
            node.put(key, (Integer) value);
        } else if (value instanceof Long) {
            node.put(key, (Long) value);
        } else if (value instanceof List) {
            ArrayNode array = node.putArray(key);
            for (Object valueItem : List.class.cast(value)) {
                addObject(array, valueItem);
            }
        } else if (value instanceof Map) {
            ObjectNode objNode = node.putObject(key);
            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String propertyName = entry.getKey();
                writeObject(objNode, propertyName, entry.getValue());
            }
        } else {
            node.put(key, (String) null);
        }
    }

    private static void addObject(ArrayNode node, Object value) {
        if (value instanceof String) {
            node.add((String) value);
        } else if (value instanceof JsonNode) {
            node.add((JsonNode) value);
        } else if (value instanceof BigDecimal) {
            node.add((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            node.add(new BigDecimal((BigInteger) value));
        } else if (value instanceof Boolean) {
            node.add((Boolean) value);
        } else if (value instanceof Double) {
            node.add((Double) value);
        } else if (value instanceof Float) {
            node.add((Float) value);
        } else if (value instanceof Integer) {
            node.add((Integer) value);
        } else if (value instanceof Long) {
            node.add((Long) value);
        } else if (value instanceof List) {
            ArrayNode array = node.addArray();
            for (Object valueItem : List.class.cast(value)) {
                addObject(array, valueItem);
            }
        } else if (value instanceof Map) {
            ObjectNode objNode = node.addObject();
            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String propertyName = entry.getKey();
                writeObject(objNode, propertyName, entry.getValue());
            }
        } else {
            node.add((String) null);
        }
    }
}
