/*
 * Copyright 2025 Red Hat, Inc.
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
 */
public class ProcessingRulesTest extends ExtraSuiteTestBase<ProcessingRulesTest.ProcessingRulesTestArquillian> {

    public static class ProcessingRulesTestArquillian extends AppTestBase {
        @Deployment(name = "processing-rules")
        public static WebArchive createDeployment() {
            return ShrinkWrap.create(WebArchive.class, "ProcessingRules.war")
                    .addPackages(true, new String[] { "io.smallrye.openapi.tck.extra.procrules" })
                    .addAsManifestResource("set-model-reader.properties", "microprofile-config.properties");
        }

        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testPathItems(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            vr.body("externalDocs.url", org.hamcrest.Matchers.notNullValue());
            vr.body("externalDocs.url", org.hamcrest.Matchers.is("http://widget-external-docs.org"));
            vr.body("externalDocs.description", org.hamcrest.Matchers.notNullValue());
            vr.body("externalDocs.description", org.hamcrest.Matchers.is("Widget resource external documentation"));
        }

    }
}
