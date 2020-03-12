package test.io.smallrye.openapi.runtime.scanner.entities;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class GenericTypeTestContainer {
    // Nesting generics.
    KustomPair<KustomPair<String, String>, Integer> nesting;

    // More complex nesting of generics including unbounded wildcard
    Fuzz<KustomPair<Fuzz<String, Date>, ?>, Double> complexNesting;

    // Complex inheritance requiring manual resolution of type variables in superclasses
    Foo complexInheritance;

    // Generics with bounds
    KustomPair<? extends Integer, ? super Integer> genericWithBounds;

    // Type containing a variety of collections and maps.
    GenericFieldTestContainer<String, LocalDateTime> genericContainer;

    // Type containing fields with overridden names.
    FieldNameOverride overriddenNames;
}
