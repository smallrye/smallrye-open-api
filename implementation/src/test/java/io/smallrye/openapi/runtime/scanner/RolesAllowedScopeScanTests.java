package io.smallrye.openapi.runtime.scanner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.OAuthScope;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;

public class RolesAllowedScopeScanTests extends IndexScannerTestBase {

    @Test
    public void testClassRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(RolesAllowedApp.class, RolesAllowedResource1.class);
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
                        .getScopes()
                        .keySet()
                        .iterator().next());
    }

    @Test
    public void testPermitAllWithoutGeneratedScheme() throws IOException {
        Index index = indexOf(RolesAllowedApp.class, RolesAllowedResource1.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertNull(result.getPaths().getPathItem("/v1/open").getGET().getSecurity());
    }

    @Test
    public void testGeneratedSchemeEmptyRoles() throws IOException {
        Index index = indexOf(RolesAllowedApp.class, RolesAllowedResource1.class);
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
    public void testMethodRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(RolesAllowedApp.class, RolesAllowedResource2.class);
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
                        .getScopes()
                        .keySet()
                        .toArray());
    }

    @Test
    public void testNoEligibleScheme() throws IOException {
        Index index = indexOf(RolesNotAllowedApp.class, RolesAllowedResource1.class);
        OpenApiConfig config = emptyConfig();
        IndexView filtered = new FilteredIndexView(index, config);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(config, filtered);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        List<SecurityRequirement> requirements = result.getPaths().getPathItem("/v1/locked").getGET().getSecurity();
        assertNull(requirements);
    }

    @Test
    public void testDeclaredRolesMethodRolesAllowedGeneratedScheme() throws IOException {
        Index index = indexOf(RolesAllowedApp.class, RolesDeclaredResource.class);
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
    public void testSchemesWithoutRoles() throws IOException {
        Index index = indexOf(UndeclaredFlowsNoRolesAllowedApp.class, NoRolesResource.class);
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

    @OpenAPIDefinition(info = @Info(title = "RolesAllowed App", version = "1.0"), components = @Components(securitySchemes = {
            @SecurityScheme(securitySchemeName = "rolesScheme", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(clientCredentials = @OAuthFlow(), implicit = @OAuthFlow(scopes = {
                    @OAuthScope(name = "scope1", description = "Provided by OAI annotation") })))
    }))
    static class RolesAllowedApp extends Application {
    }

    @OpenAPIDefinition(info = @Info(title = "RolesNotAllowed App", version = "1.0"), components = @Components(securitySchemes = {
            @SecurityScheme(securitySchemeName = "noTypeScheme", flows = @OAuthFlows(clientCredentials = @OAuthFlow(), implicit = @OAuthFlow(scopes = {
                    @OAuthScope(name = "scope1", description = "Provided by OAI annotation") })))
    }))
    static class RolesNotAllowedApp extends Application {
    }

    @OpenAPIDefinition(info = @Info(title = "UndeclaredFlowsNoRolesAllowed App", version = "1.0"))
    // Single scheme missing 'flows'
    @SecuritySchemes(value = {
            @SecurityScheme(securitySchemeName = "oidc", type = SecuritySchemeType.OPENIDCONNECT, openIdConnectUrl = "https://example.com/auth/realms/custom_realm/.well-known/openid-configuration") })
    static class UndeclaredFlowsNoRolesAllowedApp extends Application {
    }

    @Path("/v1")
    @RolesAllowed("admin")
    @SuppressWarnings("unused")
    static class RolesAllowedResource1 {
        @GET
        @Path("secured")
        @Produces("application/json")
        public Response getSecuredData(int id) {
            return null;
        }

        @GET
        @Path("open")
        @Produces("application/json")
        @PermitAll
        public Response getOpenData(int id) {
            return null;
        }

        @GET
        @Path("locked")
        @Produces("application/json")
        @DenyAll
        public Response getLockedData(int id) {
            return null;
        }
    }

    @Path("/v2")
    @SuppressWarnings("unused")
    static class RolesAllowedResource2 {
        @GET
        @Path("secured")
        @Produces("application/json")
        @RolesAllowed({ "admin", "users" })
        public Response getSecuredData(int id) {
            return null;
        }
    }

    @Path("/v1")
    @SuppressWarnings("unused")
    @DeclareRoles({ "admin", "users" })
    static class RolesDeclaredResource {
        @GET
        @Path("secured")
        @Produces("application/json")
        @RolesAllowed({ "admin" })
        public Response getSecuredData(int id) {
            return null;
        }
    }

    @Path("/v1")
    @SuppressWarnings("unused")
    static class NoRolesResource {
        @GET
        @Path("secured")
        @Produces("application/json")
        @RolesAllowed({ "admin" })
        public Response getSecuredData(int id) {
            return null;
        }
    }
}
