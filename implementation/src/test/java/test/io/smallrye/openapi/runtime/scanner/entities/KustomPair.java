package test.io.smallrye.openapi.runtime.scanner.entities;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class KustomPair<A, B> {

    @Schema(required = true, maxLength = 123456)
    private A foo;

    @Schema(required = true)
    private B bar;
}
