package io.smallrye.openapi.runtime.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.Path;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.util.JandexUtil.RefType;

public class JandexUtilTests {

    @Test
    public void testRefValueWithHttpUrl() throws IOException, JSONException {
        String ref = "https://www.example.com/openapi";
        AnnotationInstance annotation = AnnotationInstance.create(DotName.createSimple(""),
                null,
                Arrays.asList(AnnotationValue.createStringValue("ref", ref)));
        String outRef = JandexUtil.refValue(annotation, RefType.Link);
        assertEquals(ref, outRef);
    }

    @Test
    public void testRefValueWithRelativeUrl() throws IOException, JSONException {
        String ref = "./additional-schemas.json";
        AnnotationInstance annotation = AnnotationInstance.create(DotName.createSimple(""),
                null,
                Arrays.asList(AnnotationValue.createStringValue("ref", ref)));
        String outRef = JandexUtil.refValue(annotation, RefType.Link);
        assertEquals(ref, outRef);
    }

    @Test
    public void testRefValueWithValidLinkName() throws IOException, JSONException {
        String ref = "L1nk.T0_Something-Useful";
        AnnotationInstance annotation = AnnotationInstance.create(DotName.createSimple(""),
                null,
                Arrays.asList(AnnotationValue.createStringValue("ref", ref)));
        String outRef = JandexUtil.refValue(annotation, RefType.Link);
        assertEquals("#/components/links/L1nk.T0_Something-Useful", outRef);
    }

    @Test
    public void testGetJaxRsResourceClasses() throws IOException, JSONException {
        Index index = IndexScannerTestBase.indexOf(I1.class, I2.class, Implementor1.class, Implementor2.class);
        Collection<ClassInfo> resources = JandexUtil.getJaxRsResourceClasses(index);
        assertEquals(3, resources.size());
        assertTrue(resources.contains(index.getClassByName(DotName.createSimple(I2.class.getName()))));
        assertTrue(resources.contains(index.getClassByName(DotName.createSimple(Implementor1.class.getName()))));
        assertTrue(resources.contains(index.getClassByName(DotName.createSimple(Implementor2.class.getName()))));
    }

    @Path("interface1")
    interface I1 {
        @Path("method1")
        public String getData();
    }

    @Path("interface2")
    interface I2 {
        @Path("method1")
        public String getData();
    }

    @Path("implementation1")
    static abstract class Implementor1 implements I1 {
    }

    @Path("implementation2")
    static class Implementor2 implements I2 {
        @Override
        public String getData() {
            return null;
        }
    }

}
