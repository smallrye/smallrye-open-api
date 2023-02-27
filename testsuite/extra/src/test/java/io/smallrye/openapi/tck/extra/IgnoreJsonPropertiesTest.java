/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.openapi.tck.extra;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.eclipse.microprofile.openapi.tck.AppTestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import io.restassured.response.ValidatableResponse;
import test.io.smallrye.openapi.tck.ExtraSuiteTestBase;

/**
 * NOTE: It's not a TCK test, it only leverages the TCK test setup
 *
 */
public class IgnoreJsonPropertiesTest extends ExtraSuiteTestBase<IgnoreJsonPropertiesTest.ExtensionsTestArquillian> {

    public static class ExtensionsTestArquillian extends AppTestBase {
        @Deployment(name = "jsonignoreproperties")
        public static WebArchive createDeployment() {
            return ShrinkWrap.create(WebArchive.class, "airlines.war")
                    .addPackages(true, new String[] { "io.smallrye.openapi.tck.extra.jsonignoreproperties" })
                    .addAsManifestResource("openapi.yaml", "openapi.yaml");
        }

        /**
         * @see io.smallrye.openapi.tck.extra.jsonignoreproperties.JsonIgnorePropertiesUpstreamBehaviourTest#testDirectAnnotation()
         * @param type
         */
        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testDirectIgnore(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String schemaPath = "components.schemas.DirectIgnore.properties";
            vr.body(schemaPath + ".ignoreMeNested", notNullValue());
            vr.body(schemaPath + ".dontIgnoreMe", notNullValue());
            vr.body(schemaPath + ".ignoreMe", nullValue());
        }

        /**
         * @see JsonIgnorePropertiesUpstreamBehaviourTest#testInheritedAnnotation()
         * @param type
         */
        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testInheritedIgnore(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String schemaPath = "components.schemas.InheritIgnore.properties";
            vr.body(schemaPath + ".ignoreMeNested", notNullValue());
            vr.body(schemaPath + ".dontIgnoreMe", notNullValue());
            vr.body(schemaPath + ".ignoreMe", nullValue());
        }

        /**
         * @see JsonIgnorePropertiesUpstreamBehaviourTest#testInheritedAnnotationThirdLevel()
         * @param type
         */
        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testInheritedIgnoreThirdLevel(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String schemaPath = "components.schemas.ThirdLevelIgnore.properties";
            vr.body(schemaPath + ".ignoreMeNested", notNullValue());
            vr.body(schemaPath + ".dontIgnoreMe", notNullValue());
            vr.body(schemaPath + ".ignoreMe", nullValue());
        }

        /**
         * @see JsonIgnorePropertiesUpstreamBehaviourTest#testInheritedAnnotationOverride()
         * @param type
         */
        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testInheritedIgnoreOverride(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String schemaPath = "components.schemas.InheritIgnoreOverride.properties";
            vr.body(schemaPath + ".ignoreMeNested", notNullValue());
            vr.body(schemaPath + ".dontIgnoreMe", nullValue());
            vr.body(schemaPath + ".ignoreMe", notNullValue());
        }

        /**
         * @see JsonIgnorePropertiesUpstreamBehaviourTest#testInheritedAnnotationNestedOverride()
         * @param type
         */
        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testInheritedIgnoreOverrideNested(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String schemaPath = "components.schemas.NestedOverride.nested.properties";
            vr.body(schemaPath + ".ignoreMeNested", nullValue());
            vr.body(schemaPath + ".dontIgnoreMe", nullValue());
            vr.body(schemaPath + ".ignoreMe", nullValue());
        }

    }
}
