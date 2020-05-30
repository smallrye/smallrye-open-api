package io.smallrye.openapi.api.constants;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

public class MutinyConstants {
    public static final Type UNI_TYPE = Type.create(DotName.createSimple("io.smallrye.mutiny.Uni"), Type.Kind.CLASS);
    public static final Type MULTI_TYPE = Type.create(DotName.createSimple("io.smallrye.mutiny.Multi"), Type.Kind.CLASS);

    private MutinyConstants() {
    }
}
