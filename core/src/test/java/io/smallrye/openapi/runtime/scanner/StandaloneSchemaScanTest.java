package io.smallrye.openapi.runtime.scanner;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.OASConfig;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import io.smallrye.openapi.api.SmallRyeOASConfig;
import test.io.smallrye.openapi.runtime.scanner.dataobject.SingleAnnotatedConstructorArgument;

class StandaloneSchemaScanTest extends IndexScannerTestBase {

    @Test
    void testUnreferencedSchemasInComponents() throws Exception {
        OpenAPI result = scan(Cat.class, Dog.class, Class.forName(getClass().getPackage().getName() + ".package-info"));
        assertJsonEquals("components.schemas.unreferenced.json", result);
    }

    @Test
    void testInheritanceAnyOf() throws Exception {
        OpenAPI result = scan(Reptile.class, Lizard.class, Snake.class, Turtle.class, Alligator.class);
        assertJsonEquals("components.schemas.inheritance.json", result);
    }

    @Test
    void testInheritanceAutomaticAnyOf() throws Exception {
        OpenAPI result = scan(config(SmallRyeOASConfig.AUTO_INHERITANCE, "BOTH"),
                ReptileNoAllOf.class, LizardNoAllOf.class, SnakeNoAllOf.class, TurtleNoAllOf.class, AlligatorNoAllOf.class);
        assertJsonEquals("components.schemas.inheritance.json", result);
    }

    @Test
    void testInheritanceAutomaticAnyOfParentOnly() throws Exception {
        OpenAPI result = scan(config(SmallRyeOASConfig.AUTO_INHERITANCE, "PARENT_ONLY"),
                ReptileNoAllOf.class, LizardNoAllOf.class, SnakeNoAllOf.class, TurtleNoAllOf.class, AlligatorNoAllOf.class);
        assertJsonEquals("components.schemas.inheritance-parent-only.json", result);
    }

    /****************************************************************/

    static class Cat {
        public String name;
        @Schema(minimum = "1", maximum = "20")
        public int age;
        @Schema(nullable = true)
        public String color;
    }

    @Schema(name = "DogType")
    static class Dog {
        public String name;
        public int age;
        @Schema(required = true)
        public int volume;
    }

    @Schema(discriminatorProperty = "type", discriminatorMapping = {
            @DiscriminatorMapping(value = "lizard", schema = Lizard.class),
            @DiscriminatorMapping(value = "snake", schema = Snake.class),
            @DiscriminatorMapping(value = "turtle", schema = Turtle.class)
    })
    static abstract class Reptile {
        @Schema(required = true)
        private String type;
    }

    @Schema(allOf = { Reptile.class, Lizard.class })
    static class Lizard extends Reptile {
        String color;
    }

    @Schema(allOf = { Reptile.class, Snake.class })
    static class Snake extends Reptile {
        int length;
        String lengthUnits;
    }

    @Schema(allOf = { Reptile.class, Turtle.class })
    static class Turtle extends Reptile {
        String shellPattern;
    }

    @Schema(description = "An alligator is a reptile without allOf inheritance")
    static class Alligator extends Reptile {
        float jawLength;
    }

    @Schema(name = "Reptile", discriminatorProperty = "type", discriminatorMapping = {
            @DiscriminatorMapping(value = "lizard", schema = LizardNoAllOf.class),
            @DiscriminatorMapping(value = "snake", schema = SnakeNoAllOf.class),
            @DiscriminatorMapping(value = "turtle", schema = TurtleNoAllOf.class)
    })
    static abstract class ReptileNoAllOf {
        @Schema(required = true)
        private String type;
    }

    @Schema(name = "Lizard")
    static class LizardNoAllOf extends ReptileNoAllOf {
        String color;
    }

    @Schema(name = "Snake")
    static class SnakeNoAllOf extends ReptileNoAllOf {
        int length;
        String lengthUnits;
    }

    @Schema(name = "Turtle")
    static class TurtleNoAllOf extends ReptileNoAllOf {
        String shellPattern;
    }

    @Schema(name = "Alligator", allOf = void.class, description = "An alligator is a reptile without allOf inheritance")
    static class AlligatorNoAllOf extends ReptileNoAllOf {
        float jawLength;
    }

    /****************************************************************/

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #649.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/649
     * https://github.com/quarkusio/quarkus/issues/14670
     */
    @Test
    void testRegisteredSchemaTypePreserved() throws IOException, JSONException {
        assertJsonEquals("components.schemas.registered-schema-type-preserved.json",
                RegisteredSchemaTypePreservedModel.Animal.class,
                RegisteredSchemaTypePreservedModel.AnimalListEnvelope.class,
                RegisteredSchemaTypePreservedModel.MessageBase.class,
                RegisteredSchemaTypePreservedModel.MessageData.class,
                RegisteredSchemaTypePreservedModel.MessageDataItems.class);
    }

    static class RegisteredSchemaTypePreservedModel {
        @Schema
        static class AnimalListEnvelope extends MessageData<MessageDataItems<Animal>> {
            public AnimalListEnvelope() {
            }

            public AnimalListEnvelope(List<Animal> animals) {
                super(new MessageDataItems<Animal>(animals));
            }
        }

        static class Animal {
            private String name;
            private int age;

            public Animal() {
            }

            public Animal(String name, int age) {
                this.name = name;
                this.age = age;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getAge() {
                return age;
            }

            public void setAge(int age) {
                this.age = age;
            }
        }

        @Schema
        static class MessageData<T> extends MessageBase {
            @Schema(description = "The business data object")
            private T data;

            public MessageData() {
            }

            public MessageData(T data) {
                this.data = data;
            }

            public T getData() {
                return data;
            }

            public void setData(T data) {
                this.data = data;
            }

            @Schema(description = "The class-name of the business data object")
            public String getKind() {
                if (data == null) {
                    return null;
                } else {
                    return data.getClass()
                            .getSimpleName();
                }
            }
        }

        @Schema
        static class MessageBase {

            @Schema(description = "The API version", example = "v3")
            protected String apiVersion = "v3";

            @Schema(description = "Unique request-id (used for logging)", example = "F176f717c7a71")
            protected String requestId;

            @Schema(description = "Optional context-value for request/response correlation")
            protected String context;

            protected MessageBase() {
            }

            public String getRequestId() {
                return requestId;
            }

            public void setRequestId(String id) {
                this.requestId = id;
            }

            public String getContext() {
                return context;
            }

            public void setContext(String context) {
                this.context = context;
            }

            public String getApiVersion() {
                return apiVersion;
            }

            public void setApiVersion(String apiVersion) {
                this.apiVersion = apiVersion;
            }
        }

        @Schema
        static class MessageDataItems<T> {
            private List<T> items;

            public MessageDataItems() {
            }

            public MessageDataItems(List<T> items) {
                this.items = new ArrayList<T>(items);
            }

            public List<T> getItems() {
                return Collections.unmodifiableList(items);
            }

            public void setItems(List<T> items) {
                this.items = new ArrayList<T>(items);
            }

            @Schema(example = "1")
            public int getCurrentItemCount() {
                return (items == null) ? 0 : items.size();
            }
        }
    }

    /****************************************************************/

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #650.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/650
     */
    @Test
    void testJavaxJaxbElementUnwrapped() throws IOException, JSONException {
        assertJsonEquals("components.schemas.jaxbelement-generic-type-unwrapped.json",
                test.io.smallrye.openapi.runtime.scanner.javax.JAXBElementDto.class);
    }

    @Test
    void testJakartaJaxbElementUnwrapped() throws IOException, JSONException {
        assertJsonEquals("components.schemas.jaxbelement-generic-type-unwrapped.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.JAXBElementDto.class);
    }

    /****************************************************************/

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/226
     */
    @Test
    void testJacksonJsonUnwrapped() throws IOException, JSONException {
        assertJsonEquals("components.schemas-jackson-jsonunwrapped.json",
                JacksonJsonPerson.class, JacksonJsonPersonWithPrefixedAddress.class,
                JacksonJsonPersonWithSuffixedAddress.class, JacksonJsonAddress.class);
    }

    @Schema
    static class JacksonJsonPerson {
        protected String name;
        @JsonUnwrapped
        protected JacksonJsonAddress address;

        @Schema(description = "Ignored since address is unwrapped")
        public JacksonJsonAddress getAddress() {
            return address;
        }
    }

    @Schema
    static class JacksonJsonPersonWithPrefixedAddress {
        protected String name;
        @JsonUnwrapped(prefix = "addr-")
        protected JacksonJsonAddress address;
    }

    @Schema
    static class JacksonJsonPersonWithSuffixedAddress {
        protected String name;
        @JsonUnwrapped(suffix = "-addr")
        protected JacksonJsonAddress address;
    }

    @Schema
    static class JacksonJsonAddress {
        protected int streetNumber;
        protected String streetName;
        protected String city;
        protected String state;
        protected String postalCode;
    }

    /****************************************************************/

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/688
     */
    @Test
    void testNestedCollectionSchemas() throws IOException, JSONException {
        // Place the JDK classes in the index to simulate Quarkus
        assertJsonEquals("components.schemas.nested-parameterized-collection-types.json", CollectionBean.class,
                EntryBean.class,
                MultivaluedCollection.class,
                MultivaluedMap.class,
                // CustomMap.class excluded intentionally
                Collection.class,
                ArrayList.class,
                HashMap.class,
                List.class,
                Map.class,
                Set.class,
                UUID.class);
    }

    @Schema
    static class CollectionBean {
        @Schema(description = "In-line schema, `additionalProperties` array `items` reference `EntryBean`")
        CustomMap<String, List<EntryBean>> a_customMapOfLists;

        @Schema(description = "Reference to `MultivaluedMapStringEntryBean")
        MultivaluedMap<String, EntryBean> b_multivaluedEntryMap;

        @Schema(description = "In-line schema, `additionalProperties` array `items` reference `EntryBean`")
        Map<String, List<EntryBean>> c_mapStringListEntryBean;

        @Schema(description = "In-line schema (All JDK types, no references)")
        Collection<Map<String, List<String>>> d_collectionOfMapsOfListsOfStrings;

        @Schema(description = "In-line schema")
        Map<UUID, Map<String, Set<UUID>>> e_mapOfMapsOfSetsOfUUIDs;

        @Schema(description = "Reference to `MultivaluedCollectionString`")
        MultivaluedCollection<String> f_listOfStringLists;
    }

    static class EntryBean {
        String name;
        String value;
    }

    /*
     * Not present in index - will cause call to Class.forName(...) and is not
     * eligible for a entry in #/components/schemas
     */
    static class CustomMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 1L;

        static {
            // We shouldn't run any code while scanning
            if (true) {
                throw new RuntimeException("CustomMap was initialized!?");
            }
        }
    }

    static class MultivaluedCollection<T> extends ArrayList<List<T>> {
        private static final long serialVersionUID = 1L;
    }

    static class MultivaluedMap<K, V> extends HashMap<K, List<V>> {
        private static final long serialVersionUID = 1L;
    }

    /****************************************************************/

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/715
     */
    @Test
    void testNestedCustomGenericSchemas() throws IOException, JSONException {
        assertJsonEquals("components.schemas.nested-custom-generics.json", Foo.class, Generic0.class, Generic1.class,
                Generic2.class, CustomMap.class);
    }

    /*
     * Do not annotate with @Schema - test relies on Generic0 registration
     * only as an array component of Generic2#arrayOfGeneric0.
     */
    static class Generic0<T> {
        T value;
    }

    static class Generic1<T> {
        T value;
    }

    static class Generic2<T> {
        Generic1<T> nested;
        CustomMap<T, T> nestedMap;
        // Do not reference Generic0 other than from this field!
        Generic0<T>[] arrayOfGeneric0;
    }

    @Schema
    static class Foo {
        Generic2<String> generic;
    }

    /****************************************************************/

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/809
     */
    @Test
    @SuppressWarnings("unused")
    void testOptionalArrayTypes() throws IOException, JSONException {
        @Schema(name = "B")
        class B {
            public UUID id;
        }
        @Schema(name = "A")
        class A {
            public UUID id;
            public Optional<B> optionalOfB;
            public List<B> listOfB;
            public Optional<List<B>> optionalListOfB;
            public Optional<B[]> optionalArrayOfB;
            public Optional<B>[] arrayOfOptionalB;
            public List<Optional<B>> listOfOptionalB;
        }

        assertJsonEquals("components.schemas.optional-arraytype.json", B.class, A.class, UUID.class, List.class,
                Optional.class);
    }

    @Target(ElementType.TYPE_USE)
    @interface TestAnno {

    }

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/831
     */
    @Test
    void testArraySchemaTypeOverridden() throws IOException, JSONException {
        @Schema(name = "Sample")
        class Sample {
            @Schema(type = SchemaType.STRING, format = "base64")
            public byte[] data;
            @Schema(type = SchemaType.STRING)
            public char[] chars;
            @Schema(type = SchemaType.ARRAY)
            public char[] arrayFromSchema;
            @Schema
            public char[] arrayFromType;
        }

        assertJsonEquals("components.schemas.array-type-override.json", Sample.class);
    }

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/937
     */
    @Test
    void testSingleAnnotatedConstructorArgumentIgnored() throws IOException, JSONException {
        assertJsonEquals("components.schemas.annotated-constructor-arg-ignored.json", SingleAnnotatedConstructorArgument.class);
    }

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/944
     */
    @Test
    void testParameterizedTypeWithNonparameterizedAncestryChainLink() throws IOException, JSONException {
        class Tuple implements Iterable<Object> {
            @Override
            public Iterator<Object> iterator() {
                return null;
            }
        }

        class Pair<T1, T2> extends Tuple {
        }

        @Schema(name = "TestBean")
        class Bean {
            @SuppressWarnings("unused")
            Pair<String, String> pair;
        }

        assertJsonEquals("components.schemas.nonparameterized-ancestry-chain-link.json", Bean.class, Pair.class, Tuple.class);
    }

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/1049
     */
    @Test
    @SuppressWarnings("unused")
    void testSchemaDeprecation() throws IOException, JSONException {
        @Schema(name = "Bean1")
        @Deprecated
        class Bean1 {
            String prop1;
            String prop2;
        }

        @Schema(name = "Bean2")
        class Bean2 {
            @Deprecated
            String prop1;
            String prop2;
            Bean1 prop3;
        }

        @Schema(name = "Bean3")
        class Bean3 {
            String prop1;
            String prop2;
            Bean2 prop3;
        }

        OpenAPI result = scan(Bean1.class, Bean2.class, Bean3.class);

        assertTrue(result.getComponents().getSchemas().get("Bean1").getDeprecated());
        assertNull(result.getComponents().getSchemas().get("Bean1").getProperties().get("prop1").getDeprecated());
        assertNull(result.getComponents().getSchemas().get("Bean1").getProperties().get("prop2").getDeprecated());

        assertNull(result.getComponents().getSchemas().get("Bean2").getDeprecated());
        assertTrue(result.getComponents().getSchemas().get("Bean2").getProperties().get("prop1").getDeprecated());
        assertNull(result.getComponents().getSchemas().get("Bean2").getProperties().get("prop2").getDeprecated());
        assertNull(result.getComponents().getSchemas().get("Bean2").getProperties().get("prop3").getDeprecated());

        assertNull(result.getComponents().getSchemas().get("Bean3").getDeprecated());
        assertNull(result.getComponents().getSchemas().get("Bean3").getProperties().get("prop1").getDeprecated());
        assertNull(result.getComponents().getSchemas().get("Bean3").getProperties().get("prop2").getDeprecated());
        assertNull(result.getComponents().getSchemas().get("Bean3").getProperties().get("prop3").getDeprecated());
    }

    @Test
    void testFieldSchemaOverridesTypeAssertion() throws IOException, JSONException {
        @Schema(name = "OtherBean", description = "The first bean")
        class OtherBean {
            @Schema(maxLength = 5)
            String prop1;

            @SuppressWarnings("unused")
            Object prop2;
        }

        @Schema(name = "Bean")
        class Bean {
            @Schema(title = "In-lined schema with overridden attributes", description = "Not 'The first bean'", properties = {
                    @SchemaProperty(name = "prop1", maxLength = 4),
                    @SchemaProperty(name = "prop2", type = SchemaType.INTEGER)
            })
            OtherBean first;

            @Schema(title = "Property with `allOf` referring to `OtherBean`")
            OtherBean second;

            // Direct ref only
            @Schema
            OtherBean third;
        }

        assertJsonEquals("components.schemas.field-overrides-type.json", OtherBean.class,
                /* BeanTwo.class, BeanThree.class, */ Bean.class);
    }

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/1359
     */
    @Test
    @SuppressWarnings("unused")
    void testStreamTypes() throws IOException, JSONException {
        @SuppressWarnings("serial")
        @Schema(name = "StringArray")
        class StringArray extends ArrayList<String> {

        }

        @Schema(name = "TestBean")
        class Bean {
            Stream<String> stringArrayFromStream;
            IntStream intArrayFromStream;
            Properties anyValueMapFromProperties;
            StringArray stringArrayFromIterator;
            LongStream longArrayFromStream;
            DoubleStream doubleArrayFromStream;
            @SuppressWarnings("rawtypes")
            Collection anyArrayFromRawCollection;
        }

        assertJsonEquals("components.schemas.iterator-stream-map-types.json", Bean.class, StringArray.class);
    }

    /**
     * Check that an array item that is considered "terminal" because it is a known type
     * is registered in the schema registry and `#/components/schemas`.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/1573
     */
    @Test
    void testZonedDateTimeArrayWrapper() throws IOException, JSONException {
        @Schema(name = "ZonedDateTimeArrayWrapper")
        class ZonedDateTimeArrayWrapper {
            @SuppressWarnings("unused")
            ZonedDateTime[] now;
        }

        assertJsonEquals("components.schemas.terminal-array-item-registration.json", ZonedDateTimeArrayWrapper.class,
                ZonedDateTime.class);
    }

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/1565
     *
     * Verify that registered schemas are not set to a self reference.
     * Previously, a schema of an object property may have been set
     * in components as a ref to itself when the property schema was
     * discovered as one of the parent object's fields. Here, Class2
     * would have been a self-ref if Class1 were scanned first. If Class2
     * were scanned first, the issue would not occur.
     */
    @Test
    void testNoSelfRefToSchemaOfAnnotatedObjectProperty() throws IOException, JSONException {
        @Schema(type = SchemaType.STRING, name = "MyValueClass")
        class Class2 {
        }

        @Schema(description = "some description", name = "MyClass")
        class Class1 {
            @SuppressWarnings("unused")
            Class2 value;
        }

        assertJsonEquals("components.schemas.no-self-ref-for-property-schema.json", Class1.class, Class2.class);
    }

    @Test
    @SuppressWarnings("unused")
    void testParameterizedTypeSchemaConfig() throws IOException, JSONException {
        class Nullable<T> {
            T value;
            boolean isPresent;

            boolean isPresent() {
                return isPresent;
            }
        }

        @Schema(name = "Bean")
        class Bean {
            Nullable<String[]> nullableString;
        }

        String nullableStringArySig = Nullable.class.getName() + "<java.lang.String[]>";
        OpenAPI result = scan(config(
                OASConfig.SCHEMA_PREFIX + nullableStringArySig,
                "{ \"name\": \"NullableStringArray\", \"type\": \"array\", \"items\": { \"type\": \"string\" }, \"nullable\": true }"),
                Nullable.class, Bean.class);
        assertJsonEquals("components.schemas.parameterized-type-schema-config.json", result);
    }

    @Test
    void testJacksonPropertyAccess() throws IOException, JSONException {
        @Schema(name = "Bean")
        class Bean {
            @JsonProperty
            String dflt;

            @JsonProperty(access = Access.READ_ONLY)
            String ro;

            @JsonProperty(access = Access.WRITE_ONLY)
            String wo;

            @JsonProperty(access = Access.READ_WRITE)
            String rw;

            @JsonProperty(access = Access.AUTO)
            @JsonIgnore
            String ignored;

            @SuppressWarnings("unused")
            public void setRo(String ro) {
                this.ro = ro;
            }

            @SuppressWarnings("unused")
            public String getWo() {
                return wo;
            }
        }

        assertJsonEquals("components.schemas.jackson-property-access.json", Bean.class);
    }
}
