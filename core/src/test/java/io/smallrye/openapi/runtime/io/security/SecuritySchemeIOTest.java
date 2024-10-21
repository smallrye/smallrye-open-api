package io.smallrye.openapi.runtime.io.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.OAuthScope;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.runtime.io.IOContext;
import io.smallrye.openapi.runtime.scanner.FilteredIndexView;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

class SecuritySchemeIOTest extends IndexScannerTestBase {

    @Test
    void testReadFlow() {
        FilteredIndexView index = new FilteredIndexView(IndexScannerTestBase.indexOf(Endpoint1.class), emptyConfig());

        AnnotationScannerContext context = new AnnotationScannerContext(index, Thread.currentThread().getContextClassLoader(),
                Collections.emptyList(),
                emptyConfig(), OASFactory.createOpenAPI());

        ClassInfo clazz = index.getClassByName(Endpoint1.class);
        AnnotationInstance annotation = clazz.annotation(SecurityScheme.class);
        AnnotationInstance flowAnnotation = annotation
                .value("flows").asNested()
                .value("implicit").asNested();
        AnnotationInstance scopeAnnotation = flowAnnotation
                .value("scopes").asNestedArray()[0];

        IOContext<?, ?, ?, ?, ?> ioContext = IOContext.forScanning(context);

        OAuthScopeIO<?, ?, ?, ?, ?> scopeIO = ioContext.oauthScopeIO();
        String value = scopeIO.read(scopeAnnotation);
        assertNull(value);

        OAuthFlowIO<?, ?, ?, ?, ?> flowIO = ioContext.oauthFlowIO();
        org.eclipse.microprofile.openapi.models.security.OAuthFlow flow = flowIO.read(flowAnnotation);
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", null);
        assertEquals(expected, flow.getScopes());
    }

    @SecurityScheme(securitySchemeName = "OAuth2Authorization", type = SecuritySchemeType.OAUTH2, description = "authentication needed to delete a profile", flows = @OAuthFlows(implicit = @OAuthFlow(authorizationUrl = "https://example.com", scopes = @OAuthScope(name = "foo"))))
    static class Endpoint1 {
    }
}
