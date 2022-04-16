package test.io.smallrye.openapi.runtime.scanner.javax;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

/**
 * Test class from example in issue #197.
 *
 * https://github.com/smallrye/smallrye-open-api/issues/197
 *
 */
@SuppressWarnings(value = "unused")
@Path(value = "/test")
public class RestEasyFieldsAndSettersTestResource {

    @GET
    public String getTest() {
        return "TEST";
    }

    // make sure these don't break the build when fields
    @org.jboss.resteasy.annotations.jaxrs.PathParam
    String pathField;
    @org.jboss.resteasy.annotations.jaxrs.FormParam
    String formField;
    @org.jboss.resteasy.annotations.jaxrs.CookieParam
    String cookieField;
    @org.jboss.resteasy.annotations.jaxrs.HeaderParam
    String headerField;
    @org.jboss.resteasy.annotations.jaxrs.MatrixParam
    String matrixField;
    @org.jboss.resteasy.annotations.jaxrs.QueryParam
    String queryField;
    String unusedfield;

    // make sure these don't break the build when properties
    public String getPathProperty() {
        return null;
    }

    @org.jboss.resteasy.annotations.jaxrs.PathParam
    public void setPathProperty(String p) {
    }

    public String getFormProperty() {
        return null;
    }

    @org.jboss.resteasy.annotations.jaxrs.FormParam
    public void setFormProperty(String p) {
    }

    public String getCookieProperty() {
        return null;
    }

    @org.jboss.resteasy.annotations.jaxrs.CookieParam
    public void setCookieProperty(String p) {
    }

    public String getHeaderProperty() {
        return null;
    }

    @org.jboss.resteasy.annotations.jaxrs.HeaderParam
    public void setHeaderProperty(String p) {
    }

    // This annotation is not considered for processing
    @org.jboss.resteasy.annotations.jaxrs.MatrixParam
    public String getMatrixProperty() {
        return null;
    }

    @org.jboss.resteasy.annotations.jaxrs.MatrixParam
    public void setMatrixProperty(String p) {
    }

    public String getQueryProperty() {
        return null;
    }

    @org.jboss.resteasy.annotations.jaxrs.QueryParam
    public void setQueryProperty(String p) {
    }

    @org.jboss.resteasy.annotations.jaxrs.QueryParam
    public void queryProperty2(String p) {
    }

    // This annotation is not considered for processing
    @org.jboss.resteasy.annotations.jaxrs.CookieParam
    public String getUnusedField() {
        return null;
    }

    // This annotation is not considered for processing
    @Parameter(name = "unusedField2")
    public String getUnusedField2() {
        return null;
    }

    // This annotation is not considered for processing (too many arguments)
    @org.jboss.resteasy.annotations.jaxrs.QueryParam
    public void setQueryProperty(String p, String p2) {
    }

}
