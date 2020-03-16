package io.smallrye.openapi.runtime.io.server;

import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.servers.Servers;
import org.jboss.jandex.DotName;

/**
 * Constants related to Server
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#serverObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ServerConstant {
    public static final DotName DOTNAME_SERVER = DotName.createSimple(Server.class.getName());
    public static final DotName DOTNAME_SERVERS = DotName.createSimple(Servers.class.getName());

    public static final String PROP_VARIABLES = "variables";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_URL = "url";

    private ServerConstant() {
    }
}
