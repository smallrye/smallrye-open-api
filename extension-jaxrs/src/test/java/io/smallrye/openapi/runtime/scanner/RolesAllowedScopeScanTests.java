package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;

class RolesAllowedScopeScanTests extends IndexScannerTestBase {

    @Test
    void testJavaxClassRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.RolesAllowedResource1.class);

        testClassRolesAllowedGeneratedScheme(index);
    }

    @Test
    void testJakartaClassRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedResource1.class);

        testClassRolesAllowedGeneratedScheme(index);
    }

    void testClassRolesAllowedGeneratedScheme(Index index) throws IOException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        SecurityRequirement requirement = result.getPaths().getPathItem("/v1/secured").getGET().getSecurity().get(0);
        assertNotNull(requirement);
        assertEquals(1, requirement.getScheme("rolesScheme").size());
        assertEquals("admin", requirement.getScheme("rolesScheme").get(0));
        assertEquals("admin",
                result.getComponents()
                        .getSecuritySchemes()
                        .get("rolesScheme")
                        .getFlows()
                        .getClientCredentials()
                        .getScopes()
                        .keySet()
                        .iterator().next());
    }

    @Test
    void testJavaxPermitAllWithoutGeneratedScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.RolesAllowedResource1.class);
        testPermitAllWithoutGeneratedScheme(index);
    }

    @Test
    void testJakartaPermitAllWithoutGeneratedScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedResource1.class);
        testPermitAllWithoutGeneratedScheme(index);
    }

    void testPermitAllWithoutGeneratedScheme(Index index) throws IOException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertNull(result.getPaths().getPathItem("/v1/open").getGET().getSecurity());
    }

    @Test
    void testJavaxGeneratedSchemeEmptyRoles() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.RolesAllowedResource1.class);
        testGeneratedSchemeEmptyRoles(index);
    }

    @Test
    void testJakartaGeneratedSchemeEmptyRoles() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedResource1.class);
        testGeneratedSchemeEmptyRoles(index);
    }

    void testGeneratedSchemeEmptyRoles(Index index) throws IOException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        SecurityRequirement requirement = result.getPaths().getPathItem("/v1/locked").getGET().getSecurity().get(0);
        assertNotNull(requirement);
        assertEquals(0, requirement.getScheme("rolesScheme").size());
    }

    @Test
    void testJavaxMethodRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.RolesAllowedResource2.class);
        testMethodRolesAllowedGeneratedScheme(index);
    }

    @Test
    void testJakartaMethodRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedResource2.class);
        testMethodRolesAllowedGeneratedScheme(index);
    }

    void testMethodRolesAllowedGeneratedScheme(Index index) throws IOException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        SecurityRequirement requirement = result.getPaths().getPathItem("/v2/secured").getGET().getSecurity().get(0);
        assertNotNull(requirement);
        assertEquals(2, requirement.getScheme("rolesScheme").size());
        assertEquals("admin", requirement.getScheme("rolesScheme").get(0));
        assertEquals("users", requirement.getScheme("rolesScheme").get(1));
        assertArrayEquals(new String[] { "admin", "users" },
                result.getComponents()
                        .getSecuritySchemes()
                        .get("rolesScheme")
                        .getFlows()
                        .getClientCredentials()
                        .getScopes()
                        .keySet()
                        .toArray());
    }

    @Test
    void testJavaxNoEligibleScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.RolesNotAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.RolesAllowedResource1.class);
        testNoEligibleScheme(index);
    }

    @Test
    void testJakartaNoEligibleScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.RolesNotAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedResource1.class);
        testNoEligibleScheme(index);
    }

    void testNoEligibleScheme(Index index) throws IOException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        List<SecurityRequirement> requirements = result.getPaths().getPathItem("/v1/locked").getGET().getSecurity();
        assertNull(requirements);
    }

    @Test
    void testJavaxDeclaredRolesMethodRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.RolesDeclaredResource.class);
        testDeclaredRolesMethodRolesAllowedGeneratedScheme(index);
    }

    @Test
    void testJakartaDeclaredRolesMethodRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.RolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.RolesDeclaredResource.class);
        testDeclaredRolesMethodRolesAllowedGeneratedScheme(index);
    }

    void testDeclaredRolesMethodRolesAllowedGeneratedScheme(Index index) throws IOException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        SecurityRequirement requirement = result.getPaths().getPathItem("/v1/secured").getGET().getSecurity().get(0);
        assertNotNull(requirement);
        assertEquals(1, requirement.getScheme("rolesScheme").size());
        assertEquals("admin", requirement.getScheme("rolesScheme").get(0));
        assertArrayEquals(new String[] { "admin", "users" },
                result.getComponents()
                        .getSecuritySchemes()
                        .get("rolesScheme")
                        .getFlows()
                        .getClientCredentials()
                        .getScopes()
                        .keySet()
                        .toArray());
    }

    /*
     * Test case derived for Smallrye OpenAPI issue #240.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/240
     *
     */
    @Test
    void testJavaxSchemesWithoutRoles() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.UndeclaredFlowsNoRolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.NoRolesResource.class);
        testSchemesWithoutRoles(index);
    }

    @Test
    void testJakartaSchemesWithoutRoles() throws IOException {
        Index index = indexOf(test.io.smallrye.openapi.runtime.scanner.jakarta.UndeclaredFlowsNoRolesAllowedApp.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.NoRolesResource.class);
        testSchemesWithoutRoles(index);
    }

    void testSchemesWithoutRoles(Index index) throws IOException {
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        SecurityRequirement requirement = result.getPaths().getPathItem("/v1/secured").getGET().getSecurity().get(0);
        assertNotNull(requirement);
        assertEquals(1, requirement.getScheme("oidc").size());
        assertEquals("admin", requirement.getScheme("oidc").get(0));
        assertNull(result.getComponents()
                .getSecuritySchemes()
                .get("oidc")
                .getFlows());
    }
}
