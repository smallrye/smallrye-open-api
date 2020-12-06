/*
 * Copyright 2020 Red Hat, Inc.
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
import test.io.smallrye.openapi.tck.BaseTckTest;
import test.io.smallrye.openapi.tck.TckTest;

/**
 * NOTE: It's not a TCK test, it only leverages the TCK test setup
 */
@TckTest
public class DisabledApplicationPathTest extends BaseTckTest<DisabledApplicationPathTest.ApplicationPathTestArquillian> {

    public static class ApplicationPathTestArquillian extends AppTestBase {
        @Deployment(name = "jaxrs")
        public static WebArchive createDeployment() {
            return ShrinkWrap.create(WebArchive.class, "ApplicationPathTest.war")
                    .addPackages(true, new String[] { "io.smallrye.openapi.tck.extra.apppath" })
                    .addAsManifestResource("disable-app-path.properties", "microprofile-config.properties");
        }

        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testPathItems(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            vr.body("paths.'/restapi/v1/apppaths'.get", org.hamcrest.Matchers.nullValue());
            vr.body("paths.'/apppaths'.get", org.hamcrest.Matchers.notNullValue());
        }

    }
}
