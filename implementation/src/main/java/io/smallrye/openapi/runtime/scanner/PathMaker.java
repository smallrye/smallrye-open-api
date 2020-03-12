package io.smallrye.openapi.runtime.scanner;

/**
 * Help with making a valid path
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PathMaker {

    private PathMaker() {
    }

    /**
     * Make a path out of a number of path segments.
     * 
     * @param segments String paths
     * @return Path built from the segments
     */
    public static String makePath(String... segments) {
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (segment.startsWith("/")) {
                segment = segment.substring(1);
            }
            if (segment.endsWith("/")) {
                segment = segment.substring(0, segment.length() - 1);
            }
            if (segment.isEmpty()) {
                continue;
            }
            builder.append("/");
            builder.append(segment);
        }
        String rval = builder.toString();
        if (rval.isEmpty()) {
            return "/";
        }
        return rval;
    }
}
