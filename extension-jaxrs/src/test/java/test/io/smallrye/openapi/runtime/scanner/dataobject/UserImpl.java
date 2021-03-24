package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema
public class UserImpl extends BaseUser implements User {

    @Schema(description = "The user identifier", minimum = "15")
    public Integer getId() {
        return 0;
    }

}
