package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class DiscriminatorMappingTests extends IndexScannerTestBase {

    @Test
    void testJavaxDiscriminatorFullDeclaredInResponse() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator.json",
                test.io.smallrye.openapi.runtime.scanner.javax.DiscriminatorFullDeclaredInResponseTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Lizard.class);
    }

    @Test
    void testJakartaDiscriminatorFullDeclaredInResponse() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.DiscriminatorFullDeclaredInResponseTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Lizard.class);
    }

    @Test
    void testJavaxDiscriminatorNoMappingTestResource() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-no-mapping.json",
                test.io.smallrye.openapi.runtime.scanner.javax.DiscriminatorNoMappingTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Lizard.class);
    }

    @Test
    void testJakartaDiscriminatorNoMappingTestResource() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-no-mapping.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.DiscriminatorNoMappingTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Lizard.class);
    }

    @Test
    void testJavaxDiscriminatorMappingNoSchema() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-no-mapping-schema.json",
                test.io.smallrye.openapi.runtime.scanner.javax.DiscriminatorMappingNoSchemaTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Lizard.class);
    }

    @Test
    void testJakartaDiscriminatorMappingNoSchema() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-no-mapping-schema.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.DiscriminatorMappingNoSchemaTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Lizard.class);
    }

    @Test
    void testJavaxDiscriminatorMappingNoKey() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-no-mapping-key.json",
                test.io.smallrye.openapi.runtime.scanner.javax.DiscriminatorMappingNoKeyTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Lizard.class);
    }

    @Test
    void testJakartaDiscriminatorMappingNoKey() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-no-mapping-key.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.DiscriminatorMappingNoKeyTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Lizard.class);
    }

    @Test
    void testJavaxDiscriminatorMappingEmptyMapping() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-empty-mapping.json",
                test.io.smallrye.openapi.runtime.scanner.javax.DiscriminatorMappingEmptyMappingTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Lizard.class);
    }

    @Test
    void testJakartaDiscriminatorMappingEmptyMapping() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-empty-mapping.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.DiscriminatorMappingEmptyMappingTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Lizard.class);
    }

    @Test
    void testJavaxDiscriminatorMappingNoPropertyName() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-no-property-name.json",
                test.io.smallrye.openapi.runtime.scanner.javax.DiscriminatorMappingNoPropertyNameTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.javax.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.javax.Lizard.class);
    }

    @Test
    void testJakartaDiscriminatorMappingNoPropertyName() throws IOException, JSONException {
        assertJsonEquals("polymorphism.declared-discriminator-no-property-name.json",
                test.io.smallrye.openapi.runtime.scanner.jakarta.DiscriminatorMappingNoPropertyNameTestResource.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.AbstractPet.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Canine.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Cat.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Dog.class,
                test.io.smallrye.openapi.runtime.scanner.jakarta.Lizard.class);
    }
}
