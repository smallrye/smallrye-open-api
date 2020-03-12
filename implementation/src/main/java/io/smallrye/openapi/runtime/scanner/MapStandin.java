package io.smallrye.openapi.runtime.scanner;

import java.util.Map;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public abstract class MapStandin<K, V> implements Map<K, V> {
    K key;
    V value;
}
