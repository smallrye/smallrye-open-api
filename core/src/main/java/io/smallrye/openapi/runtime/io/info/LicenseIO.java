package io.smallrye.openapi.runtime.io.info;

import org.eclipse.microprofile.openapi.models.info.License;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;

public class LicenseIO<V, A extends V, O extends V, AB, OB> extends ModelIO<License, V, A, O, AB, OB> {

    public LicenseIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.LICENSE, Names.create(License.class));
    }

    @Override
    public License read(AnnotationInstance annotation) {
        return read(License.class, annotation);
    }
}
