package io.smallrye.openapi.runtime.io.paths;

/**
 * Constants related to Paths
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#pathsObject">pathsObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class PathsConstant {

    public static final String PROP_DELETE = "delete";
    public static final String PROP_PATCH = "patch";
    public static final String PROP_PUT = "put";
    public static final String PROP_GET = "get";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_POST = "post";
    public static final String PROP_PARAMETERS = "parameters";
    public static final String PROP_HEAD = "head";
    public static final String PROP_OPTIONS = "options";
    public static final String PROP_SERVERS = "servers";
    public static final String PROP_METHOD = "method";
    public static final String PROP_SUMMARY = "summary";
    public static final String PROP_TRACE = "trace";

    private PathsConstant() {
    }
}
