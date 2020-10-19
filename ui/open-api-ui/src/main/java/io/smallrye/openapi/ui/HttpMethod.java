package io.smallrye.openapi.ui;

/**
 * List of HTTP methods that have the "Try it out" feature enabled. An empty array disables "Try it out" for all operations.
 * This does not filter the operations from the display.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum HttpMethod {

    get,
    put,
    post,
    delete,
    options,
    head,
    patch,
    trace
}
