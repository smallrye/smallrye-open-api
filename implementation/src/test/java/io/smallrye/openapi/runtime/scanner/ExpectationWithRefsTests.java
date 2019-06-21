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
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import io.smallrye.openapi.api.models.OpenAPIImpl;
import test.io.smallrye.openapi.runtime.scanner.entities.Bar;
import test.io.smallrye.openapi.runtime.scanner.entities.BuzzLinkedList;
import test.io.smallrye.openapi.runtime.scanner.entities.EnumContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.EnumRequiredContainer;
import test.io.smallrye.openapi.runtime.scanner.entities.GenericTypeTestContainer;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class ExpectationWithRefsTests extends OpenApiDataObjectScannerTestBase {

    OpenAPIImpl oai;
    SchemaRegistry registry;

    @Before
    public void setupRegistry() {
        oai = new OpenAPIImpl();
        registry = SchemaRegistry.newInstance(nestingSupportConfig(), oai, index);
    }

    private void testAssertion(Class<?> target, String expectedResourceName) throws IOException, JSONException {
        DotName name = componentize(target.getName());
        Type type = ClassType.create(name, Type.Kind.CLASS);
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, type);

        Schema result = scanner.process();
        registry.register(type, result);

        printToConsole(oai);
        assertJsonEquals(expectedResourceName, oai);
    }

    private void testAssertion(Class<?> containerClass,
            String targetField,
            String expectedResourceName) throws IOException, JSONException {

        String containerName = containerClass.getName();
        Type parentType = getFieldFromKlazz(containerName, targetField).type();

        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, parentType);

        Schema result = scanner.process();
        registry.register(parentType, result);

        printToConsole(oai);
        assertJsonEquals(expectedResourceName, oai);
    }

    /**
     * Unresolvable type parameter.
     */
    @Test
    public void testUnresolvableWithRefs() throws IOException, JSONException {
        testAssertion(Bar.class, "refsEnabled.unresolvable.expected.json");
    }

    /**
     * Cyclic reference.
     */
    @Test
    public void testCycleWithRef() throws IOException, JSONException {
        testAssertion(BuzzLinkedList.class, "refsEnabled.cycle.expected.json");
    }

    @Test
    public void testBareEnumWithRef() throws IOException, JSONException {
        testAssertion(EnumContainer.class, "refsEnabled.enum.expected.json");
    }

    @Test
    public void testRequiredEnumWithRef() throws IOException, JSONException {
        testAssertion(EnumRequiredContainer.class, "refsEnabled.enumRequired.expected.json");
    }

    @Test
    public void testNestedGenericsWithRefs() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "nesting", "refsEnabled.generic.nested.expected.json");
    }

    @Test
    public void testComplexNestedGenericsWithRefs() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "complexNesting", "refsEnabled.generic.complexNesting.expected.json");
    }

    @Test
    public void testComplexInheritanceGenericsWithRefs() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "complexInheritance",
                "refsEnabled.generic.complexInheritance.expected.json");
    }

    @Test
    public void testGenericsWithBoundsWithRef() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "genericWithBounds", "refsEnabled.generic.withBounds.expected.json");
    }

    @Test
    public void genericFieldWithRefTest() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "genericContainer", "refsEnabled.generic.fields.expected.json");
    }

    @Test
    public void fieldNameOverrideWithRefTest() throws IOException, JSONException {
        testAssertion(GenericTypeTestContainer.class, "overriddenNames",
                "refsEnabled.generic.fields.overriddenNames.expected.json");
    }
}
