package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class StandaloneSchemaScanTest extends IndexScannerTestBase {

    @Test
    public void testUnreferencedSchemasInComponents() throws Exception {
        Index index = indexOf(Cat.class, Dog.class, Class.forName(getClass().getPackage().getName() + ".package-info"));
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.unreferenced.json", result);
    }

    @Test
    public void testInheritanceAnyOf() throws Exception {
        Index index = indexOf(Reptile.class, Lizard.class, Snake.class, Turtle.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.inheritance.json", result);

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

    /****************************************************************/

    /*
     * Test case derived from original example in Smallrye OpenAPI issue #649.
     *
     * https://github.com/smallrye/smallrye-open-api/issues/649
     * https://github.com/quarkusio/quarkus/issues/14670
     */
    @Test
    public void testRegisteredSchemaTypePreserved() throws IOException, JSONException {
        Index index = indexOf(RegisteredSchemaTypePreservedModel.Animal.class,
                RegisteredSchemaTypePreservedModel.AnimalListEnvelope.class,
                RegisteredSchemaTypePreservedModel.MessageBase.class,
                RegisteredSchemaTypePreservedModel.MessageData.class,
                RegisteredSchemaTypePreservedModel.MessageDataItems.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);

        OpenAPI result = scanner.scan();

        printToConsole(result);
        assertJsonEquals("components.schemas.registered-schema-type-preserved.json", result);
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
    public void testJaxbElementUnwrapped() throws IOException, JSONException {
        Index index = indexOf(JAXBElementDto.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("components.schemas.jaxbelement-generic-type-unwrapped.json", result);
    }

    @Schema
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "JAXBElementDto", propOrder = {
            "caseSubtitleFree",
            "caseSubtitle"
    })
    static class JAXBElementDto {
        @XmlElementRef(name = "CaseSubtitle", namespace = "urn:Milo.API.Miljo.DataContracts.V1", type = JAXBElement.class, required = false)
        protected JAXBElement<String> caseSubtitle;
        @XmlElementRef(name = "CaseSubtitleFree", namespace = "urn:Milo.API.Miljo.DataContracts.V1", type = JAXBElement.class, required = false)
        protected JAXBElement<String> caseSubtitleFree;
    }

    /****************************************************************/

    /*
     * https://github.com/smallrye/smallrye-open-api/issues/226
     */
    @Test
    public void testJacksonJsonUnwrapped() throws IOException, JSONException {
        Index index = indexOf(JacksonJsonPerson.class, JacksonJsonPersonWithPrefixedAddress.class,
                JacksonJsonPersonWithSuffixedAddress.class, JacksonJsonAddress.class);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("components.schemas-jackson-jsonunwrapped.json", result);
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
}
