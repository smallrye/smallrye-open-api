package io.smallrye.openapi.runtime.scanner;

import java.util.Collection;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public abstract class CollectionStandin<E> implements Collection<E> {
    E value;
}
