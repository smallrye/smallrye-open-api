package io.smallrye.openapi.runtime.io.license;

import org.eclipse.microprofile.openapi.models.info.License;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * This reads the License from annotations or json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#licenseObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class LicenseReader {
    private static final Logger LOG = Logger.getLogger(LicenseReader.class);

    private LicenseReader() {
    }

    /**
     * Reads an License annotation.
     * 
     * @param annotationValue the {@literal @}License annotation
     * @return License model
     */
    public static License readLicense(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a @License annotation.");
        AnnotationInstance nested = annotationValue.asNested();
        License license = new LicenseImpl();
        license.setName(JandexUtil.stringValue(nested, LicenseConstant.PROP_NAME));
        license.setUrl(JandexUtil.stringValue(nested, LicenseConstant.PROP_URL));
        return license;
    }

    /**
     * Reads an {@link License} OpenAPI node.
     * 
     * @param node the json node
     * @return License model
     */
    public static License readLicense(final JsonNode node) {
        if (node == null) {
            return null;
        }
        LOG.debug("Processing a License json node.");
        License license = new LicenseImpl();
        license.setName(JsonUtil.stringProperty(node, LicenseConstant.PROP_NAME));
        license.setUrl(JsonUtil.stringProperty(node, LicenseConstant.PROP_URL));
        ExtensionReader.readExtensions(node, license);
        return license;
    }
}
