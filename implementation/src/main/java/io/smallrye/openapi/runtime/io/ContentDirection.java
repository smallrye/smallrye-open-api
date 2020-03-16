package io.smallrye.openapi.runtime.io;

/**
 * Simple enum to indicate whether an {@literal @}Content annotation being processed is
 * an input or an output.
 * 
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public enum ContentDirection {
    Input,
    Output,
    Parameter
}