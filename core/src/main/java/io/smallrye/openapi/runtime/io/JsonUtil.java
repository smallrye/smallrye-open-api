package io.smallrye.openapi.runtime.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities methods for reading information from a Json Tree.
 * 
 * @author eric.wittmann@gmail.com
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNodeFactory factory = JsonNodeFactory.instance;

    public static ObjectNode objectNode() {
        return factory.objectNode();
    }

    public static ArrayNode arrayNode() {
        return factory.arrayNode();
    }

    /**
     * Constructor.
     */
    private JsonUtil() {
    }

    /**
     * Extract a string property from the given json tree. Returns null if no
     * property exists or is not a text node.
     * 
     * @param node JsonNode
     * @param propertyName Property to extract
     * @return String holding the value found for the property
     */
    public static String stringProperty(JsonNode node, String propertyName) {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode != null) {
            return propertyNode.asText();
        }
        return null;
    }

    /**
     * Sets the value of a property for a given json node. If the value is null,
     * then the property is not written.
     * 
     * @param node ObjectNode
     * @param propertyName Property to be set
     * @param propertyValue Value to be set
     */
    public static void stringProperty(ObjectNode node, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            return;
        }
        node.set(propertyName, factory.textNode(propertyValue));
    }

    /**
     * Sets the value of a property for a given json node. If the value is null,
     * then the property is not written.
     * 
     * @param node ObjectNode
     * @param propertyName Property to be set
     * @param propertyValue Value to be set
     * @param <E> Type of the property value
     */
    public static <E extends Enum<E>> void enumProperty(ObjectNode node, String propertyName, E propertyValue) {
        if (propertyValue == null) {
            return;
        }
        node.set(propertyName, factory.textNode(propertyValue.toString()));
    }

    /**
     * Extract a boolean property from the given json tree. Returns null if no
     * property exists or is not a boolean node.
     * 
     * @param node JsonNode
     * @param propertyName Property to extract
     * @return Boolean containing the value extracted
     */
    public static Optional<Boolean> booleanProperty(JsonNode node, String propertyName) {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode != null) {
            return Optional.of(propertyNode.asBoolean());
        }
        return Optional.empty();
    }

    /**
     * Sets the value of a property for a given json node. If the value is null,
     * then the property is not written.
     * 
     * @param node ObjectNode
     * @param propertyName Property to be set
     * @param propertyValue Boolean value to be set
     */
    public static void booleanProperty(ObjectNode node, String propertyName, Boolean propertyValue) {
        if (propertyValue == null) {
            return;
        }
        node.set(propertyName, factory.booleanNode(propertyValue));
    }

    /**
     * Extract a integer property from the given json tree. Returns null if no
     * property exists or is not a boolean node.
     * 
     * @param node JsonNode
     * @param propertyName Property to extract
     * @return Integer containing the extracted value
     */
    public static Integer intProperty(JsonNode node, String propertyName) {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode != null) {
            return propertyNode.asInt();
        }
        return null;
    }

    /**
     * Sets the value of a property for a given json node. If the value is null,
     * then the property is not written.
     * 
     * @param node ObjectNode
     * @param propertyName Property to be set
     * @param propertyValue Integer value to be set
     */
    public static void intProperty(ObjectNode node, String propertyName, Integer propertyValue) {
        if (propertyValue == null) {
            return;
        }
        node.set(propertyName, factory.numberNode(propertyValue));
    }

    /**
     * Extract a BigDecimal property from the given json tree. Returns null if no
     * property exists or is not a boolean node.
     * 
     * @param node JsonNode
     * @param propertyName Property to extract
     * @return BigDecimal containing the extracted value
     */
    public static BigDecimal bigDecimalProperty(JsonNode node, String propertyName) {
        JsonNode propertyNode = node.get(propertyName);
        if (propertyNode != null) {
            return new BigDecimal(propertyNode.asText());
        }
        return null;
    }

    /**
     * Sets the value of a property for a given json node. If the value is null,
     * then the property is not written.
     * 
     * @param node ObjectNode
     * @param propertyName Property to be set
     * @param propertyValue BigDecimal value to be set
     */
    public static void bigDecimalProperty(ObjectNode node, String propertyName, BigDecimal propertyValue) {
        if (propertyValue == null) {
            return;
        }
        if (isIntegerValue(propertyValue)) {
            node.set(propertyName, factory.numberNode(propertyValue.toBigInteger()));
        } else {
            node.set(propertyName, factory.numberNode(propertyValue));
        }
    }

    private static boolean isIntegerValue(BigDecimal bd) {
        return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
    }

    /**
     * Reads the node as a Java object.This is typically expected to be a literal of
     * some sort, as in the case of default values and examples. The node may be anything
     * from a string to a javascript object.
     * 
     * @param node the json node
     * @return a java object
     */
    public static Object readObject(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isBigDecimal()) {
            return new BigDecimal(node.asText());
        }
        if (node.isBigInteger()) {
            return new BigInteger(node.asText());
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isDouble()) {
            return node.asDouble();
        }
        if (node.isFloat()) {
            return node.asDouble();
        }
        if (node.isInt()) {
            return node.asInt();
        }
        if (node.isLong()) {
            return node.asLong();
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            List<Object> items = new ArrayList<>();
            for (JsonNode itemNode : arrayNode) {
                items.add(readObject(itemNode));
            }
            return items;
        }
        if (node.isObject()) {
            Map<String, Object> items = new LinkedHashMap<>();
            for (Iterator<Entry<String, JsonNode>> fields = node.fields(); fields.hasNext();) {
                Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                Object fieldValue = readObject(field.getValue());
                items.put(fieldName, fieldValue);
            }
            return items;
        }
        return null;
    }

    /**
     * Parses an extension value. The value may be:
     *
     * - JSON object - starts with {
     * - JSON array - starts with [
     * - number
     * - boolean
     * - string
     *
     * @param value the value to parse
     * @return Extension
     */
    public static Object parseValue(String value) {
        if (value == null || value =="") {
            return null;
        }

        value = value.trim();

        if ("true".equals(value) || "false".equals(value)) {
            return Boolean.valueOf(value);
        }

        switch (value.charAt(0)) {
            case '{': /* JSON Object */
            case '[': /* JSON Array */
            case '-': /* JSON Negative Number */
            case '0': /* JSON Numbers */
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                try {
                    com.fasterxml.jackson.databind.JsonNode node = MAPPER.readTree(value);
                    return readObject(node);
                } catch (Exception e) {
                    // TODO log the error
                    break;
                }
            default:
                break;
        }

        // JSON String
        return value;
    }

    /**
     * Reads a string array.
     * 
     * @param node the json node
     * @return List of strings
     */
    public static Optional<List<String>> readStringArray(final JsonNode node) {
        if (node != null && node.isArray()) {

            List<String> rval = new ArrayList<>(node.size());
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode arrayItem : arrayNode) {
                if (arrayItem != null) {
                    rval.add(arrayItem.asText());
                }
            }
            return Optional.of(rval);
        }
        return Optional.empty();
    }

    /**
     * Reads an object array.
     * 
     * @param node the json node
     * @return list of objects
     */
    public static Optional<List<Object>> readObjectArray(final JsonNode node) {
        if (node != null && node.isArray()) {

            List<Object> rval = new ArrayList<>(node.size());
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode arrayItem : arrayNode) {
                if (arrayItem != null) {
                    rval.add(readObject(arrayItem));
                }
            }
            return Optional.of(rval);
        }
        return Optional.empty();
    }

    /**
     * Reads a map of strings.
     * 
     * @param node json map
     * @return a String-String map
     */
    public static Optional<Map<String, String>> readStringMap(JsonNode node) {
        if (node != null && node.isObject()) {
            Map<String, String> rval = new LinkedHashMap<>();
            for (Iterator<String> fieldNames = node.fieldNames(); fieldNames.hasNext();) {
                String fieldName = fieldNames.next();
                String value = JsonUtil.stringProperty(node, fieldName);
                rval.put(fieldName, value);
            }
            return Optional.of(rval);
        }
        return Optional.empty();
    }

}
