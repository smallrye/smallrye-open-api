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

    public static final DotName DEPRECATED = DotName
            .createSimple("kotlin.Deprecated");

    public static final DotName JETBRAINS_NULLABLE = DotName
            .createSimple("org.jetbrains.annotations.Nullable");

    public static final DotName JETBRAINS_NOT_NULL = DotName
            .createSimple("org.jetbrains.annotations.NotNull");

    public static final DotName FLOW = DotName
            .createSimple("kotlinx.coroutines.flow.Flow");

    private KotlinConstants() {
    }
}
