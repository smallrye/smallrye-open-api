package io.smallrye.openapi.runtime.io.example;

import io.smallrye.openapi.runtime.io.Referenceable;

/**
 * Constants related to Example.
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#exampleObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class ExampleConstant implements Referenceable {

    public static final String PROP_NAME = "name";
    public static final String PROP_VALUE = "value";
    public static final String PROP_SUMMARY = "summary";
    public static final String PROP_EXTERNAL_VALUE = "externalValue";
    public static final String PROP_EXAMPLES = "examples";
    public static final String PROP_DESCRIPTION = "description";

    private ExampleConstant() {
    }
}
