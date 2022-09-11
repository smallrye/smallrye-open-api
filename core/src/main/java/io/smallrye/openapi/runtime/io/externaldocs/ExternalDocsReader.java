package io.smallrye.openapi.runtime.io.externaldocs;

import org.eclipse.microprofile.openapi.models.ExternalDocumentation;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.openapi.api.models.ExternalDocumentationImpl;
import io.smallrye.openapi.runtime.io.IoLogging;
import io.smallrye.openapi.runtime.io.JsonUtil;
import io.smallrye.openapi.runtime.io.extension.ExtensionReader;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.Annotations;

/**
 * This reads annotations and json for External Documentation
 *
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#externalDocumentationObject">externalDocumentationObject</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExternalDocsReader {

    private ExternalDocsReader() {
    }

    /**
     * Reads an ExternalDocumentation annotation.
     *
     * @param context scanning context
     * @param annotationValue the {@literal @}ExternalDocumentation annotation
     * @return ExternalDocumentation model
     */
    public static ExternalDocumentation readExternalDocs(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        return readExternalDocs(context, annotationValue.asNested());
    }

    /**
     * Reads an ExternalDocumentation annotation.
     *
     * @param context scanning context
     * @param annotationInstance the {@literal @}ExternalDocumentation annotation
     * @return ExternalDocumentation model
     */
    public static ExternalDocumentation readExternalDocs(AnnotationScannerContext context,
            AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        IoLogging.logger.annotation("@ExternalDocumentation");
        ExternalDocumentation externalDoc = new ExternalDocumentationImpl();
        externalDoc.setDescription(
                Annotations.stringValue(annotationInstance, ExternalDocsConstant.PROP_DESCRIPTION));
        externalDoc.setUrl(Annotations.stringValue(annotationInstance, ExternalDocsConstant.PROP_URL));
        externalDoc.setExtensions(ExtensionReader.readExtensions(context, annotationInstance));
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
