package io.smallrye.openapi.runtime.scanner;

/**
 * @author Eric Wittmann
 */
public abstract class IterableStandin<E> implements Iterable<E> {
    E value;
}
