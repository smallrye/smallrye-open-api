/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.openapi.runtime.scanner;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.OpenApiConstants;
import io.smallrye.openapi.api.models.ComponentsImpl;
import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class OpenApiDataObjectScannerTestBase {

    private static final Logger LOG = Logger.getLogger(OpenApiDataObjectScannerTestBase.class);
    protected static Index index;

    @BeforeClass
    public static void createIndex() {
        Indexer indexer = new Indexer();

        // Stand-in stuff
        index(indexer, "io/smallrye/openapi/runtime/scanner/CollectionStandin.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/MapStandin.class");

        // Test samples
        indexDirectory(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/");

        // Microprofile TCK classes
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Airline.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Booking.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/CreditCard.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Flight.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Airline.class");

        // Test containers
        //indexDirectory(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/");

        index = indexer.complete();
    }

    static DotName componentize(String className) {
        DotName prefix = null;
        for (String localName : className.split("\\.")) {
            prefix = DotName.createComponentized(prefix, localName);
        }
        return prefix;
    }

    static void indexDirectory(Indexer indexer, String baseDir) {
        InputStream directoryStream = tcclGetResourceAsStream(baseDir);
        BufferedReader reader = new BufferedReader(new InputStreamReader(directoryStream));
        reader.lines()
                .filter(resName -> resName.endsWith(".class"))
                .map(resName -> Paths.get(baseDir, resName)) // e.g. test/io/smallrye/openapi/runtime/scanner/entities/ + Bar.class
                .forEach(path -> index(indexer, path.toString()));
    }

    private static InputStream tcclGetResourceAsStream(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path);
    }

    static void index(Indexer indexer, String resName) {
        try {
            InputStream stream = tcclGetResourceAsStream(resName);
            indexer.index(stream);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static void printToConsole(String entityName, Schema schema) throws IOException {
        // Remember to set debug level logging.
        LOG.debug(schemaToString(entityName, schema));
        System.out.println(schemaToString(entityName, schema));
    }

    public static void printToConsole(OpenAPI oai) throws IOException {
        // Remember to set debug level logging.
        LOG.debug(OpenApiSerializer.serialize(oai, OpenApiSerializer.Format.JSON));
        System.out.println(OpenApiSerializer.serialize(oai, OpenApiSerializer.Format.JSON));
    }

    public static String schemaToString(String entityName, Schema schema) throws IOException {
        Map<String, Schema> map = new HashMap<>();
        map.put(entityName, schema);
        OpenAPIImpl oai = new OpenAPIImpl();
        ComponentsImpl comp = new ComponentsImpl();
        comp.setSchemas(map);
        oai.setComponents(comp);
        return OpenApiSerializer.serialize(oai, OpenApiSerializer.Format.JSON);
    }

    public static void assertJsonEquals(String entityName, String expectedResource, Schema actual) throws JSONException, IOException {
        URL resourceUrl = OpenApiDataObjectScannerTestBase.class.getResource(expectedResource);
        JSONAssert.assertEquals(loadResource(resourceUrl), schemaToString(entityName, actual),  true);
    }

    public static void assertJsonEquals(String expectedResource, OpenAPI actual) throws JSONException, IOException {
        URL resourceUrl = OpenApiDataObjectScannerTestBase.class.getResource(expectedResource);
        JSONAssert.assertEquals(loadResource(resourceUrl), OpenApiSerializer.serialize(actual, OpenApiSerializer.Format.JSON),  true);
    }

    public static String loadResource(URL testResource) throws IOException {
        return IOUtils.toString(testResource, "UTF-8");
    }

    public FieldInfo getFieldFromKlazz(String containerName, String fieldName) {
        ClassInfo container = index.getClassByName(DotName.createSimple(containerName));
        return container.field(fieldName);
    }

    public static OpenApiConfig emptyConfig() {
        return new OpenApiConfigImpl(new Config() {

            @Override
            public <T> T getValue(String propertyName, Class<T> propertyType) {
                return null;
            }

            @Override
            public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
                return Optional.empty();
            }

            @Override
            public Iterable<String> getPropertyNames() {
                return Collections.emptyList();
            }

            @Override
            public Iterable<ConfigSource> getConfigSources() {
                return Collections.emptyList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static OpenApiConfig nestingSupportConfig() {
        return new OpenApiConfigImpl(new Config() {
            @Override
            public <T> T getValue(String propertyName, Class<T> propertyType) {
                if (OpenApiConstants.SCHEMA_REFERENCES_ENABLE.equals(propertyName)) {
                    return (T) Boolean.TRUE;
                }
                return null;
            }

            @Override
            public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
                if (OpenApiConstants.SCHEMA_REFERENCES_ENABLE.equals(propertyName)) {
                    return (Optional<T>) Optional.of(Boolean.TRUE);
                }
                return Optional.empty();
            }

            @Override
            public Iterable<String> getPropertyNames() {
                return Arrays.asList(OpenApiConstants.SCHEMA_REFERENCES_ENABLE);
            }

            @Override
            public Iterable<ConfigSource> getConfigSources() {
                // Not needed for this test case
                return Collections.emptyList();
            }
        });
    }
}
