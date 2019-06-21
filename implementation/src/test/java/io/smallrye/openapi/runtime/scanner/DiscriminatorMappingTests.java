/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.json.JSONException;
import org.junit.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class DiscriminatorMappingTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(nestingSupportConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    public void testDiscriminatorFullDeclaredInResponse() throws IOException, JSONException {
        test("polymorphism.declared-discriminator.json",
                DiscriminatorFullDeclaredInResponseTestResource.class,
                AbstractPet.class,
                Cat.class,
                Dog.class,
                Lizard.class);
    }

    @Test
    public void testDiscriminatorNoMappingTestResource() throws IOException, JSONException {
        test("polymorphism.declared-discriminator-no-mapping.json",
                DiscriminatorNoMappingTestResource.class,
                AbstractPet.class,
                Cat.class,
                Dog.class,
                Lizard.class);
    }

    @Test
    public void testDiscriminatorMappingNoSchema() throws IOException, JSONException {
        test("polymorphism.declared-discriminator-no-mapping-schema.json",
                DiscriminatorMappingNoSchemaTestResource.class,
                AbstractPet.class,
                Cat.class,
                Dog.class,
                Lizard.class);
    }

    @Test
    public void testDiscriminatorMappingNoKey() throws IOException, JSONException {
        test("polymorphism.declared-discriminator-no-mapping-key.json",
                DiscriminatorMappingNoKeyTestResource.class,
                AbstractPet.class,
                Cat.class,
                Dog.class,
                Lizard.class);
    }

    @Test
    public void testDiscriminatorMappingEmptyMapping() throws IOException, JSONException {
        test("polymorphism.declared-discriminator-empty-mapping.json",
                DiscriminatorMappingEmptyMappingTestResource.class,
                AbstractPet.class,
                Cat.class,
                Dog.class,
                Lizard.class);
    }

    @Test
    public void testDiscriminatorMappingNoPropertyName() throws IOException, JSONException {
        test("polymorphism.declared-discriminator-no-property-name.json",
                DiscriminatorMappingNoPropertyNameTestResource.class,
                AbstractPet.class,
                Cat.class,
                Dog.class,
                Lizard.class);
    }

    /* Test models and resources below. */

    public static abstract class AbstractPet {
        @Schema(name = "pet_type", required = true)
        private String type;
    }

    public static class Cat extends AbstractPet {
        String name;
    }

    public static class Dog extends AbstractPet {
        String bark;
    }

    public static class Lizard extends AbstractPet {
        boolean lovesRocks;
    }

    @Path("/pets")
    static class DiscriminatorFullDeclaredInResponseTestResource {
        @Path("{id}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(summary = "Returns an AbstractPet with a discriminator declared in the response")
        @APIResponse(content = {
                @Content(schema = @Schema(oneOf = { Cat.class, Dog.class,
                        Lizard.class }, discriminatorProperty = "pet_type", discriminatorMapping = {
                                @DiscriminatorMapping(value = "dog", schema = Dog.class)
                        }))
        })
        @SuppressWarnings("unused")
        public AbstractPet get(@PathParam("id") String id) {
            return null;
        }
    }

    @Path("/pets")
    static class DiscriminatorNoMappingTestResource {
        @Path("{id}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(summary = "Returns an AbstractPet with only a discriminator property declared in the response, "
                + "no Dogs allowed!")
        @APIResponse(content = {
                @Content(schema = @Schema(oneOf = { Cat.class, Lizard.class }, discriminatorProperty = "pet_type"))
        })
        @SuppressWarnings("unused")
        public AbstractPet get(@PathParam("id") String id) {
            return null;
        }
    }

    @Path("/pets")
    static class DiscriminatorMappingNoSchemaTestResource {
        @Path("{id}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(summary = "Returns an AbstractPet with a discriminator declared in the response, "
                + "no mapping due to undeclared mapping schema")
        @APIResponse(content = {
                @Content(schema = @Schema(oneOf = { Cat.class, Dog.class,
                        Lizard.class }, discriminatorProperty = "pet_type", discriminatorMapping = {
                                @DiscriminatorMapping(value = "dog") }))
        })
        @SuppressWarnings("unused")
        public AbstractPet get(@PathParam("id") String id) {
            return null;
        }
    }

    @Path("/pets")
    static class DiscriminatorMappingNoKeyTestResource {
        @Path("{id}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(summary = "Returns an AbstractPet with a discriminator declared in the response, "
                + "mapping with default (implied) key")
        @APIResponse(content = {
                @Content(schema = @Schema(oneOf = { Cat.class, Dog.class,
                        Lizard.class }, discriminatorProperty = "pet_type", discriminatorMapping = {
                                @DiscriminatorMapping(schema = Dog.class) }))
        })
        @SuppressWarnings("unused")
        public AbstractPet get(@PathParam("id") String id) {
            return null;
        }
    }

    @Path("/pets")
    static class DiscriminatorMappingEmptyMappingTestResource {
        @Path("{id}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(summary = "Returns an AbstractPet with a discriminator declared in the response, "
                + "no mapping due to empty @DiscriminatorMapping")
        @APIResponse(content = {
                @Content(schema = @Schema(oneOf = { Cat.class, Dog.class,
                        Lizard.class }, discriminatorProperty = "pet_type", discriminatorMapping = { @DiscriminatorMapping }))
        })
        @SuppressWarnings("unused")
        public AbstractPet get(@PathParam("id") String id) {
            return null;
        }
    }

    @Path("/pets")
    static class DiscriminatorMappingNoPropertyNameTestResource {
        @Path("{id}")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Operation(summary = "Returns an AbstractPet with a discriminator declared in the response, "
                + "no property name (invalid OpenAPI document)")
        @APIResponse(content = {
                @Content(schema = @Schema(oneOf = { Cat.class, Dog.class, Lizard.class }, discriminatorMapping = {
                        @DiscriminatorMapping(value = "dog", schema = Dog.class) }))
        })
        @SuppressWarnings("unused")
        public AbstractPet get(@PathParam("id") String id) {
            return null;
        }
    }
}
