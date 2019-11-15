/*
 * Copyright 2019 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.time.LocalDate;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.json.JSONException;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ResourceInheritanceTests extends OpenApiDataObjectScannerTestBase {

    /*
     * Test case derived from original example linked from Smallrye OpenAPI
     * issue #184.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/184
     * https://github.com/quarkusio/quarkus/issues/4298
     *
     */
    @Test
    public void testInheritedResourceMethod() throws IOException, JSONException {
        Index index = indexOf(GenericResource.class,
                ExampleResource1.class,
                ExampleResource2.class,
                Greetable.class,
                Greetable.GreetingBean.class);

        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("resource.inheritance.params.json", result);
    }

    static class GenericResource {
        @GET
        @Path("/extension")
        @Produces(MediaType.TEXT_PLAIN)
        public String helloExtension() {
            return "hello extension";
        }
    }

    static interface Greetable {
        static class GreetingBean {
            String name;

            @FormParam("greetingName")
            public void setName(String name) {
                this.name = name;
            }
        }

        @PathParam("from")
        @Parameter(name = "from", in = ParameterIn.PATH, description = "The name of the person sending the greeting")
        void setFromName(String from);

        @HeaderParam("date")
        @Parameter(name = "date", in = ParameterIn.HEADER, description = "The local date when the greeting is sent", allowEmptyValue = true)
        void setGreetingDate(LocalDate date);

        @POST
        @Path("/greet/{from}")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.TEXT_PLAIN)
        @Parameter(name = "greetingName", style = ParameterStyle.FORM)
        String greet(@BeanParam GreetingBean bean);
    }

    @Path("/hi")
    // All JAX-RS annotations inherited
    static class ExampleResource1 extends GenericResource implements Greetable {
        String from;

        @Override
        public void setFromName(String from) {
            this.from = from;
        }

        LocalDate date;

        @Override
        public void setGreetingDate(LocalDate date) {
            this.date = date;
        }

        @Override
        public String greet(GreetingBean bean) {
            return "hi " + bean.name + ", from: " + from + "; on date: " + date;
        }
    }

    @Path("/hello")
    static class ExampleResource2 extends GenericResource implements Greetable {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String hello() {
            return "hello";
        }

        String from;

        @Override
        @Parameter(name = "from", in = ParameterIn.PATH, style = ParameterStyle.SIMPLE)
        public void setFromName(String from) {
            this.from = from;
        }

        @Parameter(name = "date", in = ParameterIn.HEADER, example = "2019-12-31", allowEmptyValue = false)
        LocalDate date;

        @Override
        public void setGreetingDate(LocalDate date) {
            this.date = date;
        }

        @Override
        public String greet(GreetingBean bean) {
            return "hello " + bean.name + ", from: " + from + "; on date: " + date;
        }
    }

    /*************************************************************************/

}
