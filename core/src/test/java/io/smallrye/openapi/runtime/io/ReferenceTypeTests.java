package io.smallrye.openapi.runtime.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.junit.jupiter.api.Test;

import io.smallrye.openapi.model.ReferenceType;

class ReferenceTypeTests {

    @Test
    void testRefValueWithHttpUrl() {
        String ref = "https://www.example.com/openapi";
        AnnotationInstance annotation = AnnotationInstance.create(DotName.createSimple(""),
                null,
                Arrays.asList(AnnotationValue.createStringValue("ref", ref)));
        String outRef = ReferenceType.LINK.refValue(annotation);
        assertEquals(ref, outRef);
    }

    @Test
    void testRefValueWithRelativeUrl() {
        String ref = "./additional-schemas.json";
        AnnotationInstance annotation = AnnotationInstance.create(DotName.createSimple(""),
                null,
                Arrays.asList(AnnotationValue.createStringValue("ref", ref)));
        String outRef = ReferenceType.LINK.refValue(annotation);
        assertEquals(ref, outRef);
    }

    @Test
    void testRefValueWithValidLinkName() {
        String ref = "L1nk.T0_Something-Useful";
        AnnotationInstance annotation = AnnotationInstance.create(DotName.createSimple(""),
                null,
                Arrays.asList(AnnotationValue.createStringValue("ref", ref)));
        String outRef = ReferenceType.LINK.refValue(annotation);
        assertEquals("#/components/links/L1nk.T0_Something-Useful", outRef);
    }

}
