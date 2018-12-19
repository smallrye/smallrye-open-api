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

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;

import org.eclipse.microprofile.openapi.tck.FilterTest;
import org.jboss.arquillian.testng.Arquillian;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.smallrye.openapi.api.OpenApiDocument;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import io.smallrye.openapi.runtime.io.OpenApiSerializer.Format;

/**
 * Base class for all Tck tests.
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
    }

    @AfterClass
    public static final void tearDown() throws Exception {
        server.stop(0);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = null;
            try {
                response = OpenApiSerializer.serialize(OpenApiDocument.INSTANCE.get(), Format.JSON);
            } catch (Throwable e) {
                e.printStackTrace();
                t.getResponseHeaders().add("Content-Type", APPLICATION_JSON);
                OutputStream os = t.getResponseBody();
                os.write("{}".getBytes("UTF-8"));
                os.flush();
                os.close();
                return;
            }

            t.getResponseHeaders().add("Content-Type", APPLICATION_JSON);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    /**
     * Calls the endpoint.
     * @param format
     */
    protected ValidatableResponse doCallEndpoint(String format) {
        ValidatableResponse vr;
        vr = given().accept(APPLICATION_JSON).when().get("/openapi").then().statusCode(200);
        return vr;
    }

    /**
     * Returns an instance of the TCK test being run.  The subclass must implement
     * this so that the correct test delegate is created *and* its callEndpoint()
     * method can be properly overridden.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public T getDelegate() {
        try {
            ParameterizedType ptype = (ParameterizedType) this.getClass().getGenericSuperclass();
            Class cc = (Class)ptype.getActualTypeArguments()[0];
            return (T) cc.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Arguments to pass to each of the test methods in the TCK test.  This is
     * typically null (no arguments) but at least one test ( {@link FilterTest} )
     * has arguments to its methods.
     */
    public Object[] getTestArguments() {
        return new String[] { "JSON" };
    }

}
