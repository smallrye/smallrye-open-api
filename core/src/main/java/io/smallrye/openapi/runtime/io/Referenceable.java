package io.smallrye.openapi.runtime.io;

/**
 * Can be referenced
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Referenceable {
    @SuppressWarnings("squid:S00115") // Instruct SonarCloud to ignore this unconventional variable name
    public static final String PROP_$REF = "$ref";

    private Referenceable() {
    }
}
