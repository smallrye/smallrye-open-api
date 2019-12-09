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

import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.tck.utils.YamlToJsonFilter;
import org.jboss.arquillian.testng.Arquillian;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.restassured.RestAssured;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;

/**
 * Base class for all Tck tests.
 * 
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("restriction")
@RunWith(TckTestRunner.class)
public abstract class BaseTckTest<T extends Arquillian> {

    protected static final String APPLICATION_JSON = "application/json";
    protected static final String TEXT_PLAIN = "text/plain";

    private static HttpServer server;
    private static int HTTP_PORT;

    @BeforeClass
    public static final void setUp() throws Exception {
        String portEnv = System.getProperty("smallrye.openapi.server.port");
        if (portEnv == null || portEnv.isEmpty()) {
            portEnv = "8082";
        }
        HTTP_PORT = Integer.valueOf(portEnv);
        // Set RestAssured default port directly. A bit nasty, but we have no easy way to change
        // AppTestBase#callEndpoint in the upstream. They also seem to do it this way, so it's no worse
        // than what's there.
        RestAssured.port = HTTP_PORT;
        // Set up a little HTTP server so that Rest assured has something to pull /openapi from
        System.out.println("Starting TCK test server on port " + HTTP_PORT);
        server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        server.createContext("/openapi", new MyHandler());
        server.setExecutor(null);
        server.start();

        // Register a filter that performs YAML to JSON conversion
        // Called here because the TCK's AppTestBase#setUp() is not called. (Remove for 2.0)
        RestAssured.filters(new YamlToJsonFilter());
    }

    @AfterClass
    public static final void tearDown() throws Exception {
        server.stop(0);
        Thread.sleep(100);
        System.out.println("TCK test server stopped.");
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
     * Returns an instance of the TCK test being run. The subclass must implement
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
