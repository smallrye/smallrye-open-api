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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.eclipse.microprofile.openapi.tck.AppTestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import io.restassured.response.ValidatableResponse;
import test.io.smallrye.openapi.tck.BaseTckTest;
import test.io.smallrye.openapi.tck.TckTest;

/**
 * NOTE: It's not a TCK test, it only leverages the TCK test setup
 * 
 * @author eric.wittmann@gmail.com
 */
@TckTest
public class ExtensionsTest extends BaseTckTest<ExtensionsTest.ExtensionsTestArquillian> {

    public static class ExtensionsTestArquillian extends AppTestBase {
        @Deployment(name = "extensions")
        public static WebArchive createDeployment() {
            return ShrinkWrap.create(WebArchive.class, "airlines.war")
                    .addPackages(true, new String[] { "io.smallrye.openapi.tck.extra.extensions" })
                    .addAsManifestResource("openapi.yaml", "openapi.yaml");
        }

        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testTypeExtensions(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String operation = "paths.'/extensions/typeExtension'.get";
            vr.body(operation + ".x-operation-extension", nullValue());
            vr.body(operation + ".x-type-extension", equalTo("Type extension value."));
        }

        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testOperationExtensions(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String operation = "paths.'/extensions/opExtension'.get";
            vr.body(operation + ".x-operation-extension", equalTo("Operation extension value."));
            vr.body(operation + ".x-type-extension", nullValue());
        }

        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testOperationWrapperExtensions(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String operation = "paths.'/extensions/opWrapperExtension'.get";
            vr.body(operation + ".x-operation-extension-1", equalTo("Operation extension wrapper value (1)."));
            vr.body(operation + ".x-operation-extension-2", equalTo("Operation extension wrapper value (2)."));
            vr.body(operation + ".x-type-extension", nullValue());
        }
    }
}
