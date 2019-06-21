/*
 * Copyright 2019 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ParameterScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    public void testIgnoredMpOpenApiHeaders() throws IOException, JSONException {
        test("params.ignored-mp-openapi-headers.json",
                IgnoredMpOpenApiHeaderArgsTestResource.class,
                Widget.class);
    }

    @Test
    public void testParameterOnMethod() throws IOException, JSONException {
        test("params.parameter-on-method.json",
                ParameterOnMethodTestResource.class,
                Widget.class);
    }

    @Test
    public void testParameterOnField() throws IOException, JSONException {
        test("params.parameter-on-field.json",
                ResourcePathParamTestResource.class,
                Widget.class);
    }

    @Test
    public void testParameterInBeanFromField() throws IOException, JSONException {
        test("params.parameter-in-bean-from-field.json",
                ParameterInBeanFromFieldTestResource.class,
                ParameterInBeanFromFieldTestResource.Bean.class,
                Widget.class);
    }

    @Test
    public void testParameterInBeanFromSetter() throws IOException, JSONException {
        test("params.parameter-in-bean-from-setter.json",
                ParameterInBeanFromSetterTestResource.class,
                ParameterInBeanFromSetterTestResource.Bean.class,
                Widget.class);
    }

    @Test
    public void testPathParamWithFormParams() throws IOException, JSONException {
        test("params.path-param-with-form-params.json",
                PathParamWithFormParamsTestResource.class,
                Widget.class);
    }

    @Test
    public void testMultipleContentTypesWithFormParams() throws IOException, JSONException {
        test("params.multiple-content-types-with-form-params.json",
                MultipleContentTypesWithFormParamsTestResource.class,
                Widget.class);
    }

    @Test
    public void testParametersInConstructor() throws IOException, JSONException {
        test("params.parameters-in-constructor.json",
                ParametersInConstructorTestResource.class,
                ParametersInConstructorTestResource.Bean.class,
                Widget.class);
    }

    @Test
    public void testMatrixParamsOnResourceMethodArgs() throws IOException, JSONException {
        test("params.matrix-params-on-resource-method-args.json",
                MatrixParamsOnResourceMethodArgsTestResource.class,
                Widget.class);
    }

    @Test
    public void testMatrixParamsOnResourceMethodCustomName() throws IOException, JSONException {
        test("params.matrix-params-on-resource-method-custom-name.json",
                MatrixParamsOnResourceMethodCustomNameTestResource.class,
                Widget.class);
    }

    @Test
    public void testMatrixParamsOnMethodAndFieldArgs() throws IOException, JSONException {
        test("params.matrix-params-on-method-and-field-args.json",
                MatrixParamsOnMethodAndFieldArgsTestResource.class,
                Widget.class);
    }

    @Test
    public void testAllTheParams() throws IOException, JSONException {
        test("params.all-the-params.json",
                AllTheParamsTestResource.class,
                AllTheParamsTestResource.Bean.class,
                Widget.class);
    }

    /***************** Test models and resources below. ***********************/

    public static class Widget {
        String id;
        String name;
    }

    @Path("ignored-headers")
    static class IgnoredMpOpenApiHeaderArgsTestResource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @SuppressWarnings("unused")
        public Widget get(@HeaderParam("Authorization") String auth,
                @HeaderParam("Content-Type") String contentType,
                @HeaderParam("Accept") String accept,
                @HeaderParam("X-Custom-Header") String custom) {
            return null;
        }
    }

    @Path("parameter-on-method/{id}")
    static class ParameterOnMethodTestResource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @SuppressWarnings("unused")
        @Parameter(name = "X-Custom-Header", in = ParameterIn.HEADER, required = true)
        @Parameter(name = "id", in = ParameterIn.PATH)
        public Widget get(@HeaderParam("X-Custom-Header") String custom,
                @PathParam("id") @DefaultValue("000") String id) {
            return null;
        }
    }

    @Path("/parameter-on-field/{id}")
    static class ResourcePathParamTestResource {
        @PathParam("id")
        @DefaultValue("ABC")
        String id;

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get() {
            return null;
        }
    }

    @Path("/parameter-in-bean-from-field/{id}")
    static class ParameterInBeanFromFieldTestResource {
        static class Bean {
            @PathParam("id")
            @DefaultValue("BEAN")
            String id;
        }

        @BeanParam
        private Bean param;

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get() {
            return null;
        }
    }

    @Path("/parameter-in-bean-from-setter/{id}/{id2}")
    @SuppressWarnings("unused")
    static class ParameterInBeanFromSetterTestResource {
        static class Bean {
            @PathParam("id")
            @DefaultValue("BEAN-FROM-SETTER")
            String id;
        }

        @SuppressWarnings("unused")
        private Bean param;

        @BeanParam
        public void setParam(Bean param) {
            this.param = param;
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get(@PathParam("id2") String id2) {
            return null;
        }
    }

    @Path("/path-param-with-form-params/{id}")
    @SuppressWarnings("unused")
    static class PathParamWithFormParamsTestResource {
        @PathParam("id")
        @DefaultValue("12345")
        @NotNull
        @Size(min = 1, max = 12)
        String id;

        @FormParam("form-param1")
        // Not supported on resource fields and parameters
        private String formParam1;

        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.APPLICATION_JSON)
        public Widget update(@FormParam("form-param2") @Size(max = 10) String formParam2) {
            return null;
        }
    }

    @Path("/multiple-content-types-with-form-params")
    @SuppressWarnings("unused")
    static class MultipleContentTypesWithFormParamsTestResource {
        @POST
        @Path("/widgets/create")
        @Consumes(MediaType.APPLICATION_JSON)
        @Operation(operationId = "createWidget")
        public void createWidget(
                @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Widget.class))) final Widget w) {
        }

        @POST
        @Path("/widgets/create")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Operation(operationId = "createWidget")
        public void createWidget(@FormParam("id") String id, @FormParam("name") String name) {
        }
    }

    @Path("/parameters-in-constructor/{id}/{p1}")
    @SuppressWarnings("unused")
    static class ParametersInConstructorTestResource {
        static class Bean {
            @PathParam("id")
            @DefaultValue("BEAN")
            String id;
        }

        private Bean param;

        public ParametersInConstructorTestResource(
                @Parameter(name = "h1", in = ParameterIn.HEADER, description = "Description of h1") @HeaderParam("h1") @Deprecated String h1,
                @Parameter(name = "h2", in = ParameterIn.HEADER, hidden = true) @HeaderParam("h2") String h2,
                @Parameter(name = "q1", deprecated = true) @QueryParam("q1") String q1,
                @NotNull @CookieParam("c1") String c1,
                @PathParam("p1") String p1,
                @BeanParam Bean b1) {
        }

        @DELETE
        public void deleteWidget() {
        }
    }

    @Path("/matrix-params-on-resource-method-args/{id}")
    @SuppressWarnings("unused")
    static class MatrixParamsOnResourceMethodArgsTestResource {
        @PathParam("id")
        @NotNull
        @Size(max = 10)
        String id;

        @GET
        @Path("/anotherpathsegment/reloaded/")
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get(@MatrixParam("m1") @DefaultValue("default-m1") String m1,
                @MatrixParam("m2") @Size(min = 20) String m2) {
            return null;
        }
    }

    @Path("/matrix-params-on-resource-method-custom-name/{id}")
    @SuppressWarnings("unused")
    static class MatrixParamsOnResourceMethodCustomNameTestResource {
        @PathParam("id")
        @Size(max = 10)
        String id;

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Parameter(name = "r1", in = ParameterIn.PATH, style = ParameterStyle.MATRIX, description = "Additional information for id2")
        public Widget get(@MatrixParam("m1") @DefaultValue("default-m1") String m1,
                @MatrixParam("m2") @Size(min = 20) String m2) {
            return null;
        }
    }

    @Path("/matrix-params-on-method-and-field-args/{id}")
    @SuppressWarnings("unused")
    static class MatrixParamsOnMethodAndFieldArgsTestResource {
        @PathParam("id")
        @Size(max = 10)
        String id;

        @MatrixParam("c1")
        String c1;

        @MatrixParam("c2")
        String c2;

        @GET
        @Path("/seg1/seg2/resourceA")
        @Produces(MediaType.APPLICATION_JSON)
        @Parameter(in = ParameterIn.PATH, name = "methodMatrix", style = ParameterStyle.MATRIX)
        public Widget get(@MatrixParam("m1") @DefaultValue("default-m1") int m1,
                @MatrixParam("m2") @DefaultValue("100") @Max(200) int m2) {
            return null;
        }
    }

    @Path("/all/the/params/{id1}/{id2}")
    @SuppressWarnings("unused")
    static class AllTheParamsTestResource {
        public AllTheParamsTestResource(@PathParam("id1") int id1,
                @org.jboss.resteasy.annotations.jaxrs.PathParam String id2) {
        }

        static class Bean {
            @org.jboss.resteasy.annotations.jaxrs.MatrixParam
            @DefaultValue("BEAN1")
            String matrixF1;

            @MatrixParam("matrixF2")
            @DefaultValue("BEAN2")
            String matrixF2;

            @org.jboss.resteasy.annotations.jaxrs.CookieParam
            @DefaultValue("COOKIE1")
            @Deprecated
            String cookieF1;
        }

        @Parameter(in = ParameterIn.PATH, style = ParameterStyle.MATRIX, name = "mtx")
        @BeanParam
        private Bean param;

        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.APPLICATION_JSON)
        public CompletionStage<Widget> upd(@FormParam("f1") @DefaultValue("42") int f1,
                @org.jboss.resteasy.annotations.jaxrs.FormParam @DefaultValue("f2-default") @NotNull String f2,
                @HeaderParam("h1") @Deprecated int h1,
                @org.jboss.resteasy.annotations.jaxrs.HeaderParam("h2") String notH2) {
            return null;
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get(@QueryParam("q1") @Deprecated long q1,
                @org.jboss.resteasy.annotations.jaxrs.QueryParam("q2") String notQ2) {
            return null;
        }
    }
}
