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

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.Test;

import test.io.smallrye.openapi.runtime.scanner.entities.IgnoreSchemaOnFieldExample;
import test.io.smallrye.openapi.runtime.scanner.entities.IgnoreTestContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.JsonIgnoreOnFieldExample;
import test.io.smallrye.openapi.runtime.scanner.entities.JsonIgnoreTypeExample;
import test.io.smallrye.openapi.runtime.scanner.entities.JsonbTransientOnFieldExample;
import test.io.smallrye.openapi.runtime.scanner.entities.TransientFieldExample;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class IgnoreTests extends OpenApiDataObjectScannerTestBase {

    // Always ignore nominated properties when given class is used.
    @Test
    public void testIgnore_jsonIgnorePropertiesOnClass() throws IOException, JSONException {
        String name = IgnoreTestContainer.class.getName();
        Type type = getFieldFromKlazz(name, "jipOnClassTest").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, type);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "ignore.jsonIgnorePropertiesOnClass.expected.json", result);
    }

    // Ignore nominated properties of the field in this instance only.
    @Test
    public void testIgnore_jsonIgnorePropertiesOnField() throws IOException, JSONException {
        String name = IgnoreTestContainer.class.getName();
        FieldInfo fieldInfo = getFieldFromKlazz(name, "jipOnFieldTest");
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, fieldInfo, fieldInfo.type());

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, "ignore.jsonIgnorePropertiesOnField.expected.json", result);
    }

    // Entirely ignore a single field once.
    @Test
    public void testIgnore_jsonIgnoreField() throws IOException, JSONException {
        DotName name = DotName.createSimple(JsonIgnoreOnFieldExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.jsonIgnoreField.expected.json", result);
    }

    // Entirely ignore a single field once.
    @Test
    public void testIgnore_jsonIgnoreType() throws IOException, JSONException {
        DotName name = DotName.createSimple(JsonIgnoreTypeExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.jsonIgnoreType.expected.json", result);
    }

    // Entirely ignore a single field once using JSON-B.
    @Test
    public void testIgnore_jsonbTransientField() throws IOException, JSONException {
        DotName name = DotName.createSimple(JsonbTransientOnFieldExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.jsonbTransientField.expected.json", result);
    }

    // Entirely ignore a single field once using hidden attribute of Schema.
    @Test
    public void testIgnore_schemaHiddenField() throws IOException, JSONException {
        DotName name = DotName.createSimple(IgnoreSchemaOnFieldExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.schemaHiddenField.expected.json", result);
    }

    @Test
    public void testIgnore_transientField() throws IOException, JSONException {
        DotName name = DotName.createSimple(TransientFieldExample.class.getName());
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index,
                ClassType.create(name, Type.Kind.CLASS));

        Schema result = scanner.process();

        printToConsole(name.local(), result);
        assertJsonEquals(name.local(), "ignore.transientField.expected.json", result);
    }
}
