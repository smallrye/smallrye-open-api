package io.smallrye.openapi.runtime.io;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class JacksonJsonIOTest extends JsonIOTest<JsonNode, ArrayNode, ObjectNode, ArrayNode, ObjectNode> {

    @BeforeEach
    void setup() {
        super.target = new JacksonJsonIO(new ObjectMapper()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
    }

}
