package io.smallrye.openapi.runtime.io.externaldocs;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.util.JandexUtil;

/**
 * This reads annotations and json for External Documentation
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#externalDocumentationObject">externalDocumentationObject</a>
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
        return readExternalDocs(annotationValue.asNested());
    }

    /**
     * Reads an ExternalDocumentation annotation.
     * 
     * @param annotationInstance the {@literal @}ExternalDocumentation annotation
     * @return ExternalDocumentation model
     */
    public static ExternalDocumentation readExternalDocs(AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing an @ExternalDocumentation annotation.");
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(
                JandexUtil.stringValue(annotationInstance, ExternalDocsConstant.PROP_DESCRIPTION));
        externalDoc.setUrl(JandexUtil.stringValue(annotationInstance, ExternalDocsConstant.PROP_URL));
        return externalDoc;
    }

    /**
     * Reads an {@link ExternalDocumentation} OpenAPI node.
     * 
     * @param node the json node
     * @return ExternalDocumentation model
     */
    public static ExternalDocumentation readExternalDocs(final JsonNode node) {
        if (node == null) {
            return null;
        }
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(JsonUtil.stringProperty(node, ExternalDocsConstant.PROP_DESCRIPTION));
        externalDoc.setUrl(JsonUtil.stringProperty(node, ExternalDocsConstant.PROP_URL));
        ExtensionReader.readExtensions(node, externalDoc);
        return externalDoc;
    }
}
