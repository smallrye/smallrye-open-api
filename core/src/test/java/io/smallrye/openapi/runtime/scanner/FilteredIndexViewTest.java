package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;

class FilteredIndexViewTest {

    @Test
    void testAcceptsEmptyConfig() {
        Map<String, String> properties = new HashMap<>();
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
    }

    @Test
    void testAccepts_IncludedClass_ExcludedPackage() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "com.example.pkgA.MyBean,com.example.pkgA.MyClass");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example.pkgA");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
    }

    @Test
    void testAccepts_ExcludedClass_IncludedClassPattern() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "^(?:com.example.pkgA.My.*)$");
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "com.example.pkgA.MyImpl");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    void testAccepts_IncludedClassPattern_ExcludedPackage() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "^(?:com.example.pkgA.My.*)$");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example.pkgA");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    void testAccepts_ExcludedSimpleClassPattern_NotIncludedClassMatch() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "example.pkgA.MyImpl$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    void testAccepts_ExcludedSimpleClassPattern_IncludedClassShorterMatch() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:pkgA.My.*)$");
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "example.pkgA.MyImpl$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    void testAccepts_ExcludedSimpleClassPattern_IncludedClassLongerMatch() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:example.pkgA.My.*)$");
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "pkgA.MyImpl$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    void testAccepts_IncludedSimpleClassPattern_ExcludedPackage() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:pkgA.My(Bean|Class))$");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example.pkgA");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    void testAccepts_IncludedSimpleClassPattern_ExcludedPackagePrefix() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:pkgA.My(Bean|Class))$");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "com.example");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyImpl")));
    }

    @Test
    void testAccepts_IncludedSimpleClassPattern_ExcludedPackagePattern() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "(?:pkgA.MyBean)$");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "example.pkg[AB]$");
        properties.put(OASConfig.SCAN_PACKAGES, "^(com|org).example");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("org.example.pkgB.MyImpl")));
    }

    @Test
    void testAccepts_IncludedSimpleClassPattern_ExcludedSimpleClass() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "Resource$");
        properties.put(OASConfig.SCAN_EXCLUDE_CLASSES, "BarResource");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.FooResource")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.BarResource")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.BazDataObject")));
    }

    @Test
    void testAccepts_ExcludedPackageOverridesIncludedPackage() {
        Map<String, String> properties = new HashMap<>();
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
    void testAccepts_IncludedPackagePatternEndAnchor() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_PACKAGES, "example.pkgA$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgA.pkgB.MyImpl")));
    }

    @Test
    void testAccepts_IncludedPackageOverridesExcludedPackage() {
        Map<String, String> properties = new HashMap<>();
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
    void testAccepts_IncludedPackageExcludesOtherPackage() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_PACKAGES, "com.example.pkgA");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgB.MyImpl")));
    }

    @Test
    void testAccepts_IncludedClassesImpliesOtherClassesExcluded() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_CLASSES, "^com.example.pkgA.My(Bean|Class)$");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyBean")));
        assertTrue(view.accepts(DotName.createSimple("com.example.pkgA.MyClass")));
        assertFalse(view.accepts(DotName.createSimple("com.example.pkgB.MyImpl")));
    }

    @Test
    void testAccepts_IncludedPackageAroundExcludedPackage() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_PACKAGES, "org.example.a, org.example.a.b.c");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "org.example.a.b");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("org.example.a.MyClassA")));
        assertFalse(view.accepts(DotName.createSimple("org.example.a.b.MyClassB")));
        assertTrue(view.accepts(DotName.createSimple("org.example.a.b.c.MyClassC")));
    }

    @Test
    void testAccepts_ExcludedPackageAroundIncludedPackage() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OASConfig.SCAN_PACKAGES, "org.example.a.b");
        properties.put(OASConfig.SCAN_EXCLUDE_PACKAGES, "org.example.a, org.example.a.b.c");
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertFalse(view.accepts(DotName.createSimple("org.example.a.MyClassA")));
        assertTrue(view.accepts(DotName.createSimple("org.example.a.b.MyClassB")));
        assertFalse(view.accepts(DotName.createSimple("org.example.a.b.c.MyClassC")));
    }

    @Test
    void testAccepts_EmptyPackage() {
        Map<String, String> properties = new HashMap<>();
        OpenApiConfig config = IndexScannerTestBase.dynamicConfig(properties);
        FilteredIndexView view = new FilteredIndexView(null, config);
        assertTrue(view.accepts(DotName.createSimple("int")));
    }

    @Test
    void testGetAnnotationsWithRepeatable() {
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
