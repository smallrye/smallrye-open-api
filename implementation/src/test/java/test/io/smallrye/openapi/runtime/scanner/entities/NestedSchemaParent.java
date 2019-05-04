package test.io.smallrye.openapi.runtime.scanner.entities;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */

public class NestedSchemaParent {

    NestedSchemaSiblingA child1;

    @Schema(type = SchemaType.OBJECT)
    NestedSchemaSiblingB child2;

}
