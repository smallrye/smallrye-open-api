package io.smallrye.openapi.api.constants;

import org.jboss.jandex.DotName;

/**
 * Constants related to the kotlinx.serialization library
 *
 * @author Nicklas Jensen {@literal <nillerr@gmail.com>}
 */
public class KotlinSerializationConstants {

    public static final DotName SERIAL_NAME = DotName
            .createSimple("kotlinx.serialization.SerialName");

    public static final DotName REQUIRED = DotName
            .createSimple("kotlinx.serialization.Required");

    public static final String PROP_VALUE = "value";

    private KotlinSerializationConstants() {
    }
}
