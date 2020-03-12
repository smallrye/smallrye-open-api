package test.io.smallrye.openapi.runtime.scanner.entities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class GenericFieldTestContainer<K, V> {
    // Plain field K
    V genericFieldK;

    // Collection of V with wildcard
    ArrayList<? extends V> arrayListOfV;

    // Simple K to V map
    Map<K, V> mapOfKV;

    // Map K to Foo
    LinkedHashMap<K, Foo> mapOfKToFoo;
}
