package io.smallrye.openapi.runtime.io.info;

import java.util.Optional;

import org.eclipse.microprofile.openapi.models.info.License;
import org.jboss.jandex.AnnotationInstance;

import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.ModelIO;
import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.io.extensions.ExtensionIO;

public class LicenseIO<V, A extends V, O extends V, AB, OB> extends ModelIO<License, V, A, O, AB, OB> {

    private static final String PROP_NAME = "name";
    private static final String PROP_URL = "url";

    private final ExtensionIO<V, A, O, AB, OB> extension;

    public LicenseIO(IOContext<V, A, O, AB, OB> context) {
        super(context, Names.LICENSE, Names.create(License.class));
        extension = new ExtensionIO<>(context);
    }

    @Override
    public License read(AnnotationInstance annotation) {
        IoLogging.logger.singleAnnotation("@License");
        License license = new LicenseImpl();
        license.setName(value(annotation, PROP_NAME));
        license.setUrl(value(annotation, PROP_URL));
        license.setExtensions(extension.readExtensible(annotation));
        return license;
    }

    @Override
    public License readObject(O node) {
        IoLogging.logger.singleJsonNode("License");
        License license = new LicenseImpl();
        license.setName(jsonIO.getString(node, PROP_NAME));
        license.setUrl(jsonIO.getString(node, PROP_URL));
        extension.readMap(node).forEach(license::addExtension);
        return license;
    }

    public Optional<O> write(License model) {
        return optionalJsonObject(model).map(node -> {
            setIfPresent(node, PROP_NAME, jsonIO.toJson(model.getName()));
            setIfPresent(node, PROP_URL, jsonIO.toJson(model.getUrl()));
            setAllIfPresent(node, extension.write(model));
            return node;
        }).map(jsonIO::buildObject);
    }
}
