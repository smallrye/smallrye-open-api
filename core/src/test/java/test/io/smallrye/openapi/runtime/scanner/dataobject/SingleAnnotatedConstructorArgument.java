package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Data structure with a single property")
public class SingleAnnotatedConstructorArgument {

    @Schema(description = "The single property", example = "some value")
    public String property1;

    // replicates the structure of a single-property Java record
    SingleAnnotatedConstructorArgument(@Schema(description = "The single property", example = "some value") String property1) {
    }

}
