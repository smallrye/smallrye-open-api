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

    static final String PROP_NAME = "name";
    static final String PROP_VALUE = "value";
    static final String PROP_SUMMARY = "summary";
    static final String PROP_EXTERNAL_VALUE = "externalValue";
    static final String PROP_DESCRIPTION = "description";

    private ExampleConstant() {
    }
}
