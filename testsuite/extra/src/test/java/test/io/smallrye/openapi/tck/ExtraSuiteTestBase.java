/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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

package test.io.smallrye.openapi.tck;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.tck.utils.YamlToJsonFilter;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.restassured.RestAssured;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

/**
 * Base class for all extra test suite tests.
 *
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("restriction")
public abstract class ExtraSuiteTestBase<T extends Arquillian> {

    private static final Logger LOGGER = Logger.getLogger(ExtraSuiteTestBase.class);
    public static Map<Class<?>, OpenAPI> OPEN_API_DOCS = new HashMap<>();

    protected static final String APPLICATION_JSON = "application/json";
    protected static final String TEXT_PLAIN = "text/plain";

    private static HttpServer server;

    @TestFactory
    Collection<DynamicTest> generateTests() throws Exception {
        ExtraTestRunner runner = new ExtraTestRunner(getClass());

        return runner.getChildren()
                .stream()
                .map(test -> DynamicTest.dynamicTest(runner.describeChild(test), () -> runner.runChild(test)))
                .collect(Collectors.toList());
    }

    @BeforeAll
    public static final void setUp() throws Exception {
        // Set up a little HTTP server so that Rest assured has something to pull /openapi from
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int dynamicPort = server.getAddress().getPort();
        LOGGER.debugf("Starting test server on port %d", dynamicPort);

        server.createContext("/openapi", new MyHandler());
        server.setExecutor(null);
        server.start();

        // Register a filter that performs YAML to JSON conversion
        // Called here because the TCK's AppTestBase#setUp() is not called. (Remove for 2.0)
        RestAssured.filters(new YamlToJsonFilter());
        // Set RestAssured default port directly. A bit nasty, but we have no easy way to change
        // AppTestBase#callEndpoint in the upstream. They also seem to do it this way, so it's no worse
        // than what's there.
        RestAssured.port = dynamicPort;
    }

    @AfterAll
    public static final void tearDown() throws Exception {
        server.stop(0);
        LOGGER.debugf("Test server stopped.");
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String acceptedTypes = t.getRequestHeaders().getFirst("Accept");
            if (acceptedTypes == null) {
                acceptedTypes = MediaType.WILDCARD;
            }
            String queryString = t.getRequestURI().getQuery();
            Format format;
            String mediaType;

            if ((queryString != null && Arrays.asList(queryString.split("&")).contains("format=JSON"))
                    || acceptedTypes.contains(MediaType.APPLICATION_JSON)) {
                format = Format.JSON;
                mediaType = MediaType.APPLICATION_JSON;
            } else {
                format = Format.YAML;
                mediaType = "application/x-yaml";
            }

            String response = null;
            try {
                response = OpenApiSerializer.serialize(OpenApiDocument.INSTANCE.get(), format);
            } catch (Throwable e) {
                e.printStackTrace();
                t.getResponseHeaders().add("Content-Type", mediaType);
                OutputStream os = t.getResponseBody();
                if (format == Format.JSON) {
                    os.write("{}".getBytes("UTF-8"));
                } else {
                    os.write("".getBytes("UTF-8"));
                }
                os.flush();
                os.close();
                return;
            }

            t.getResponseHeaders().add("Content-Type", mediaType);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    /**
     * Returns an instance of the test class being run. The subclass must implement
     * this so that the correct test delegate is created *and* its callEndpoint()
     * method can be properly overridden.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public T getDelegate() {
        try {
            ParameterizedType ptype = (ParameterizedType) this.getClass().getGenericSuperclass();
            Class cc = (Class) ptype.getActualTypeArguments()[0];
            return (T) cc.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
