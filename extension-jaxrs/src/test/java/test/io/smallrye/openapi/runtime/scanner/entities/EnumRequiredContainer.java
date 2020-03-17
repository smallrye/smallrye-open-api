package test.io.smallrye.openapi.runtime.scanner.entities;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class EnumRequiredContainer {
    @Schema(required = true)
    BazEnum bazEnum;
}
