package io.smallrye.openapi.runtime.io;

import com.fasterxml.jackson.databind.node.ObjectNode;

@FunctionalInterface
public interface NodeWriter<T> {
    void write(ObjectNode node, T value, String key);
}
