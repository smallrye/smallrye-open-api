package io.smallrye.openapi.tck;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import java.net.HttpURLConnection;
import java.net.URL;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestApplication extends Arquillian {
    /**
     * The base URL for the container under test
     */
    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addClass(TestEndpoint.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    @RunAsClient
    public void rest() {
        String uri = baseURL.toExternalForm();
        System.out.println("uri = " + uri);
        WebTarget echoEndpointTarget = ClientBuilder.newClient().target(uri);
        Response response = echoEndpointTarget.request(TEXT_PLAIN).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
    }

    @Test
    @RunAsClient
    public void openApi() {
        String uri = baseURL.toExternalForm() + "openapi";
        System.out.println("uri = " + uri);
        WebTarget echoEndpointTarget = ClientBuilder.newClient().target(uri);
        Response response = echoEndpointTarget.request(TEXT_PLAIN).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
    }

    @RequestScoped
    @Path("/")
    public static class TestEndpoint {
        @Inject
        HelloBean helloBean;

        @GET
        public String hello() {
            return helloBean.hello();
        }
    }

    @ApplicationScoped
    public static class HelloBean {
        public String hello() {
            return "hello";
        }
    }
}
