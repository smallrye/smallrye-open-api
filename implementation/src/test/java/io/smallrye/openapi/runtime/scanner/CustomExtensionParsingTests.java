/*
 * Copyright 2020 Red Hat, Inc, and individual contributors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callbacks;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Special tests using a custom {@link AnnotationScannerExtension#parseExtension(String, String)}
 * implementation. The tests in this class will only run when system property `classpath.jackson.excluded`
 * is set to `true`. In that case, the Jackson dependencies should not be present on the class path.
 * 
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
@RunWith(CustomExtensionParsingTests.JacksonExcludedRunner.class)
public class CustomExtensionParsingTests {

    public static class JacksonExcludedRunner extends BlockJUnit4ClassRunner {

        public JacksonExcludedRunner(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override
        protected boolean isIgnored(FrameworkMethod child) {
            return !Boolean.valueOf(System.getProperty("classpath.jackson.excluded"));
        }
    }

    @Test
    public void testDefaultExtensionParseThrowsJacksonNotFound() {
        Index index = IndexScannerTestBase.indexOf(ExtensionParsingTestResource.class);
        NoClassDefFoundError err = assertThrows(NoClassDefFoundError.class,
                () -> new OpenApiAnnotationScanner(IndexScannerTestBase.emptyConfig(), index).scan());
        assertTrue(err.getMessage().contains("jackson"));
    }

    @Test
    public void testCustomAnnotationScannerExtension() {
        Index index = IndexScannerTestBase.indexOf(ExtensionParsingTestResource.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(IndexScannerTestBase.emptyConfig(), index,
                Arrays.asList(new AnnotationScannerExtension() {
                    @Override
                    public Object parseExtension(String name, String value) {
                        /*
                         * "parsing" consists of creating a singleton map with the
                         * extension name as the key and the unparsed value as the value
                         */
                        return Collections.singletonMap(name, value);
                    }
                }));

        OpenAPI result = scanner.scan();
        org.eclipse.microprofile.openapi.models.callbacks.Callback cb;
        cb = result.getPaths().getPathItem("/ext-custom").getPOST().getCallbacks().get("extendedCallback");
        Map<String, Object> ext = cb.getPathItem("http://localhost:8080/resources/ext-callback").getGET().getExtensions();
        assertEquals(4, ext.size());
        assertEquals(Collections.singletonMap("x-object", "{ \"key\":\"value\" }"), ext.get("x-object"));
        assertEquals("{ \"key\":\"value\" }", ext.get("x-object-unparsed"));
        assertEquals(Collections.singletonMap("x-array", "[ \"val1\",\"val2\" ]"), ext.get("x-array"));
        assertEquals("true", ext.get("x-booltrue"));
    }

    /* Test models and resources below. */

    @Path("/ext-custom")
    static class ExtensionParsingTestResource {
        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        @Callbacks({
                @Callback(name = "extendedCallback", callbackUrlExpression = "http://localhost:8080/resources/ext-callback", operations = @CallbackOperation(summary = "Get results", extensions = {
                        @Extension(name = "x-object", value = "{ \"key\":\"value\" }", parseValue = true),
                        @Extension(name = "x-object-unparsed", value = "{ \"key\":\"value\" }"),
                        @Extension(name = "x-array", value = "[ \"val1\",\"val2\" ]", parseValue = true),
                        @Extension(name = "x-booltrue", value = "true", parseValue = false)
                }, method = "get", responses = @APIResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = String.class)))))
        })
        public String get(String data) {
            return data;
        }
    }
}
