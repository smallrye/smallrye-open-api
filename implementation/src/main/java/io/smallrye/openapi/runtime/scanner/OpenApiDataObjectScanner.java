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
package io.smallrye.openapi.runtime.scanner;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.scanner.dataobject.AnnotationTargetProcessor;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.DataObjectDeque;
import io.smallrye.openapi.runtime.scanner.dataobject.IgnoreResolver;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.util.SchemaFactory;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Explores the class graph from the provided root, creating an OpenAPI {@link Schema}
 * from the entities encountered.
 * <p>
 * A depth first search is performed, with the following precedence (high to low):
 * <ol>
 * <li>Explicitly provided attributes/overrides on <tt>@Schema</tt> annotated elements.
 * Note that some attributes have special behaviours: for example, <tt>ref</tt> is mutually
 * exclusive, and <tt>implementation</tt> replaces the implementation entirely.</li>
 * <li>Unannotated fields unless property <tt>openapi.infer-unannotated-types</tt> set false</li>
 * <li>Inferred attributes, such as name, type, format, etc.</li>
 * </ol>
 *
 * <p>
 * Well-known types, such as Collection, Map, Date, etc, are handled in a custom manner.
 * Jandex-indexed objects from the user's deployment are traversed until a terminal type is
 * met (such as a primitive, boxed primitive, date, etc), or an entity is encountered that is not
 * well-known or is not in the Jandex {@link IndexView}.
 *
 * <em>Current Limitations:</em>
 * If a type is not available in the provided IndexView then it is not accessible. Excepting
 * well-known types, this means non-deployment objects may not be scanned.
 * <p>
 * Future work could consider making the user's deployment classes available to this classloader,
 * with additional code to traverse non-Jandex types reachable from this classloader. But, this is
 * troublesome for performance, security and initialisation reasons -- particular caution would
 * be needed to avoid accidental initialisation of classes that may have externally visible side-effects.
 *
 * @see org.eclipse.microprofile.openapi.annotations.media.Schema Schema Annotation
 * @see Schema Schema Object
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class OpenApiDataObjectScanner {

    private static final Logger LOG = Logger.getLogger(OpenApiDataObjectScanner.class);
    // Object
    public static final Type OBJECT_TYPE = Type.create(DotName.createSimple(java.lang.Object.class.getName()), Type.Kind.CLASS);
    // Collection (list-type things)
    public static final DotName COLLECTION_INTERFACE_NAME = DotName.createSimple(Collection.class.getName());
    public static final Type COLLECTION_TYPE = Type.create(COLLECTION_INTERFACE_NAME, Type.Kind.CLASS);
    // Map
    public static final DotName MAP_INTERFACE_NAME = DotName.createSimple(Map.class.getName());
    public static final Type MAP_TYPE = Type.create(MAP_INTERFACE_NAME, Type.Kind.CLASS);
    // Enum
    public static final DotName ENUM_INTERFACE_NAME = DotName.createSimple(Enum.class.getName());
    public static final Type ENUM_TYPE = Type.create(ENUM_INTERFACE_NAME, Type.Kind.CLASS);
    // String type
    public static final Type STRING_TYPE = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);
    // Array type
    public static final Type ARRAY_TYPE_OBJECT = ArrayType.create(DotName.createSimple("[Ljava.lang.Object;"), Type.Kind.ARRAY);

    private Schema rootSchema;
    private AnnotationTarget rootAnnotationTarget;
    private final Type rootClassType;
    private final ClassInfo rootClassInfo;
    private final TypeUtil.TypeWithFormat rootClassTypeFormat;
    private final AugmentedIndexView index;
    private final DataObjectDeque objectStack;
    private final IgnoreResolver ignoreResolver;

    /**
     * Constructor for data object scanner.
     * <p>
     * Call {@link #process()} to build and return the {@link Schema}.
     *
     * @param _index index of types to scan
     * @param classType root to begin scan
     */
    public OpenApiDataObjectScanner(IndexView _index, Type classType) {
        this.index = new AugmentedIndexView(_index);
        this.objectStack = new DataObjectDeque(index);
        this.ignoreResolver = new IgnoreResolver(index);
        this.rootClassType = classType;
        this.rootClassTypeFormat = TypeUtil.getTypeFormat(classType);
        this.rootSchema = new SchemaImpl();
        this.rootClassInfo = initialType(classType);
    }

    OpenApiDataObjectScanner(IndexView _index, AnnotationTarget annotationTarget, Type classType) {
        this.index = new AugmentedIndexView(_index);
        this.objectStack = new DataObjectDeque(index);
        this.ignoreResolver = new IgnoreResolver(index);
        this.rootClassType = classType;
        this.rootClassTypeFormat = TypeUtil.getTypeFormat(classType);
        this.rootSchema = new SchemaImpl();
        this.rootClassInfo = initialType(classType);
        this.rootAnnotationTarget = annotationTarget;
    }

    /**
     * Build a Schema with ClassType as root.
     *
     * @param index index of types to scan
     * @param type root to begin scan
     * @return the OAI schema
     */
    public static Schema process(IndexView index, Type type) {
        return new OpenApiDataObjectScanner(index, type).process();
    }

    /**
     * Build a Schema with PrimitiveType as root.
     *
     * @param primitive root to begin scan
     * @return the OAI schema
     */
    public static Schema process(PrimitiveType primitive) {
        TypeUtil.TypeWithFormat typeFormat = TypeUtil.getTypeFormat(primitive);
        Schema primitiveSchema = new SchemaImpl();
        primitiveSchema.setType(typeFormat.getSchemaType());
        primitiveSchema.setFormat(typeFormat.getFormat().format());
        return primitiveSchema;
    }

    /**
     * Build the Schema
     *
     * @return the OAI schema
     */
    Schema process() {
        LOG.debugv("Starting processing with root: {0}", rootClassType.name());

        // If top level item is simple
        if (isTerminalType(rootClassType)) {
            SchemaImpl simpleSchema = new SchemaImpl();
            simpleSchema.setType(rootClassTypeFormat.getSchemaType());
            simpleSchema.setFormat(rootClassTypeFormat.getFormat().format());
            return simpleSchema;
        }

        // If top level item is not indexed
        if (rootClassInfo == null && objectStack.isEmpty()) {
            // If there's something on the objectStack stack then pre-scanning may have found something.
            return null;
        }

        // Create root node.
        DataObjectDeque.PathEntry root = objectStack.rootNode(rootAnnotationTarget, rootClassInfo, rootClassType, rootSchema);

        // For certain special types (map, list, etc) we need to do some pre-processing.
        if (isSpecialType(rootClassType)) {
            resolveSpecial(root, rootClassType);
        } else {
            objectStack.push(root);
        }

        depthFirstGraphSearch();
        return rootSchema;
    }

    // Scan depth first.
    private void depthFirstGraphSearch() {
        DataObjectDeque.PathEntry currentPathEntry;

        while (!objectStack.isEmpty()) {
            currentPathEntry = objectStack.pop();

            ClassInfo currentClass = currentPathEntry.getClazz();
            Schema currentSchema = currentPathEntry.getSchema();
            Type currentType = currentPathEntry.getClazzType();

            // First, handle class annotations.
            currentPathEntry.setSchema(readKlass(currentClass, currentSchema));

            if (currentSchema.getType() == null) {
                // If not schema has yet been set, consider this an "object"
                currentSchema.setType(Schema.SchemaType.OBJECT);
            }

            LOG.debugv("Getting all fields for: {0} in class: {1}", currentType, currentClass);

            // Get all fields *including* inherited.
            Map<FieldInfo, TypeResolver> allFields = TypeResolver.getAllFields(index, currentType, currentClass);

            // Handle fields
            for (Map.Entry<FieldInfo, TypeResolver> entry : allFields.entrySet()) {
                FieldInfo field = entry.getKey();
                TypeResolver resolver = entry.getValue();
                // Ignore static fields and fields annotated with ignore.
                if (!Modifier.isStatic(field.flags()) && !ignoreResolver.isIgnore(field, currentPathEntry)) {
                    LOG.debugv("Iterating field {0}", field);
                    AnnotationTargetProcessor.process(index, objectStack, resolver, currentPathEntry, field);
                }
            }

            // Handle methods
            // TODO put it here!
        }
    }

    private Schema readKlass(ClassInfo currentClass,
            Schema currentSchema) {
        AnnotationInstance annotation = TypeUtil.getSchemaAnnotation(currentClass);
        if (annotation != null) {
            // Because of implementation= field, *may* return a new schema rather than modify.
            return SchemaFactory.readSchema(index, currentSchema, annotation, Collections.emptyMap());
        }
        return currentSchema;
    }

    private void resolveSpecial(DataObjectDeque.PathEntry root, Type type) {
        Map<FieldInfo, TypeResolver> fieldResolution = TypeResolver.getAllFields(index, type, rootClassInfo);
        rootSchema = preProcessSpecial(type, fieldResolution.values().iterator().next(), root);
    }

    private Schema preProcessSpecial(Type type, TypeResolver typeResolver, DataObjectDeque.PathEntry currentPathEntry) {
        return AnnotationTargetProcessor.process(index, objectStack, typeResolver, currentPathEntry, type);
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(index, testSubject, test);
    }

    private boolean isTerminalType(Type type) {
        if (type.kind() == Type.Kind.TYPE_VARIABLE ||
                type.kind() == Type.Kind.WILDCARD_TYPE ||
                type.kind() == Type.Kind.ARRAY) {
            return false;
        }

        if (type.kind() == Type.Kind.PRIMITIVE ||
                type.kind() == Type.Kind.VOID) {
            return true;
        }

        TypeUtil.TypeWithFormat tf = TypeUtil.getTypeFormat(type);
        // If is known type.
        return tf.getSchemaType() != Schema.SchemaType.OBJECT &&
                tf.getSchemaType() != Schema.SchemaType.ARRAY;
    }

    // Is Map, Collection, etc.
    private boolean isSpecialType(Type type) {
        return isA(type, COLLECTION_TYPE) || isA(type, MAP_TYPE);
    }

    private ClassInfo initialType(Type type) {
        if (isA(type, COLLECTION_TYPE)) {
            return index.getClass(CollectionStandin.class);
        }

        if (isA(type, MAP_TYPE)) {
            return index.getClass(MapStandin.class);
        }

        return index.getClass(type);
    }

}
