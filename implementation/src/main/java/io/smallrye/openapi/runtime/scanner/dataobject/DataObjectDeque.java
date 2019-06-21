/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.openapi.runtime.scanner.dataobject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

/**
 * Deque for exploring object graph.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class DataObjectDeque {

    private final Logger LOG = Logger.getLogger(DataObjectDeque.class);
    private final Deque<PathEntry> path = new ArrayDeque<>();
    private final AugmentedIndexView index;

    public DataObjectDeque(AugmentedIndexView index) {
        this.index = index;
    }

    /**
     * @see Deque#size()
     * @return the number of elements in this Deque
     */
    public int size() {
        return path.size();
    }

    /**
     * @see Deque#isEmpty()
     * @return true if no elements in this Deque
     */
    public boolean isEmpty() {
        return path.isEmpty();
    }

    /**
     * Look at top of stack, but don't remove.
     *
     * @see Deque#peek()
     * @return the top element of the stack
     */
    public PathEntry peek() {
        return path.peek();
    }

    /**
     * Push entry to stack. Does not perform cycle detection.
     *
     * @see Deque#push(Object)
     * @param entry the entry
     */
    public void push(PathEntry entry) {
        path.push(entry);
    }

    /**
     * Remove and return top element from stack.
     *
     * @see Deque#pop()
     * @return the top element of the stack
     */
    public PathEntry pop() {
        return path.pop();
    }

    /**
     * Create new entry and push to stack. Performs cycle detection.
     *
     * @param annotationTarget annotation target
     * @param parentPathEntry parent path entry
     * @param type the annotated type
     * @param schema the schema corresponding to this position
     */
    public void push(AnnotationTarget annotationTarget,
            @NotNull PathEntry parentPathEntry,
            @NotNull Type type,
            @NotNull Schema schema) {
        PathEntry entry = leafNode(parentPathEntry, annotationTarget, type, schema);
        ClassInfo klazzInfo = entry.getClazz();
        if (parentPathEntry.hasParent(entry)) {
            // Cycle detected, don't push path.
            LOG.debugv("Possible cycle was detected at: {0}. Will not search further.", klazzInfo);
            LOG.debugv("Path: {0}", entry.toStringWithGraph());
            if (schema.getDescription() == null) {
                schema.description("Cyclic reference to " + klazzInfo.name());
            }
        } else {
            // Push path to be inspected later.
            LOG.debugv("Adding child node to path: {0}", klazzInfo);
            path.push(entry);
        }
    }

    /**
     * Create a root node (first entry in graph).
     *
     * @param annotationTarget annotation target
     * @param type the annotated type
     * @param classInfo the root classInfo
     * @param rootSchema the schema corresponding to this position
     * @return a new root node
     */
    public PathEntry rootNode(AnnotationTarget annotationTarget, ClassInfo classInfo, Type type, Schema rootSchema) {
        return new PathEntry(null, annotationTarget, classInfo, type, rootSchema);
    }

    /**
     * Create a leaf node (i.e. is attached to a parent)
     *
     * @param parentNode parent node
     * @param annotationTarget annotation target
     * @param classType the class type
     * @param schema the schema
     * @return the new leaf node
     */
    public PathEntry leafNode(PathEntry parentNode,
            AnnotationTarget annotationTarget,
            Type classType,
            Schema schema) {
        ClassInfo classInfo = index.getClass(classType);
        return new PathEntry(parentNode, annotationTarget, classInfo, classType, schema);
    }

    /**
     * An entry on the object stack.
     */
    public static final class PathEntry {
        private final PathEntry enclosing;
        private final AnnotationTarget annotationTarget;
        private final Type clazzType;
        private final ClassInfo clazz;

        // May be changed
        private Schema schema;

        private PathEntry(PathEntry enclosing,
                AnnotationTarget annotationTarget,
                @NotNull ClassInfo clazz,
                @NotNull Type clazzType,
                @NotNull Schema schema) {
            this.enclosing = enclosing;
            this.annotationTarget = annotationTarget;
            this.clazz = clazz;
            this.clazzType = clazzType;
            this.schema = schema;
        }

        public boolean hasParent(PathEntry candidate) {
            PathEntry test = this;
            while (test != null) {
                if (candidate.equals(test)) {
                    return true;
                }
                test = test.enclosing;
            }
            return false;
        }

        public AnnotationTarget getAnnotationTarget() {
            return annotationTarget;
        }

        public PathEntry getEnclosing() {
            return enclosing;
        }

        public Type getClazzType() {
            return clazzType;
        }

        public ClassInfo getClazz() {
            return clazz;
        }

        public Schema getSchema() {
            return schema;
        }

        public void setSchema(Schema schema) {
            this.schema = schema;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PathEntry otherEntry = (PathEntry) o;

            boolean result = clazz != null ? clazz.equals(otherEntry.clazz) : otherEntry.clazz == null;

            // For parameterized types, do a simple check of generic arguments to
            // permit nested generic types like List<List<String>>.
            if (this.clazzType.kind() == Type.Kind.PARAMETERIZED_TYPE &&
                    otherEntry.clazzType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                return result && argsEqual(otherEntry);
            }

            return result;
        }

        private boolean argsEqual(PathEntry otherPair) {
            ParameterizedType thisClazzPType = clazzType.asParameterizedType();
            ParameterizedType otherClazzPType = otherPair.clazzType.asParameterizedType();

            List<Type> thisArgs = thisClazzPType.arguments();
            List<Type> otherArgs = otherClazzPType.arguments();
            return thisArgs.equals(otherArgs);
        }

        @Override
        public int hashCode() {
            return clazz != null ? clazz.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "PathEntry{" +
                    "clazz=" + clazz +
                    ", schema=" + schema +
                    '}';
        }

        public String toStringWithGraph() {
            return "PathEntry{" +
                    "clazz=" + clazz +
                    ", schema=" + schema +
                    ", parent=" + (enclosing != null ? enclosing.toStringWithGraph() : "<root>") + "}";
        }

    }
}
