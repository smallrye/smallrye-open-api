package test.io.smallrye.openapi.runtime.scanner.entities;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */

public class TransientFieldExample {
    // Transient field will be ignored by default
    transient String ignoredTransientField;

    // Transient field will be included if we explicitly 'unhide' it with a Schema annotation (and `hidden` is false)
    @Schema(hidden = false)
    transient String unhiddenTransientField;
}
