package io.smallrye.openapi.runtime.io.externaldocs;

/**
 * Constants related to External Docs
 * 
 * @see <a href=
 *      "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#externalDocumentationObject">externalDocumentationObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExternalDocsConstant {
    public static final String PROP_EXTERNAL_DOCS = "externalDocs";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_URL = "url";

    private ExternalDocsConstant() {
    }
}
