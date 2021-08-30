package io.smallrye.openapi.api.constants;

import org.jboss.jandex.DotName;

/**
 * Constants related to the Kotlin language
 * 
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class KotlinConstants {

    public static final DotName CONTINUATION = DotName
            .createSimple("kotlin.coroutines.Continuation");

    private KotlinConstants() {
    }
}
