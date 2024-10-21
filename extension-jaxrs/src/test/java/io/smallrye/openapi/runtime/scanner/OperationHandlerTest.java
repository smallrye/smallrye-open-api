package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.SmallRyeOpenAPI;

class OperationHandlerTest {

    @Test
    void testOperationHandlerInvoked() throws Exception {
        @jakarta.ws.rs.Path("/names")
        class NameResource {
            @jakarta.ws.rs.GET
            @jakarta.ws.rs.Path("{id}")
            public String getNameById(@jakarta.ws.rs.PathParam("id") String id) {
                return null;
            }
        }

        SmallRyeOpenAPI result = SmallRyeOpenAPI.builder()
                .enableModelReader(false)
                .enableStandardFilter(false)
                .enableStandardStaticFiles(false)
                .enableAnnotationScan(true)
                .withIndex(Index.of(NameResource.class))
                .withOperationHandler((o, c, m) -> {
                    String className = c.simpleName();
                    String methodName = m.name();
                    o.addExtension("x-method-info", className + '#' + methodName);
                })
                .build();

        OpenAPI model = result.model();
        Map<String, Object> extensions = model.getPaths().getPathItem("/names/{id}").getGET().getExtensions();
        String methodInfo = (String) extensions.get("x-method-info");

        assertEquals(NameResource.class.getSimpleName() + "#getNameById", methodInfo);
    }

}
