package io.smallrye.openapi.runtime.io.server;

import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.servers.Servers;
import org.jboss.jandex.DotName;

/**
 * Constants related to Server
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverObject">serverObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerConstant {
    public static final String PROP_VARIABLES = "variables";

    static final DotName DOTNAME_SERVER = DotName.createSimple(Server.class.getName());
    static final DotName DOTNAME_SERVERS = DotName.createSimple(Servers.class.getName());
    static final String PROP_SERVER = "server";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_URL = "url";

    private ServerConstant() {
    }
}
