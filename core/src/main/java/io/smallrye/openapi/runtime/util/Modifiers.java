package io.smallrye.openapi.runtime.util;

/**
 * Some utility methods for dealing with modifiers.
 *
 * @author Nicklas Jensen {@literal <nillerr@gmail.com>}
 */
public class Modifiers {
    /**
     * Constructor.
     */
    private Modifiers() {
    }

    /**
     * Checks if the given flags indicate a synthetic member.
     * @param flags flags of the member
     * @return true if the member is synthetic, false otherwise
     */
    static boolean isSynthetic(int flags) {
        return (flags & 0x00001000) != 0;
    }
}
