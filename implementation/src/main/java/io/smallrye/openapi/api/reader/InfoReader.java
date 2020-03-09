package io.smallrye.openapi.api.reader;

import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.info.ContactImpl;
import io.smallrye.openapi.api.models.info.InfoImpl;
import io.smallrye.openapi.api.models.info.LicenseImpl;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * This reads the Info annotations (including Contact and License)
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
        info.setTitle(JandexUtil.stringValue(nested, OpenApiConstants.PROP_TITLE));
        info.setDescription(JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION));
        info.setTermsOfService(JandexUtil.stringValue(nested, OpenApiConstants.PROP_TERMS_OF_SERVICE));
        info.setContact(readContact(nested.value(OpenApiConstants.PROP_CONTACT)));
        info.setLicense(readLicense(nested.value(OpenApiConstants.PROP_LICENSE)));
        info.setVersion(JandexUtil.stringValue(nested, OpenApiConstants.PROP_VERSION));
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
        LOG.debug("Processing an @Contact annotation.");
        AnnotationInstance nested = annotationValue.asNested();
        Contact contact = new ContactImpl();
        contact.setName(JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME));
        contact.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        contact.setEmail(JandexUtil.stringValue(nested, OpenApiConstants.PROP_EMAIL));
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
        LOG.debug("Processing an @License annotation.");
        AnnotationInstance nested = annotationValue.asNested();
        License license = new LicenseImpl();
        license.setName(JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME));
        license.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        return license;
    }
}
