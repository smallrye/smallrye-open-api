package io.smallrye.openapi.runtime.io;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;

import io.smallrye.openapi.api.OpenApiConfig;

class JakartaJsonIOTest extends JsonIOTest<JsonValue, JsonArray, JsonObject, JsonArrayBuilder, JsonObjectBuilder> {

    @BeforeEach
    void setup() {
        super.target = new JakartaJsonIO(OpenApiConfig.fromConfig(ConfigProvider.getConfig()));
    }

}
