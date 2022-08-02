package test.io.smallrye.openapi.runtime.scanner.jakarta;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

public class ParameterDefaultValueInheritance {

    public static final Class<?>[] CLASSES = {
            InterfaceAlpha.class,
            InterfaceBeta.class,
            Resource1.class
    };

    interface InterfaceAlpha {
        @GET
        @Path("/alpha")
        @Produces(MediaType.TEXT_PLAIN)
        String getAlpha(@DefaultValue("1") @QueryParam("omega") int omega);
    }

    @Produces(MediaType.TEXT_XML)
    interface InterfaceBeta extends InterfaceAlpha {
        @Override
        @GET
        @Path("/alpha")
        @Produces(MediaType.TEXT_PLAIN)
        String getAlpha(@DefaultValue("10") @QueryParam("omega") int omega);

        @GET
        @Path("/beta")
        String getBeta(@DefaultValue("true") @QueryParam("upsilon") boolean upsilon);
    }

    @Path("/1")
    static class Resource1 implements InterfaceBeta {
        @Override
        public String getAlpha(int omega) {
            return null;
        }

        @Override
        public String getBeta(@DefaultValue("false") @QueryParam("upsilon") boolean upsilon) {
            return null;
        }
    }
}
