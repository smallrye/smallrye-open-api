package io.smallrye.openapi.runtime.scanner.dataobject;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.Annotations;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class TypeResolverTests extends IndexScannerTestBase {

    private Map<String, TypeResolver> getProperties(Class<?> leafClass, OpenApiConfig config, Class<?>... indexClasses) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        AnnotationScannerContext context = new AnnotationScannerContext(indexOf(indexClasses), loader, config);
        ClassInfo leafKlazz = context.getIndex().getClassByName(componentize(leafClass.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        return TypeResolver.getAllFields(context, leaf, leafKlazz, null);
    }

    private Map<String, TypeResolver> getProperties(Class<?> leafClass, Class<?>... indexClasses) {
        return getProperties(leafClass, emptyConfig(), indexClasses);
    }

    @Test
    void testJavaxAnnotatedMethodOverridesParentSchema() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Animal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Feline.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.Cat.class);

        testAnnotatedMethodOverridesParentSchema(properties);
    }

    @Test
    void testJakartaAnnotatedMethodOverridesParentSchema() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Animal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Feline.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.Cat.class);

        testAnnotatedMethodOverridesParentSchema(properties);
    }

    void testAnnotatedMethodOverridesParentSchema(Map<String, TypeResolver> properties) {
        TypeResolver resolver = properties.get("type");
        assertEquals(Kind.METHOD, resolver.getAnnotationTarget().kind());
        AnnotationInstance schema = TypeUtil.getSchemaAnnotation(resolver.getAnnotationTarget());
        assertEquals("type", schema.value("name").asString());
        assertEquals(false, schema.value("required").asBoolean());
        assertEquals("Cat", schema.value("example").asString());
        assertArrayEquals(new String[] { "age", "type", "name", "speciesName", "extinct" },
                properties.values().stream().map(TypeResolver::getPropertyName).toArray());
    }

    @Test
    void testJavaxAnnotatedFieldsOverridesInterfaceSchema() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Animal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Feline.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.Cat.class);
        testAnnotatedFieldsOverridesInterfaceSchema(properties);
    }

    @Test
    void testJakartaAnnotatedFieldsOverridesInterfaceSchema() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Animal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Feline.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.Cat.class);
        testAnnotatedFieldsOverridesInterfaceSchema(properties);
    }

    void testAnnotatedFieldsOverridesInterfaceSchema(Map<String, TypeResolver> properties) {
        TypeResolver resolver = properties.get("name");
        assertEquals(Kind.FIELD, resolver.getAnnotationTarget().kind());
        AnnotationInstance schema = TypeUtil.getSchemaAnnotation(resolver.getAnnotationTarget());
        assertEquals(true, schema.value("required").asBoolean());
        assertEquals("Felix", schema.value("example").asString());
    }

    @Test
    void testJavaxAnnotatedInterfaceMethodOverridesImplMethod() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Animal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.Dog.class);

        testAnnotatedInterfaceMethodOverridesImplMethod(properties);
    }

    @Test
    void testJakartaAnnotatedInterfaceMethodOverridesImplMethod() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Animal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.Dog.class);

        testAnnotatedInterfaceMethodOverridesImplMethod(properties);
    }

    void testAnnotatedInterfaceMethodOverridesImplMethod(Map<String, TypeResolver> properties) {
        assertEquals(6, properties.size());
        TypeResolver speciesNameResolver = properties.get("speciesName");
        assertEquals(Kind.METHOD, speciesNameResolver.getAnnotationTarget().kind());
        TypeResolver nameResolver = properties.get("name");
        assertEquals(Kind.METHOD, nameResolver.getAnnotationTarget().kind());
        AnnotationInstance schema = TypeUtil.getSchemaAnnotation(nameResolver.getAnnotationTarget());
        assertEquals("c_name", schema.value("name").asString());
        assertEquals(50, schema.value("maxLength").asInt());
        assertEquals("The name of the canine", schema.value("description").asString());
        assertArrayEquals(new String[] { "age", "type", "c_name", "bark", "speciesName", "extinct" },
                properties.values().stream().map(TypeResolver::getPropertyName).toArray());
    }

    @Test
    void testAnnotatedInterfaceMethodOverridesStaticField() {
        Map<String, TypeResolver> properties = getProperties(test.io.smallrye.openapi.runtime.scanner.dataobject.Lizard.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Animal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.AbstractAnimal.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Reptile.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.Lizard.class);

        TypeResolver resolver = properties.get("scaleColor");
        assertEquals(Kind.METHOD, resolver.getAnnotationTarget().kind());
        AnnotationInstance schema = TypeUtil.getSchemaAnnotation(resolver.getAnnotationTarget());
        assertEquals("scaleColor", schema.value("name").asString());
        assertNull(schema.value("deprecated"));
        assertEquals("The color of a reptile's scales", schema.value("description").asString());

        TypeResolver ageResolver = properties.get("age");
        assertEquals(Type.Kind.CLASS, ageResolver.getUnresolvedType().kind());
        assertEquals(DotName.createSimple(String.class.getName()), ageResolver.getUnresolvedType().name());
    }

    @Test
    void testJavaxBareInterface() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.MySchema.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.MySchema.class);

        testBareInterface(properties);
    }

    @Test
    void testJakartaBareInterface() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.MySchema.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.MySchema.class);

        testBareInterface(properties);
    }

    void testBareInterface(Map<String, TypeResolver> properties) {
        assertEquals(3, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        assertEquals("field1", iter.next().getKey());
        assertEquals("field3", iter.next().getKey());
        assertEquals("field2", iter.next().getKey());

        TypeResolver field1 = properties.get("field1");
        assertEquals(Kind.METHOD, field1.getAnnotationTarget().kind());
        AnnotationInstance schema1 = TypeUtil.getSchemaAnnotation(field1.getAnnotationTarget());
        assertEquals(1, schema1.values().size());
        assertEquals(true, schema1.value("required").asBoolean());

        TypeResolver field2 = properties.get("field2");
        assertEquals(Kind.METHOD, field1.getAnnotationTarget().kind());
        AnnotationInstance schema2 = TypeUtil.getSchemaAnnotation(field2.getAnnotationTarget());
        assertEquals(1, schema2.values().size());
        assertEquals("anotherField", schema2.value("name").asString());

        TypeResolver field3 = properties.get("field3");
        assertEquals(Kind.METHOD, field3.getAnnotationTarget().kind());
        AnnotationInstance schema3 = TypeUtil.getSchemaAnnotation(field3.getAnnotationTarget());
        assertNull(schema3);
    }

    @Test
    void testJacksonPropertyOrderDefault() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.JacksonPropertyOrderDefault.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.JacksonPropertyOrderDefault.class);
        assertEquals(4, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        assertEquals("comment", iter.next().getValue().getPropertyName());
        assertEquals("theName", iter.next().getValue().getPropertyName());
    }

    @Test
    void testJacksonPropertyOrderCustomName() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.JacksonPropertyOrderCustomName.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.JacksonPropertyOrderCustomName.class);
        assertEquals(4, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        assertEquals("theName", iter.next().getValue().getPropertyName());
        assertEquals("comment2ActuallyFirst", iter.next().getValue().getPropertyName());
        assertEquals("comment", iter.next().getValue().getPropertyName());
    }

    @Test
    void testJavaxJaxbCustomPropertyOrder() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.JaxbCustomPropertyOrder.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.JaxbCustomPropertyOrder.class);

        testJaxbCustomPropertyOrder(properties);
    }

    @Test
    void testJakartaJaxbCustomPropertyOrder() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.JaxbCustomPropertyOrder.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.JaxbCustomPropertyOrder.class);

        testJaxbCustomPropertyOrder(properties);
    }

    void testJaxbCustomPropertyOrder(Map<String, TypeResolver> properties) {
        assertEquals(4, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        assertEquals("comment", iter.next().getValue().getPropertyName());
        assertEquals("name2", iter.next().getValue().getPropertyName());
        assertEquals("name", iter.next().getValue().getPropertyName());
        assertEquals("comment2", iter.next().getValue().getPropertyName());
    }

    @Test
    void testNonJavaBeansPropertyAccessor() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.NonJavaBeanAccessorProperty.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.NonJavaBeanAccessorProperty.class);
        assertEquals(1, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        TypeResolver property = iter.next().getValue();
        assertEquals("name", property.getPropertyName());
        assertEquals(property.getReadMethod(), property.getAnnotationTarget());
        assertEquals("Name of the property", Annotations.getAnnotationValue(property.getAnnotationTarget(),
                SchemaConstant.DOTNAME_SCHEMA,
                SchemaConstant.PROP_TITLE));
    }

    @Test
    void testNonJavaBeansPropertyMutator() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.NonJavaBeanMutatorProperty.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.NonJavaBeanMutatorProperty.class);
        assertEquals(1, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        TypeResolver property = iter.next().getValue();
        assertEquals("name", property.getPropertyName());
        assertEquals(property.getWriteMethod(), property.getAnnotationTarget());
        assertEquals("Name of the property", Annotations.getAnnotationValue(property.getAnnotationTarget(),
                SchemaConstant.DOTNAME_SCHEMA,
                SchemaConstant.PROP_TITLE));
    }

    @Test
    void testOneSidedPropertiesHidden() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.OneSidedProperties.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.OneSidedProperties.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.OneSidedParent.class);
        assertEquals(5, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();

        TypeResolver parentProp1 = iter.next().getValue();
        assertEquals("parentProp1", parentProp1.getPropertyName());
        assertTrue(parentProp1.isIgnored());

        TypeResolver parentProp2 = iter.next().getValue();
        assertEquals("parentProp2", parentProp2.getPropertyName());
        assertTrue(parentProp2.isIgnored());

        TypeResolver prop1 = iter.next().getValue();
        assertEquals("prop1", prop1.getPropertyName());
        assertFalse(prop1.isIgnored());

        TypeResolver prop2 = iter.next().getValue();
        assertEquals("prop2", prop2.getPropertyName());
        assertFalse(prop2.isIgnored());

        TypeResolver prop3 = iter.next().getValue();
        assertEquals("prop3", prop3.getPropertyName());
        assertFalse(prop3.isIgnored());
    }

    @Test
    void testJavaxXmlAccessTransientField() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlTransientField.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlTransientField.class);

        testXmlAccessTransientField(properties);
    }

    @Test
    void testJakartaXmlAccessTransientField() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlTransientField.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlTransientField.class);

        testXmlAccessTransientField(properties);
    }

    void testXmlAccessTransientField(Map<String, TypeResolver> properties) {
        assertEquals(2, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        TypeResolver property = iter.next().getValue();
        assertEquals("prop1Field", property.getPropertyName());
        assertTrue(property.isIgnored());
        property = iter.next().getValue();
        assertEquals("prop2Field", property.getPropertyName());
        assertFalse(property.isIgnored());
    }

    @Test
    void testJavaxXmlAccessTransientClass() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlTransientClass.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlTransientClass.class);

        testXmlAccessTransientClass(properties);
    }

    @Test
    void testJakartaXmlAccessTransientClass() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlTransientClass.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlTransientClass.class);

        testXmlAccessTransientClass(properties);
    }

    void testXmlAccessTransientClass(Map<String, TypeResolver> properties) {
        assertEquals(3, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();

        TypeResolver property = iter.next().getValue();
        assertEquals("prop1Field", property.getPropertyName());
        assertTrue(property.isIgnored());

        property = iter.next().getValue();
        assertEquals("prop2Field", property.getPropertyName());
        assertTrue(property.isIgnored());

        property = iter.next().getValue();
        assertEquals("prop3Property", property.getPropertyName());
        assertTrue(property.isIgnored());
    }

    @Test
    void testJavaxXmlAccessPublicMember() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlAccessTypePublicMember.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlAccessTypePublicMember.class);

        testXmlAccessPublicMember(properties);
    }

    @Test
    void testJakartaXmlAccessPublicMember() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlAccessTypePublicMember.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlAccessTypePublicMember.class);

        testXmlAccessPublicMember(properties);
    }

    void testXmlAccessPublicMember(Map<String, TypeResolver> properties) {
        assertEquals(3, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();

        TypeResolver property = iter.next().getValue();
        assertEquals("prop1Field", property.getPropertyName());
        assertFalse(property.isIgnored());

        property = iter.next().getValue();
        assertEquals("prop2Field", property.getPropertyName());
        assertTrue(property.isIgnored());

        property = iter.next().getValue();
        assertEquals("prop3Property", property.getPropertyName());
        assertFalse(property.isIgnored());
    }

    @Test
    void testJavaxXmlAccessTypeFieldOnly() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlAccessTypeFieldOnly.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlAccessTypeFieldOnly.class);

        testXmlAccessTypeFieldOnly(properties);
    }

    @Test
    void testJakartaXmlAccessTypeFieldOnly() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlAccessTypeFieldOnly.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlAccessTypeFieldOnly.class);

        testXmlAccessTypeFieldOnly(properties);
    }

    void testXmlAccessTypeFieldOnly(Map<String, TypeResolver> properties) {
        assertEquals(2, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();

        TypeResolver property = iter.next().getValue();
        assertEquals("prop1Field", property.getPropertyName());
        assertFalse(property.isIgnored());

        property = iter.next().getValue();
        assertEquals("prop2Property", property.getPropertyName());
        assertTrue(property.isIgnored());
    }

    @Test
    void testJavaxXmlAccessTypePropertyOnly() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlAccessTypePropertyOnly.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.javax.XmlAccessTypePropertyOnly.class);

        testXmlAccessTypePropertyOnly(properties);
    }

    @Test
    void testJakartaXmlAccessTypePropertyOnly() {
        Map<String, TypeResolver> properties = getProperties(
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlAccessTypePropertyOnly.class,
                test.io.smallrye.openapi.runtime.scanner.dataobject.jakarta.XmlAccessTypePropertyOnly.class);

        testXmlAccessTypePropertyOnly(properties);
    }

    void testXmlAccessTypePropertyOnly(Map<String, TypeResolver> properties) {
        assertEquals(2, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        TypeResolver property;

        property = iter.next().getValue();
        assertEquals("prop2Field", property.getPropertyName());
        assertTrue(property.isIgnored());

        property = iter.next().getValue();
        assertEquals("prop1Property", property.getPropertyName());
        assertFalse(property.isIgnored());
    }

    @Test
    void testPrivatePropertyHidden() {
        @SuppressWarnings("unused")
        class Test {
            private String field1;
            public String field2;
            protected String field3;
            String field4;
        }

        Map<String, TypeResolver> properties = getProperties(Test.class,
                dynamicConfig(OpenApiConstants.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, false),
                Test.class);

        assertEquals(4, properties.size());
        assertTrue(properties.get("field1").isIgnored());
        assertFalse(properties.get("field2").isIgnored());
        assertTrue(properties.get("field3").isIgnored());
        assertTrue(properties.get("field4").isIgnored());
    }

    @Test
    void testPrivatePropertyExposedWithSchema() {
        @SuppressWarnings("unused")
        class Test {
            @Schema(hidden = false)
            private String field1;
            public String field2;
            @Schema(hidden = false)
            protected String field3;
            @Schema(hidden = false)
            String field4;
        }

        Map<String, TypeResolver> properties = getProperties(Test.class,
                dynamicConfig(OpenApiConstants.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, false),
                Test.class);

        assertEquals(4, properties.size());
        assertFalse(properties.get("field1").isIgnored());
        assertFalse(properties.get("field2").isIgnored());
        assertFalse(properties.get("field3").isIgnored());
        assertFalse(properties.get("field4").isIgnored());
    }

    @Test
    void testPublicPropertyHiddenWithSchema() {
        @SuppressWarnings("unused")
        class Test {
            private String field1;
            @Schema(hidden = true)
            public String field2;
            protected String field3;
            String field4;
        }

        Map<String, TypeResolver> properties = getProperties(Test.class,
                dynamicConfig(OpenApiConstants.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, false),
                Test.class);

        assertEquals(4, properties.size());
        assertTrue(properties.get("field1").isIgnored());
        assertTrue(properties.get("field2").isIgnored());
        assertTrue(properties.get("field3").isIgnored());
        assertTrue(properties.get("field4").isIgnored());
    }

    @Test
    void testPrivatePropertyVisibleWithPublicGetter() {
        @SuppressWarnings("unused")
        class Test {
            private String field1;
            public String field2;
            protected String field3;
            String field4;

            public String getField1() {
                return field1;
            }

            public String getField3() {
                return field3;
            }

            public String getField4() {
                return field4;
            }
        }

        Map<String, TypeResolver> properties = getProperties(Test.class,
                dynamicConfig(OpenApiConstants.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, false),
                Test.class);

        assertEquals(4, properties.size());
        assertFalse(properties.get("field1").isIgnored());
        assertFalse(properties.get("field2").isIgnored());
        assertFalse(properties.get("field3").isIgnored());
        assertFalse(properties.get("field4").isIgnored());
    }

    @Test
    void testPrivatePropertyHiddenWithPrivateGetter() {
        @SuppressWarnings("unused")
        class Test {
            private String field1;
            private String field2;
            private String field3;
            private String field4;

            public String getField1() {
                return field1;
            }

            private String getField2() {
                return field2;
            }

            protected String getField3() {
                return field3;
            }

            String getField4() {
                return field4;
            }
        }

        Map<String, TypeResolver> properties = getProperties(Test.class,
                dynamicConfig(OpenApiConstants.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, false),
                Test.class);

        assertEquals(4, properties.size());
        assertFalse(properties.get("field1").isIgnored());
        assertTrue(properties.get("field2").isIgnored());
        assertTrue(properties.get("field3").isIgnored());
        assertTrue(properties.get("field4").isIgnored());
    }

    @Test
    void testPrivatePropertyHiddenWithPrivateGetterAndMissingField() {
        @SuppressWarnings("unused")
        class Test {
            private String field1;

            public String getField1() {
                return field1;
            }

            private String getField2() {
                return "field2";
            }

            protected String getField3() {
                return "field3";
            }

            String getField4() {
                return "field4";
            }
        }

        Map<String, TypeResolver> properties = getProperties(Test.class,
                dynamicConfig(OpenApiConstants.SMALLRYE_PRIVATE_PROPERTIES_ENABLE, false),
                Test.class);

        assertEquals(4, properties.size());
        assertFalse(properties.get("field1").isIgnored());
        assertTrue(properties.get("field2").isIgnored());
        assertTrue(properties.get("field3").isIgnored());
        assertTrue(properties.get("field4").isIgnored());
    }

    @Test
    /*
     * Issue: https://github.com/smallrye/smallrye-open-api/issues/746
     */
    void testSingleCharacterPropertyName() {
        @SuppressWarnings("unused")
        class Test {
            private boolean b;

            public boolean isB() {
                return b;
            }

            public void setB(boolean b) {
                this.b = b;
            }
        }

        Map<String, TypeResolver> properties = getProperties(Test.class, emptyConfig(), Test.class);
        assertEquals(1, properties.size());
        assertEquals("b", properties.keySet().iterator().next());
    }

    @ParameterizedTest
    @CsvSource({
            "false, c, b, a",
            "true, a, b, c",
    })
    void testSortedPropertyNames(boolean unsortedEnabled, String first, String second, String third) {
        @SuppressWarnings("unused")
        class Test {
            int c;
            int b;
            int a;
        }

        OpenApiConfig config = dynamicConfig(OpenApiConstants.SMALLRYE_SORTED_PROPERTIES_ENABLE, unsortedEnabled);
        Map<String, TypeResolver> properties = getProperties(Test.class, config, Test.class);
        assertEquals(3, properties.size());
        Iterator<String> keys = properties.keySet().iterator();
        assertEquals(first, keys.next());
        assertEquals(second, keys.next());
        assertEquals(third, keys.next());
    }

    static class TestForClassWithGenericInterfaceClasses {
        static final Class<?>[] NESTED = {
                Fruit.class,
                FruitResource.class,
                FruitResourceImpl.class,
                Apple.class,
                AppleResource.class,
                AppleResourceImpl.class
        };

        abstract static class Fruit implements Serializable {
            private static final long serialVersionUID = 1L;
            Long id;
        }

        interface FruitResource<DTO extends Fruit> {
            @GET
            DTO get();
        }

        abstract static class FruitResourceImpl<DTO extends Fruit> implements FruitResource<DTO> {
            @Override
            public DTO get() {
                return null;
            }
        }

        static class Apple extends Fruit {
            private static final long serialVersionUID = 1L;
            String name;
            String desc;
        }

        @Path("/rest/apple")
        interface AppleResource extends FruitResource<Apple> {
        }

        @Path("/rest/apple")
        @Tag(name = "AppleResource")
        static class AppleResourceImpl extends FruitResourceImpl<Apple> implements AppleResource {
        }
    }

    @Test
    /*
     * Issue: https://github.com/smallrye/smallrye-open-api/issues/1336
     */
    void testForClassWithGenericInterface() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        AnnotationScannerContext context = new AnnotationScannerContext(indexOf(TestForClassWithGenericInterfaceClasses.NESTED),
                loader, emptyConfig());
        ClassInfo leafKlazz = context.getIndex()
                .getClassByName(componentize(TestForClassWithGenericInterfaceClasses.AppleResource.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        TypeResolver resolver = TypeResolver.forClass(context, leafKlazz, leaf);
        Type resolved = resolver.resolve(context.getIndex()
                .getClassByName(componentize(TestForClassWithGenericInterfaceClasses.FruitResource.class.getName()))
                .method("get").returnType());
        assertEquals(componentize(TestForClassWithGenericInterfaceClasses.Apple.class.getName()), resolved.name());
    }
}
