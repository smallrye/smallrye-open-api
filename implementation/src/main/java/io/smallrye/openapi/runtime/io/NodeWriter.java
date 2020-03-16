package io.smallrye.openapi.runtime.io;

import com.fasterxml.jackson.databind.node.ObjectNode;

@FunctionalInterface
interface NodeWriter<T> {
    void write(ObjectNode node, T value, String key);
}
