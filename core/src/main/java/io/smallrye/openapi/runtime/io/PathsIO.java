package io.smallrye.openapi.runtime.io;

import org.eclipse.microprofile.openapi.models.Paths;
import org.jboss.jandex.AnnotationInstance;

public class PathsIO<V, A extends V, O extends V, AB, OB> extends ModelIO<Paths, V, A, O, AB, OB> {

    public PathsIO(IOContext<V, A, O, AB, OB> context) {
        super(context, null, Names.create(Paths.class));
    }

    @Override
    public Paths read(AnnotationInstance annotation) {
        throw new UnsupportedOperationException("@Paths annotation does not exist");
    }
}
