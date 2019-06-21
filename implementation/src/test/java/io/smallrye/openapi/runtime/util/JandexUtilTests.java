package io.smallrye.openapi.runtime.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.json.JSONException;
import org.junit.Test;

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
}
