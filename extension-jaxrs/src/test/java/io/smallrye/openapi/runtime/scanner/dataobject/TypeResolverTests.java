package io.smallrye.openapi.runtime.scanner.dataobject;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.junit.Test;

import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.IndexScannerTestBase;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class TypeResolverTests extends IndexScannerTestBase {

    @Test
    public void testAnnotatedMethodOverridesParentSchema() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(AbstractAnimal.class,
                Feline.class,
                Cat.class));

        ClassInfo leafKlazz = index.getClassByName(componentize(Cat.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
        TypeResolver resolver = properties.get("type");
        assertEquals(Kind.METHOD, resolver.getAnnotationTarget().kind());
        AnnotationInstance schema = TypeUtil.getSchemaAnnotation(resolver.getAnnotationTarget());
        assertEquals("type", schema.value("name").asString());
        assertEquals(false, schema.value("required").asBoolean());
        assertEquals("Cat", schema.value("example").asString());
        assertArrayEquals(new String[] { "age", "type", "name", "extinct" },
                properties.values().stream().map(TypeResolver::getPropertyName).toArray());
    }

    @Test
    public void testAnnotatedFieldsOverridesInterfaceSchema() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(AbstractAnimal.class,
                Feline.class,
                Cat.class));

        ClassInfo leafKlazz = index.getClassByName(componentize(Cat.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
        TypeResolver resolver = properties.get("name");
        assertEquals(Kind.FIELD, resolver.getAnnotationTarget().kind());
        AnnotationInstance schema = TypeUtil.getSchemaAnnotation(resolver.getAnnotationTarget());
        assertEquals(true, schema.value("required").asBoolean());
        assertEquals("Felix", schema.value("example").asString());
    }

    @Test
    public void testAnnotatedInterfaceMethodOverridesImplMethod() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(AbstractAnimal.class,
                Canine.class,
                Dog.class));

        ClassInfo leafKlazz = index.getClassByName(componentize(Dog.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
        assertEquals(5, properties.size());
        TypeResolver resolver = properties.get("name");
        assertEquals(Kind.METHOD, resolver.getAnnotationTarget().kind());
        AnnotationInstance schema = TypeUtil.getSchemaAnnotation(resolver.getAnnotationTarget());
        assertEquals("c_name", schema.value("name").asString());
        assertEquals(50, schema.value("maxLength").asInt());
        assertEquals("The name of the canine", schema.value("description").asString());
        assertArrayEquals(new String[] { "age", "type", "c_name", "bark", "extinct" },
                properties.values().stream().map(TypeResolver::getPropertyName).toArray());
    }

    @Test
    public void testAnnotatedInterfaceMethodOverridesStaticField() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(AbstractAnimal.class,
                Reptile.class,
                Lizard.class));

        ClassInfo leafKlazz = index.getClassByName(componentize(Lizard.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);

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
    public void testBareInterface() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(MySchema.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(MySchema.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
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
    public void testJacksonPropertyOrderDefault() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(JacksonPropertyOrderDefault.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(JacksonPropertyOrderDefault.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
        assertEquals(4, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        assertEquals("comment", iter.next().getValue().getPropertyName());
        assertEquals("theName", iter.next().getValue().getPropertyName());
    }

    @Test
    public void testJacksonPropertyOrderCustomName() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(JacksonPropertyOrderCustomName.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(JacksonPropertyOrderCustomName.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
        assertEquals(4, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        assertEquals("theName", iter.next().getValue().getPropertyName());
        assertEquals("comment2ActuallyFirst", iter.next().getValue().getPropertyName());
        assertEquals("comment", iter.next().getValue().getPropertyName());
    }

    @Test
    public void testJaxbCustomPropertyOrder() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(JaxbCustomPropertyOrder.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(JaxbCustomPropertyOrder.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
        assertEquals(4, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        assertEquals("theName", iter.next().getValue().getPropertyName());
        assertEquals("comment2ActuallyFirst", iter.next().getValue().getPropertyName());
        assertEquals("comment", iter.next().getValue().getPropertyName());
        assertEquals("name2", iter.next().getValue().getPropertyName());
    }

    @Test
    public void testNonJavaBeansPropertyAccessor() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(NonJavaBeanAccessorProperty.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(NonJavaBeanAccessorProperty.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
        assertEquals(1, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        TypeResolver property = iter.next().getValue();
        assertEquals("name", property.getPropertyName());
        assertEquals(property.getReadMethod(), property.getAnnotationTarget());
        assertEquals("Name of the property", TypeUtil.getAnnotationValue(property.getAnnotationTarget(),
                SchemaConstant.DOTNAME_SCHEMA,
                SchemaConstant.PROP_TITLE));
    }

    @Test
    public void testNonJavaBeansPropertyMutator() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(NonJavaBeanMutatorProperty.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(NonJavaBeanMutatorProperty.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
        assertEquals(1, properties.size());
        Iterator<Entry<String, TypeResolver>> iter = properties.entrySet().iterator();
        TypeResolver property = iter.next().getValue();
        assertEquals("name", property.getPropertyName());
        assertEquals(property.getWriteMethod(), property.getAnnotationTarget());
        assertEquals("Name of the property", TypeUtil.getAnnotationValue(property.getAnnotationTarget(),
                SchemaConstant.DOTNAME_SCHEMA,
                SchemaConstant.PROP_TITLE));
    }

    @Test
    public void testOneSidedPropertiesHidden() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(OneSidedProperties.class, OneSidedParent.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(OneSidedProperties.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
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
    public void testXmlAccessTransientField() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(XmlTransientField.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(XmlTransientField.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
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
    public void testXmlAccessTransientClass() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(XmlTransientClass.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(XmlTransientClass.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
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
    public void testXmlAccessPublicMember() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(XmlAccessTypePublicMember.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(XmlAccessTypePublicMember.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
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
    public void testXmlAccessTypeFieldOnly() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(XmlAccessTypeFieldOnly.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(XmlAccessTypeFieldOnly.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
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
    public void testXmlAccessTypePropertyOnly() {
        AugmentedIndexView index = new AugmentedIndexView(indexOf(XmlAccessTypePropertyOnly.class));
        ClassInfo leafKlazz = index.getClassByName(componentize(XmlAccessTypePropertyOnly.class.getName()));
        Type leaf = Type.create(leafKlazz.name(), Type.Kind.CLASS);
        Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, new IgnoreResolver(index), leaf, leafKlazz,
                null);
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

    /* Test models and resources below. */

    @com.fasterxml.jackson.annotation.JsonPropertyOrder({ "age", "type" })
    public static abstract class AbstractAnimal {
        @Schema
        private String type;

        protected Integer age;
        private boolean extinct;

        @Schema(name = "pet_type", required = true)
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Schema
        public Boolean isExtinct() {
            return extinct;
        }

        public void setExtinct(boolean extinct) {
            this.extinct = extinct;
        }
    }

    public static interface Feline {
        @Schema(name = "name", required = false, example = "Feline")
        void setName(String name);
    }

    // "type" will be first due to ordering on AbstractAnimal
    @javax.xml.bind.annotation.XmlType(propOrder = { "name", "type" })
    public static class Cat extends AbstractAnimal implements Feline {
        @Schema(required = true, example = "Felix")
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        @Schema(name = "type", required = false, example = "Cat")
        public String getType() {
            return super.getType();
        }
    }

    public static interface Canine {
        @Schema(name = "c_name", description = "The name of the canine", maxLength = 50)
        public String getName();
    }

    // "type" will be first due to ordering on AbstractAnimal
    @javax.json.bind.annotation.JsonbPropertyOrder({ "name", "type", "bark" })
    public static class Dog extends AbstractAnimal implements Canine {
        @JsonbProperty("bark")
        String bark;

        @Schema(name = "bark")
        public String getBark() {
            return bark;
        }

        @Override
        public String getName() {
            return "Fido";
        }

        @Schema(description = "This property is not used due to being static")
        public static int getStaticAge() {
            return -1;
        }
    }

    public static interface Reptile {
        @Schema(name = "scaleColor", description = "The color of a reptile's scales")
        public String getScaleColor();

        @Schema(name = "scaleColor", description = "This is how the color is set, but the description comes from getScaleColor")
        public void setScaleColor(String color);
    }

    public static class Lizard extends AbstractAnimal implements Reptile {
        @Schema(deprecated = true)
        static String scaleColor;
        boolean lovesRocks;

        @Override
        public String getScaleColor() {
            return "green";
        }

        public void setScaleColor(String scaleColor) {
            // Bad idea, but doing it anyway ;-)
            Lizard.scaleColor = scaleColor;
        }

        public void setAge(String age) {
            super.setAge(Integer.parseInt(age));
        }
    }

    // Out of order on purpose
    @JsonbPropertyOrder({ "field1", "field3", "field2" })
    public interface MySchema {
        @Schema(required = true)
        String getField1();

        @Schema(name = "anotherField")
        String getField2();

        String getField3();
    }

    @com.fasterxml.jackson.annotation.JsonPropertyOrder({ "comment", "name" })
    public static class JacksonPropertyOrderDefault {

        @com.fasterxml.jackson.annotation.JsonProperty("theName")
        String name;
        String name2;
        String comment;
        String comment2;

        public String getComment() {
            return comment;
        }

        public String getName() {
            return name;
        }
    }

    @com.fasterxml.jackson.annotation.JsonPropertyOrder({ "theName", "comment2ActuallyFirst", "comment" })
    public static class JacksonPropertyOrderCustomName {

        @com.fasterxml.jackson.annotation.JsonProperty("theName")
        String name;
        String name2;
        String comment;
        @com.fasterxml.jackson.annotation.JsonProperty("comment2ActuallyFirst")
        String comment2;

        public String getComment() {
            return comment;
        }

        public String getName() {
            return name;
        }
    }

    @XmlType(propOrder = { "theName", "comment2ActuallyFirst", "comment", "name2" })
    public static class JaxbCustomPropertyOrder {

        @XmlElement(name = "theName")
        String name;
        @XmlAttribute
        String name2;
        @XmlElement
        String comment;
        @XmlAttribute(name = "comment2ActuallyFirst")
        String comment2;

        public String getComment() {
            return comment;
        }

        public String getName() {
            return name;
        }

        public String getName2() {
            return name2;
        }

        public String getComment2() {
            return comment2;
        }
    }

    static class NonJavaBeanAccessorProperty {
        String name;

        @Schema(title = "Name of the property")
        String name() {
            return name;
        }

        // Should be skipped
        String anotherValue() {
            return null;
        }

        // Should be skipped
        String get() {
            return name;
        }

        // Should be skipped
        String isNotAnAccessor() {
            return null;
        }

        void name(String name) {
            this.name = name;
        }
    }

    static class NonJavaBeanMutatorProperty {
        String name;

        String name() {
            return name;
        }

        // Should be skipped
        void anotherValue(String value) {
            return;
        }

        // Should be skipped
        String get() {
            return name;
        }

        // Should be skipped
        String isNotAnAccessor() {
            return null;
        }

        @Schema(title = "Name of the property")
        void name(String name) {
            this.name = name;
        }
    }

    static class OneSidedParent {

        @Schema(hidden = true)
        public String getParentProp1() {
            return "";
        }

        @Schema(hidden = true)
        public void setParentProp2(String something) {

        }
    }

    static class OneSidedProperties extends OneSidedParent {

        String prop1;
        String prop2;
        String prop3;

        @Schema(hidden = true) // write only
        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }

        public String getProp2() {
            return prop2;
        }

        @Schema(hidden = true) // read only
        public void setProp2(String prop2) {
            this.prop2 = prop2;
        }
    }

    ///////////////////////////

    static class XmlTransientField {
        @XmlTransient
        String prop1Field;
        String prop2Field;
    }

    @XmlTransient
    static class XmlTransientClass {
        String prop1Field;
        String prop2Field;

        public String getProp3Property() {
            return null;
        }
    }

    @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
    static class XmlAccessTypePublicMember {
        public String prop1Field;
        @SuppressWarnings("unused")
        private String prop2Field;

        public String getProp3Property() {
            return null;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class XmlAccessTypeFieldOnly {
        String prop1Field;

        public String getProp2Property() {
            return null;
        }
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    static class XmlAccessTypePropertyOnly {
        String prop2Field;

        public String getProp1Property() {
            return null;
        }
    }
}
