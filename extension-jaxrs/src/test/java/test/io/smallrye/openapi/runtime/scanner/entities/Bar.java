package test.io.smallrye.openapi.runtime.scanner.entities;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class Bar<T extends Baz, Q>
        extends Ultimate<Q> {
    T theT;

    Q theQ;
}
