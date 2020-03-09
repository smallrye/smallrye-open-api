package io.smallrye.openapi.api.reader;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * This reads annotations for External Documentation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#externalDocumentationObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExternalDocsReader {
    private static final Logger LOG = Logger.getLogger(ExternalDocsReader.class);

    private ExternalDocsReader() {
    }

    /**
     * Reads an ExternalDocumentation annotation.
     * 
     * @param annotationValue the {@literal @}ExternalDocumentation annotation
     * @return ExternalDocumentation model
     */
    public static ExternalDocumentation readExternalDocs(final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing an @ExternalDocumentation annotation.");
        AnnotationInstance nested = annotationValue.asNested();
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(JandexUtil.stringValue(nested, OpenApiConstants.PROP_DESCRIPTION));
        externalDoc.setUrl(JandexUtil.stringValue(nested, OpenApiConstants.PROP_URL));
        return externalDoc;
    }
}
