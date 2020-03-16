package io.smallrye.openapi.runtime.io;

/**
 * A simple registry to hold the current content types
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CurrentContentTypes {
    private static final ThreadLocal<CurrentContentTypes> current = new ThreadLocal<>();

    public static void register(final String[] currentConsumes, final String[] currentProduces) {
        CurrentContentTypes registry = new CurrentContentTypes(currentConsumes, currentProduces);
        current.set(registry);
    }

    public static String[] getCurrentConsumes() {
        return current.get().currentConsumes;
    }

    public static String[] getCurrentProduces() {
        return current.get().currentProduces;
    }

    public static void remove() {
        current.remove();
    }

    private final String[] currentConsumes;
    private final String[] currentProduces;

    private CurrentContentTypes(final String[] currentConsumes, final String[] currentProduces) {
        this.currentConsumes = currentConsumes;
        this.currentProduces = currentProduces;
    }

}