package io.smallrye.openapi.runtime.scanner;

import jdk.nashorn.internal.objects.annotations.Getter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.testng.annotations.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

public class ExceptionMapperScanTests extends  IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
    //assertJsonEquals(expectedResource, result);
    }

    static class Test1 extends RuntimeException {

    }

    static class Test2 extends Test1 {

    }

    static class Test3 extends Test2 {

    }

    @Test
    public void testParentChild() {
        Index index = indexOf(Test1.class, Test2.class, Test3.class);

        ClassInfo classInfo = index.getClassByName(DotName.createSimple(Test3.class.getName()));
        DotName parent = classInfo.superName();
        ClassInfo parentClass = index.getClassByName(parent);

        System.out.println(parentClass.name());
        System.out.println(parentClass.superName());
        System.out.println(parentClass.superClassType());


    }

    @Test
    public void testExceptionMapper() throws IOException, JSONException {
        test("", TestResource.class, ExceptionHandler.class, TestExceptionHandler.class);
    }



    @Path("/resources")
    static class TestResource {

        @GET
        public String getResource() throws RuntimeException {
            return "";
        }

        @POST
        public String createResource() throws WebApplicationException {
            return "";
        }
    }

    @Provider
    static class ExceptionHandler implements ExceptionMapper<RuntimeException> {

        @Override
        @APIResponse(responseCode = "500", description = "Cannot find exception")
        public Response toResponse(RuntimeException e) {
            return null;
        }
    }

    @Provider
    static class TestExceptionHandler implements ExceptionMapper<WebApplicationException> {

        @Override
        @APIResponse(responseCode = "400", description = "Server error")
        public Response toResponse(WebApplicationException e) {
            return null;
        }
    }
}
