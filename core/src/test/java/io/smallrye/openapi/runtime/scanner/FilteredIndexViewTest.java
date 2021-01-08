package io.smallrye.openapi.runtime.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.junit.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;

public class FilteredIndexViewTest {

    @Test
    public void testAcceptsEmptyConfig() {
        Map<String, Object> properties = new HashMap<>();
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
    }

    @Test
    public void testAccepts_IncludedClass_ExcludedPackage() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "com.example.pkgA.MyBean,com.example.pkgA.MyClass");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example.pkgA");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
    }

    @Test
    public void testAccepts_ExcludedClass_IncludedClassPattern() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "^(?:com.example.pkgA.My.*)$");
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "com.example.pkgA.MyImpl");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_IncludedClassPattern_ExcludedPackage() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "^(?:com.example.pkgA.My.*)$");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example.pkgA");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_ExcludedSimpleClassPattern_NotIncludedClassMatch() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "example.pkgA.MyImpl$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_ExcludedSimpleClassPattern_IncludedClassShorterMatch() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:pkgA.My.*)$");
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "example.pkgA.MyImpl$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_ExcludedSimpleClassPattern_IncludedClassLongerMatch() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:example.pkgA.My.*)$");
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "pkgA.MyImpl$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_IncludedSimpleClassPattern_ExcludedPackage() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:pkgA.My(Bean|Class))$");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example.pkgA");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_IncludedSimpleClassPattern_ExcludedPackagePrefix() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:pkgA.My(Bean|Class))$");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_IncludedSimpleClassPattern_ExcludedPackagePatternDoesNotMatch() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:pkgA.MyBean)$");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "example.pkg[AB]$");
        properties.put(OASConfig.SCAN_PACKAGES, "^(com|org).example");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertTrue(view.accepts(DotName.createSimple("org.example.pkgB.MyImpl")));
    }

    @Test
    public void testAccepts_ExcludedPackageOverridesIncludedPackage() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_PACKAGES, "com.example");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertFalse(view.accepts(DotName.createSimple("com.example.TopLevelClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_IncludedPackageDoesNotMatch() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_PACKAGES, "example.pkgA$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_IncludedPackageOverridesExcludedPackage() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_PACKAGES, "com.example.pkgA");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertFalse(view.accepts(DotName.createSimple("com.example.TopLevelClass")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    public void testAccepts_IncludedPackageExcludesOtherPackage() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_PACKAGES, "com.example.pkgA");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgB.MyImpl")));
    }

    @Test
    public void testAccepts_IncludedClassesImpliesOtherClassesExcluded() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "^com.example.pkgA.My(Bean|Class)$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgB.MyImpl")));
    }

    @Test
    public void testAccepts_EmptyPackage() {
        Map<String, Object> properties = new HashMap<>();
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("int")));
    }

    @Test
    public void testGetAnnotationsWithRepeatable() {
        class Target {
            @APIResponses({
                    @APIResponse(),
                    @APIResponse() })
            void test1(@Parameter() String value1, @Parameter() String value2) {
            }

            @APIResponse
            void test2(@Parameter() String value1, @Parameter() String value2) {
            }
        }

        IndexView index = AugmentedIndexView.augment(IndexScannerTestBase.indexOf(APIResponse.class,
                APIResponses.class,
                Parameter.class,
                Target.class));

        OpenApiConfig config = IndexScannerTestBase.emptyConfig();
        FilteredIndexView view = new FilteredIndexView(index, config);

        Collection<AnnotationInstance> container = view
                .getAnnotationsWithRepeatable(DotName.createSimple(APIResponses.class.getName()), index);
        assertEquals(1, container.size());
        Collection<AnnotationInstance> responses = view
                .getAnnotationsWithRepeatable(DotName.createSimple(APIResponse.class.getName()), index);
        assertEquals(3, responses.size());
        Collection<AnnotationInstance> params = view
                .getAnnotationsWithRepeatable(DotName.createSimple(Parameter.class.getName()), index);
        assertEquals(4, params.size());
    }
}
