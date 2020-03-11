package io.smallrye.openapi.runtime.reader;

import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.api.models.info.InfoImpl;
import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * This reads the Info (including Contact and License) from annotations or json
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#info-object-example
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class InfoReader {
    private static final Logger LOG = Logger.getLogger(InfoReader.class);

    private InfoReader() {
    }

    /**
     * Annotation to Info
     * 
     * @param annotationValue the {@literal @}Info annotation
     * @return Info model
     */
    public static Info readInfo(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an @Info annotation.");
        AnnotationInstance nested = annotationValue.asNested();

        Info info = new InfoImpl();
        info.setTitle(JandexUtil.stringValue(nested, MPOpenApiConstants.INFO.PROP_TITLE));
        info.setDescription(JandexUtil.stringValue(nested, MPOpenApiConstants.INFO.PROP_DESCRIPTION));
        info.setTermsOfService(JandexUtil.stringValue(nested, MPOpenApiConstants.INFO.PROP_TERMS_OF_SERVICE));
        info.setContact(readContact(nested.value(MPOpenApiConstants.INFO.PROP_CONTACT)));
        info.setLicense(readLicense(nested.value(MPOpenApiConstants.INFO.PROP_LICENSE)));
        info.setVersion(JandexUtil.stringValue(nested, MPOpenApiConstants.INFO.PROP_VERSION));
        return info;
    }

    /**
     * Reads an {@link Info} OpenAPI node.
     * 
     * @param node the json node
     * @return Info model
     */
    public static Info readInfo(final JsonNode node) {
        if (node == null) {
            return null;
        }
        LOG.debug("Processing an Info json node.");

        Info info = new InfoImpl();
        info.setTitle(JsonUtil.stringProperty(node, MPOpenApiConstants.INFO.PROP_TITLE));
        info.setDescription(JsonUtil.stringProperty(node, MPOpenApiConstants.INFO.PROP_DESCRIPTION));
        info.setTermsOfService(JsonUtil.stringProperty(node, MPOpenApiConstants.INFO.PROP_TERMS_OF_SERVICE));
        info.setContact(readContact(node.get(MPOpenApiConstants.INFO.PROP_CONTACT)));
        info.setLicense(readLicense(node.get(MPOpenApiConstants.INFO.PROP_LICENSE)));
        info.setVersion(JsonUtil.stringProperty(node, MPOpenApiConstants.INFO.PROP_VERSION));
        ExtensionReader.readExtensions(node, info);
        return info;
    }

    /**
     * Reads an Contact annotation.
     * 
     * @param annotationValue the {@literal @}Contact annotation
     * @return Contact model
     */
    private static Contact readContact(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a @Contact annotation.");
        AnnotationInstance nested = annotationValue.asNested();
        Contact contact = new ContactImpl();
        contact.setName(JandexUtil.stringValue(nested, MPOpenApiConstants.CONTACT.PROP_NAME));
        contact.setUrl(JandexUtil.stringValue(nested, MPOpenApiConstants.CONTACT.PROP_URL));
        contact.setEmail(JandexUtil.stringValue(nested, MPOpenApiConstants.CONTACT.PROP_EMAIL));
        return contact;
    }

    /**
     * Reads an {@link Contact} OpenAPI node.
     * 
     * @param node the json node
     * @return Contact model
     */
    private static Contact readContact(final JsonNode node) {
        if (node == null) {
            return null;
        }
        LOG.debug("Processing a Contact json node.");
        Contact contact = new ContactImpl();
        contact.setName(JsonUtil.stringProperty(node, MPOpenApiConstants.CONTACT.PROP_NAME));
        contact.setUrl(JsonUtil.stringProperty(node, MPOpenApiConstants.CONTACT.PROP_URL));
        contact.setEmail(JsonUtil.stringProperty(node, MPOpenApiConstants.CONTACT.PROP_EMAIL));
        ExtensionReader.readExtensions(node, contact);
        return contact;
    }

    /**
     * Reads an License annotation.
     * 
     * @param annotationValue the {@literal @}License annotation
     * @return License model
     */
    private static License readLicense(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a @License annotation.");
        AnnotationInstance nested = annotationValue.asNested();
        License license = new LicenseImpl();
        license.setName(JandexUtil.stringValue(nested, MPOpenApiConstants.LICENSE.PROP_NAME));
        license.setUrl(JandexUtil.stringValue(nested, MPOpenApiConstants.LICENSE.PROP_URL));
        return license;
    }

    /**
     * Reads an {@link License} OpenAPI node.
     * 
     * @param node the json node
     * @return License model
     */
    private static License readLicense(final JsonNode node) {
        if (node == null) {
            return null;
        }
        LOG.debug("Processing a License json node.");
        License license = new LicenseImpl();
        license.setName(JsonUtil.stringProperty(node, MPOpenApiConstants.LICENSE.PROP_NAME));
        license.setUrl(JsonUtil.stringProperty(node, MPOpenApiConstants.LICENSE.PROP_URL));
        ExtensionReader.readExtensions(node, license);
        return license;
    }
}
