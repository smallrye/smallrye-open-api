package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.dataobject.AnnotationTargetProcessor;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.DataObjectDeque;
import io.smallrye.openapi.runtime.scanner.dataobject.IgnoreResolver;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Explores the class graph from the provided root, creating an OpenAPI {@link Schema}
 * from the entities encountered.
 * <p>
 * A depth first search is performed, with the following precedence (high to low):
 * <ol>
 * <li>Explicitly provided attributes/overrides on {@literal @}Schema annotated elements.
 * Note that some attributes have special behaviours: for example, ref is mutually
 * exclusive, and implementation replaces the implementation entirely.</li>
 * <li>Unannotated fields unless property openapi.infer-unannotated-types set false</li>
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

    // Object
    public static final Type OBJECT_TYPE = Type.create(DotName.createSimple(java.lang.Object.class.getName()), Type.Kind.CLASS);
    // Collection (list-type things)
    public static final DotName COLLECTION_INTERFACE_NAME = DotName.createSimple(Collection.class.getName());
    public static final Type COLLECTION_TYPE = Type.create(COLLECTION_INTERFACE_NAME, Type.Kind.CLASS);
    // Iterable (also list-type things)
    public static final DotName ITERABLE_INTERFACE_NAME = DotName.createSimple(Iterable.class.getName());
    public static final Type ITERABLE_TYPE = Type.create(ITERABLE_INTERFACE_NAME, Type.Kind.CLASS);
    // Map
    public static final DotName MAP_INTERFACE_NAME = DotName.createSimple(Map.class.getName());
    public static final Type MAP_TYPE = Type.create(MAP_INTERFACE_NAME, Type.Kind.CLASS);
    // Set
    public static final DotName SET_INTERFACE_NAME = DotName.createSimple(java.util.Set.class.getName());
    public static final Type SET_TYPE = Type.create(SET_INTERFACE_NAME, Type.Kind.CLASS);
    // Enum
    public static final DotName ENUM_INTERFACE_NAME = DotName.createSimple(Enum.class.getName());
    public static final Type ENUM_TYPE = Type.create(ENUM_INTERFACE_NAME, Type.Kind.CLASS);
    // String type
    public static final Type STRING_TYPE = Type.create(DotName.createSimple(String.class.getName()), Type.Kind.CLASS);
    // Array type
    public static final Type ARRAY_TYPE_OBJECT = Type.create(DotName.createSimple("[Ljava.lang.Object;"), Type.Kind.ARRAY);

    private static ClassInfo collectionStandin;
    private static ClassInfo iterableStandin;
    private static ClassInfo mapStandin;

    /*-
     * Index the "standin" collection types for internal use. These are required to wrap
     * collections of application classes (indexed elsewhere).
     */
    static {
        Indexer indexer = new Indexer();
        index(indexer, "CollectionStandin.class");
        index(indexer, "IterableStandin.class");
        index(indexer, "MapStandin.class");
        Index index = indexer.complete();
        collectionStandin = index.getClassByName(DotName.createSimple(CollectionStandin.class.getName()));
        iterableStandin = index.getClassByName(DotName.createSimple(IterableStandin.class.getName()));
        mapStandin = index.getClassByName(DotName.createSimple(MapStandin.class.getName()));
    }

    private static void index(Indexer indexer, String resourceName) {
        try (InputStream stream = OpenApiDataObjectScanner.class.getResourceAsStream(resourceName)) {
            indexer.index(stream);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private Schema rootSchema;
    private AnnotationTarget rootAnnotationTarget;
    private final Type rootClassType;
    private final ClassInfo rootClassInfo;
    private final AugmentedIndexView index;
    private final ClassLoader cl;
    private final DataObjectDeque objectStack;
    private final IgnoreResolver ignoreResolver;

    /**
     * Constructor for data object scanner.
     * <p>
     * Call {@link #process()} to build and return the {@link Schema}.
     *
     * @param index index of types to scan
     * @param classType root to begin scan
     */
    public OpenApiDataObjectScanner(IndexView index, Type classType) {
        this(index, ClassLoaderUtil.getDefaultClassLoader(), null, classType);
    }

    /**
     * Constructor for data object scanner.
     * <p>
     * Call {@link #process()} to build and return the {@link Schema}.
     *
     * @param index index of types to scan
     * @param cl the classloader to use
     * @param classType root to begin scan
     */
    public OpenApiDataObjectScanner(IndexView index, ClassLoader cl, Type classType) {
        this(index, cl, null, classType);
    }

    public OpenApiDataObjectScanner(IndexView index, AnnotationTarget annotationTarget, Type classType) {
        this(index, ClassLoaderUtil.getDefaultClassLoader(), annotationTarget, classType);
    }

    public OpenApiDataObjectScanner(IndexView index, ClassLoader cl, AnnotationTarget annotationTarget, Type classType) {
        this.index = AugmentedIndexView.augment(index);
        this.cl = cl;
        this.objectStack = new DataObjectDeque(this.index);
        this.ignoreResolver = new IgnoreResolver(this.index);
        this.rootClassType = classType;
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
    public static Schema process(IndexView index, ClassLoader cl, Type type) {
        return new OpenApiDataObjectScanner(index, cl, type).process();
    }

    /**
     * Build a Schema with PrimitiveType as root.
     *
     * @param primitive root to begin scan
     * @return the OAI schema
     */
    public static Schema process(PrimitiveType primitive) {
        Schema primitiveSchema = new SchemaImpl();
        TypeUtil.applyTypeAttributes(primitive, primitiveSchema);
        return primitiveSchema;
    }

    /**
     * Build the Schema
     *
     * @return the OAI schema
     */
    Schema process() {
        ScannerLogging.log.startProcessing(rootClassType.name());

        // If top level item is simple
        if (TypeUtil.isTerminalType(rootClassType)) {
            SchemaImpl simpleSchema = new SchemaImpl();
            TypeUtil.applyTypeAttributes(rootClassType, simpleSchema);
            return simpleSchema;
        }

        if (isA(rootClassType, ENUM_TYPE) && index.containsClass(rootClassType)) {
            return SchemaFactory.enumToSchema(index, cl, rootClassType);
        }

        // If top level item is not indexed
        if (rootClassInfo == null && objectStack.isEmpty()) {
            // If there's something on the objectStack stack then pre-scanning may have found something.
            return new SchemaImpl().type(SchemaType.OBJECT);
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
        while (!objectStack.isEmpty()) {
            DataObjectDeque.PathEntry currentPathEntry = objectStack.pop();

            ClassInfo currentClass = currentPathEntry.getClazz();
            Schema currentSchema = currentPathEntry.getSchema();
            Type currentType = currentPathEntry.getClazzType();

            if (SchemaRegistry.hasSchema(currentType, null)) {
                // This type has already been scanned and registered, don't do it again!
                continue;
            }

            // First, handle class annotations (re-assign since readKlass may return new schema)
            currentSchema = readKlass(currentClass, currentType, currentSchema);
            currentPathEntry.setSchema(currentSchema);

            if (currentSchema.getType() == null) {
                // If not schema has yet been set, consider this an "object"
                currentSchema.setType(Schema.SchemaType.OBJECT);
            } else {
                // Ignore the returned ref, the currentSchema will be further modified with added properties
                SchemaFactory.schemaRegistration(index, currentType, currentSchema);
            }

            if (currentSchema.getType() != Schema.SchemaType.OBJECT) {
                // Only 'object' type schemas should have properties of their own
                continue;
            }

            ScannerLogging.log.gettingFields(currentType, currentClass);

            // reference will be the field or method that declaring the current class type being scanned
            AnnotationTarget reference = currentPathEntry.getAnnotationTarget();

            // Get all fields *including* inherited.
            Map<String, TypeResolver> properties = TypeResolver.getAllFields(index, ignoreResolver, currentType, currentClass,
                    reference);

            // Handle fields
            properties.values()
                    .stream()
                    .filter(resolver -> !resolver.isIgnored())
                    .forEach(resolver -> AnnotationTargetProcessor.process(index, cl, objectStack, resolver, currentPathEntry));
        }
    }

    private Schema readKlass(ClassInfo currentClass,
            Type currentType,
            Schema currentSchema) {
        AnnotationInstance annotation = TypeUtil.getSchemaAnnotation(currentClass);
        if (annotation != null) {
            // Because of implementation= field, *may* return a new schema rather than modify.
            return SchemaFactory.readSchema(index, cl, currentSchema, annotation, currentClass);
        } else if (isA(currentType, ENUM_TYPE)) {
            return SchemaFactory.enumToSchema(index, cl, currentType);
        }
        return currentSchema;
    }

    private void resolveSpecial(DataObjectDeque.PathEntry root, Type type) {
        Map<String, TypeResolver> fieldResolution = TypeResolver.getAllFields(index, ignoreResolver, type, rootClassInfo,
                root.getAnnotationTarget());
        rootSchema = preProcessSpecial(type, fieldResolution.values().iterator().next(), root);
    }

    private Schema preProcessSpecial(Type type, TypeResolver typeResolver, DataObjectDeque.PathEntry currentPathEntry) {
        return AnnotationTargetProcessor.process(index, cl, objectStack, typeResolver, currentPathEntry, type);
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(index, cl, testSubject, test);
    }

    // Is Map, Collection, etc.
    private boolean isSpecialType(Type type) {
        return isA(type, COLLECTION_TYPE) || isA(type, ITERABLE_TYPE) || isA(type, MAP_TYPE);
    }

    private ClassInfo initialType(Type type) {
        if (isA(type, COLLECTION_TYPE)) {
            return collectionStandin;
        }

        if (isA(type, ITERABLE_TYPE)) {
            return iterableStandin;
        }

        if (isA(type, MAP_TYPE)) {
            return mapStandin;
        }

        return index.getClass(type);
    }

}
