package io.smallrye.openapi.runtime.scanner.spi;

/**
 * Abstract base class for annotation scanners
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractAnnotationScanner implements AnnotationScanner {
    protected String currentAppPath = EMPTY;
    private String contextRoot = EMPTY;

    @Override
    public void setContextRoot(String path) {
        this.contextRoot = path;
    }

    protected String makePath(String operationPath) {
        return createPathFromSegments(this.contextRoot, this.currentAppPath, operationPath);
    }

    /**
     * Make a path out of a number of path segments.
     * 
     * @param segments String paths
     * @return Path built from the segments
     */
    static String createPathFromSegments(String... segments) {
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

    private static final String EMPTY = "";
}
