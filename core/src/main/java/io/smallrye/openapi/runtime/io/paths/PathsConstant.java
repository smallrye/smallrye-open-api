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

    static final String PROP_DELETE = "delete";
    static final String PROP_PATCH = "patch";
    static final String PROP_PUT = "put";
    static final String PROP_GET = "get";
    static final String PROP_DESCRIPTION = "description";
    static final String PROP_POST = "post";
    static final String PROP_PARAMETERS = "parameters";
    static final String PROP_HEAD = "head";
    static final String PROP_OPTIONS = "options";
    static final String PROP_SERVERS = "servers";
    static final String PROP_METHOD = "method";
    static final String PROP_SUMMARY = "summary";
    static final String PROP_TRACE = "trace";

    private PathsConstant() {
    }
}
