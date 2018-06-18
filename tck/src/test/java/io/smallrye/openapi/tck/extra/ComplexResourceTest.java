package io.smallrye.openapi.tck.extra;

import static org.hamcrest.Matchers.equalTo;

import org.eclipse.microprofile.openapi.tck.AppTestBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.testng.annotations.Test;

import io.restassured.response.ValidatableResponse;
import test.io.smallrye.openapi.tck.BaseTckTest;
import test.io.smallrye.openapi.tck.TckTest;

/**
 * NOTE: It's not a TCK test, it only leverages the TCK test setup
 *
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 4/19/18
 */
@TckTest
public class ComplexResourceTest extends BaseTckTest<ComplexResourceTest.ComplexResourceTestArquillian> {

    public static class ComplexResourceTestArquillian extends AppTestBase {
        @Deployment(name = "complexTypes")
        public static WebArchive createDeployment() {
            return ShrinkWrap.create(WebArchive.class, "airlines.war")
                    .addPackages(true, new String[]{"io.smallrye.openapi.tck.extra.complex"})
                    .addAsManifestResource("openapi.yaml", "openapi.yaml");
        }

        @RunAsClient
        @Test(dataProvider = "formatProvider")
        @Ignore
        public void testArray(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String arraySchema = "paths.'/complex/array'.post.requestBody.content.'application/json'.schema";
            vr.body(arraySchema + ".type", equalTo("array"));
            vr.body(arraySchema + ".items.format", equalTo("int32"));
            vr.body(arraySchema + ".items.type", equalTo("integer"));
        }

        @RunAsClient
        @Test(dataProvider = "formatProvider")
        public void testList(String type) {
            ValidatableResponse vr = this.callEndpoint(type);
            String arraySchema = "paths.'/complex/list'.post.requestBody.content.'application/json'.schema";
            vr.body(arraySchema + ".type", equalTo("array"));
            vr.body(arraySchema + ".items.format", equalTo("int32"));
            vr.body(arraySchema + ".items.type", equalTo("integer"));
        }
    }
}
