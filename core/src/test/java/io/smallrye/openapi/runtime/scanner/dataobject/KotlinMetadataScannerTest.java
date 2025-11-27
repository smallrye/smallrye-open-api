package io.smallrye.openapi.runtime.scanner.dataobject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KotlinMetadataScannerTest {

    KotlinMetadataScanner target;

    @BeforeEach
    void setup() {
        target = new KotlinMetadataScanner();
    }

    @ParameterizedTest
    @CsvSource({
            "p1, true,  false",
            "p2, false, false",
            "p3, true,  true",
            "p4, false, true",
    })
    void testMethodParameterAttributesApplied(String paramName, boolean required, boolean nullable) {
        IndexView index = index("io.smallrye.openapi.runtime.scanner.dataobject.KotlinResource");
        ClassInfo clazz = index.getClassByName("io.smallrye.openapi.runtime.scanner.dataobject.KotlinResource");
        Type stringType = Type.create(String.class);
        MethodInfo greet0 = clazz.method("greet1", List.of(stringType, stringType, stringType, stringType));
        MethodParameterInfo param = greet0.parameters()
                .stream()
                .filter(p -> greet0.parameterName(p.position()).equals(paramName))
                .findFirst()
                .orElseThrow();

        Schema schema = OASFactory.createSchema().addType(Schema.SchemaType.STRING);
        AtomicBoolean actualRequired = new AtomicBoolean();

        target.applyMetadata(param, schema, paramName, (unused, key) -> {
            actualRequired.set(true);
        });

        assertEquals(required, actualRequired.get());
        assertEquals(nullable, schema.getType().contains(Schema.SchemaType.NULL));
    }

    @ParameterizedTest
    @CsvSource({
            "nonnullableNoDefault,         true,  false",
            "nonnullableWithDefault,       false, false",
            "nullableNoDefault,            true,  true",
            "nullableWithDefault,          false, true",
            "classBodyNullableWithDefault, false, true",
            "nonPropertyField,             false, false",
    })
    void testPropertyAttributesApplied(String propName, boolean required, boolean nullable) {
        IndexView index = index("io.smallrye.openapi.runtime.scanner.dataobject.KotlinBean");
        ClassInfo clazz = index.getClassByName("io.smallrye.openapi.runtime.scanner.dataobject.KotlinBean");
        FieldInfo field = clazz.field(propName);

        Schema schema = OASFactory.createSchema().addType(Schema.SchemaType.STRING);
        AtomicBoolean actualRequired = new AtomicBoolean();

        target.applyMetadata(field, schema, propName, (unused, key) -> {
            actualRequired.set(true);
        });

        assertEquals(required, actualRequired.get());
        assertEquals(nullable, schema.getType().contains(Schema.SchemaType.NULL));
    }

    static IndexView index(String... classNames) {
        List<Class<?>> classes = new ArrayList<>(classNames.length);

        for (String className : classNames) {
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return Index.of(classes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
