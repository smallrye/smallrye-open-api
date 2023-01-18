package io.smallrye.openapi.runtime.scanner;

import java.util.stream.BaseStream;

public abstract class StreamStandin<E, S> implements BaseStream<E, StreamStandin<E, S>> {
    E value;
}
