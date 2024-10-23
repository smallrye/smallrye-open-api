package io.smallrye.openapi.runtime.scanner;

import static io.smallrye.openapi.api.constants.JaxbConstants.PROP_NAME;
import static io.smallrye.openapi.api.constants.JaxbConstants.XML_ROOTELEMENT;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.BaseStream;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

import io.smallrye.openapi.api.OpenApiConfig.AutoInheritance;
import io.smallrye.openapi.internal.models.media.SchemaSupport;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaFactory;
import io.smallrye.openapi.runtime.scanner.dataobject.AnnotationTargetProcessor;
import io.smallrye.openapi.runtime.scanner.dataobject.AugmentedIndexView;
import io.smallrye.openapi.runtime.scanner.dataobject.DataObjectDeque;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
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

    // Iterable (also list-type things)
    public static final DotName ITERABLE_INTERFACE_NAME = DotName.createSimple(Iterable.class.getName());
    public static final Type ITERABLE_TYPE = Type.create(ITERABLE_INTERFACE_NAME, Type.Kind.CLASS);

    // Stream
    public static final DotName STREAM_INTERFACE_NAME = DotName.createSimple(BaseStream.class.getName());
    public static final Type STREAM_TYPE = Type.create(STREAM_INTERFACE_NAME, Type.Kind.CLASS);

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
    public static final Type ARRAY_TYPE_OBJECT = Type.create(DotName.createSimple(Map[].class.getName()), Type.Kind.ARRAY);

    private static ClassInfo iterableStandin;
    private static ClassInfo mapStandin;
    private static ClassInfo streamStandin;
    private static List<ClassInfo> standinClasses;

    /*-
     * Index the "standin" collection types for internal use. These are required to wrap
     * collections of application classes (indexed elsewhere).
     */
    static {
        Indexer indexer = new Indexer();
        index(indexer, "IterableStandin.class");
        index(indexer, "MapStandin.class");
        index(indexer, "StreamStandin.class");
        Index index = indexer.complete();
        iterableStandin = index.getClassByName(DotName.createSimple(IterableStandin.class.getName()));
        mapStandin = index.getClassByName(DotName.createSimple(MapStandin.class.getName()));
        streamStandin = index.getClassByName(DotName.createSimple(StreamStandin.class.getName()));
        standinClasses = Arrays.asList(iterableStandin, mapStandin, streamStandin);
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
    private final AnnotationScannerContext context;
    private final AugmentedIndexView index;
    private final DataObjectDeque objectStack;

    /**
     * Constructor for data object scanner.
     * <p>
     * Call {@link #process()} to build and return the {@link Schema}.
     *
     * @param context scanning context
     * @param classType root to begin scan
     */
    public OpenApiDataObjectScanner(final AnnotationScannerContext context, Type classType) {
        this(context, null, classType);
    }

    public OpenApiDataObjectScanner(final AnnotationScannerContext context, AnnotationTarget annotationTarget, Type classType) {
        this.context = context;
        this.index = context.getAugmentedIndex();
        this.objectStack = new DataObjectDeque(this.index);
        this.rootClassType = classType;
        this.rootSchema = OASFactory.createSchema();
        this.rootClassInfo = initialType(classType);
        this.rootAnnotationTarget = annotationTarget;
    }

    /**
     * Build a Schema with ClassType as root.
     *
     * @param context scanning context
     * @param type root to begin scan
     * @return the OAI schema
     */
    public static Schema process(final AnnotationScannerContext context, Type type) {
        try {
            context.getScanStack().push(type);
            return new OpenApiDataObjectScanner(context, type).process();
        } finally {
            context.getScanStack().pop();
        }
    }

    /**
     * Build a Schema with PrimitiveType as root.
     *
     * @param primitive root to begin scan
     * @return the OAI schema
     */
    public static Schema process(PrimitiveType primitive) {
        Schema primitiveSchema = OASFactory.createSchema();
        TypeUtil.applyTypeAttributes(primitive, primitiveSchema);
        return primitiveSchema;
    }

    /**
     * Build the Schema
     *
     * @return the OAI schema
     */
    Schema process() {
        ScannerLogging.logger.startProcessing(rootClassType.name());

        // If top level item is simple
        if (TypeUtil.isTerminalType(rootClassType)) {
            Schema simpleSchema = OASFactory.createSchema();
            TypeUtil.applyTypeAttributes(rootClassType, simpleSchema);
            return simpleSchema;
        }

        if (isA(rootClassType, ENUM_TYPE) && index.containsClass(rootClassType)) {
            return SchemaFactory.enumToSchema(context, rootClassType);
        }

        // If top level item is not indexed
        if (rootClassInfo == null && objectStack.isEmpty()) {
            // If there's something on the objectStack stack then pre-scanning may have found something.
            ScannerLogging.logger.schemaTypeNotFound(rootClassType.name());
            return OASFactory.createSchema().addType(SchemaType.OBJECT);
        }

        // Create root node.
        DataObjectDeque.PathEntry root = objectStack.rootNode(rootAnnotationTarget, rootClassInfo, rootClassType, rootSchema);

        // For certain special types (map, list, etc) we need to do some pre-processing.
        if (standinClasses.contains(rootClassInfo)) {
            resolveSpecial(root, rootClassType, rootClassInfo); // NOSONAR - rootClassInfo is known to be non-null
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

            Type currentType = currentPathEntry.getClazzType();
            /*
             * Keep the original schema for this entry in case it needs to be modified to
             * reference a registered schema.
             */
            Schema entrySchema = currentPathEntry.getSchema();
            SchemaRegistry registry = context.getSchemaRegistry();

            if (registry.hasSchema(currentType, context.getJsonViews(), null)) {
                // This type has already been scanned and registered, don't do it again!
                entrySchema.setRef(registry.lookupRef(currentType, context.getJsonViews()).getRef());
                continue;
            }

            ClassInfo currentClass = currentPathEntry.getClazz();
            Schema currentSchema = currentPathEntry.getSchema();

            // First, handle class annotations (re-assign since readKlass may return new schema)
            currentSchema = readKlass(currentClass, currentType, currentSchema);
            TypeUtil.mapDeprecated(context, currentClass, currentSchema::getDeprecated, currentSchema::setDeprecated);
            currentPathEntry.setSchema(currentSchema);

            if (!hasNonNullType(currentSchema)) {
                // If not schema has yet been set, consider this an "object"
                SchemaSupport.setType(currentSchema, Schema.SchemaType.OBJECT);
            } else {
                maybeRegisterSchema(currentType, currentSchema, entrySchema);
            }

            List<Schema.SchemaType> types = currentSchema.getType();
            if (types != null && types.contains(Schema.SchemaType.OBJECT)) {
                // Only 'object' type schemas should have properties of their own
                ScannerLogging.logger.gettingFields(currentType, currentClass);

                // reference will be the field or method that declaring the current class type being scanned
                AnnotationTarget reference = currentPathEntry.getAnnotationTarget();

                // Get all fields *including* inherited.
                Map<String, TypeResolver> properties = TypeResolver.getAllFields(context, currentType,
                        currentClass,
                        reference);

                processClassAnnotations(currentSchema, currentClass);

                // Handle fields
                properties.values()
                        .stream()
                        .filter(resolver -> !resolver.isIgnored())
                        .forEach(resolver -> AnnotationTargetProcessor.process(context, objectStack, resolver,
                                currentPathEntry));

                processInheritance(currentPathEntry);
            }
        }
    }

    private void maybeRegisterSchema(Type currentType, Schema currentSchema, Schema entrySchema) {
        Schema ref = SchemaFactory.schemaRegistration(context, currentType, currentSchema);

        /*
         * Ignore the returned ref when:
         *
         * - the ref is the currentSchema, i.e. registration did not occur
         * - the currentSchema is an object type and will be further modified with added properties
         * - the target of the ref is the schema currently being processed and using the ref would
         * result in a self-referencing schema.
         */
        if (ref != currentSchema && !currentSchema.getType().contains(Schema.SchemaType.OBJECT) && ref.getRef() != null) {
            Schema refTarget = context.getSchemaRegistry().lookupSchema(currentType, context.getJsonViews());

            if (refTarget != entrySchema) {
                entrySchema.setAll(Collections.emptyMap());
                entrySchema.setRef(ref.getRef());
            }
        }
    }

    private static boolean hasNonNullType(Schema schema) {
        return SchemaSupport.getNonNullType(schema) != null;
    }

    private void processClassAnnotations(Schema schema, ClassInfo classInfo) {
        String xmlElementName = context.annotations().getAnnotationValue(classInfo, XML_ROOTELEMENT, PROP_NAME);

        if (xmlElementName != null && !classInfo.simpleName().equals(xmlElementName)) {
            schema.setXml(OASFactory.createXML().name(xmlElementName));
        }
    }

    private void processInheritance(DataObjectDeque.PathEntry currentPathEntry) {
        ClassInfo currentClass = currentPathEntry.getClazz();
        Schema currentSchema = currentPathEntry.getSchema();
        Type currentType = currentPathEntry.getClazzType();
        Type superClassType = currentClass.superClassType();

        if (TypeUtil.isIncludedAllOf(context, currentClass, currentType)) {
            encloseCurrentSchema(currentSchema, currentType, currentPathEntry);
        } else if (superClassType != null
                && context.getConfig().getAutoInheritance() != AutoInheritance.NONE
                && !TypeUtil.knownJavaType(superClassType.name())
                && context.annotations().getAnnotationValue(currentClass, SchemaConstant.DOTNAME_SCHEMA,
                        SchemaConstant.PROP_ALL_OF) == null) {

            Schema parentSchema = OASFactory.createSchema();
            objectStack.push(currentClass, currentPathEntry, superClassType, parentSchema);
            parentSchema = context.getSchemaRegistry().registerReference(superClassType, context.getJsonViews(), null,
                    parentSchema);
            currentSchema.addAllOf(parentSchema);

            if (context.getConfig().getAutoInheritance() == AutoInheritance.BOTH) {
                encloseCurrentSchema(currentSchema, currentType, currentPathEntry);
            }
        }
    }

    private void encloseCurrentSchema(Schema currentSchema, Type currentType, DataObjectDeque.PathEntry currentPathEntry) {
        Schema enclosingSchema = OASFactory.createSchema().allOf(currentSchema.getAllOf()).addAllOf(currentSchema);
        currentSchema.setAllOf(null);

        currentSchema = enclosingSchema;
        currentPathEntry.setSchema(currentSchema);

        if (rootClassType.equals(currentType)) {
            this.rootSchema = enclosingSchema;
        }

        if (context.getSchemaRegistry().hasSchema(currentType, context.getJsonViews(), null)) {
            // Replace the registered schema if one is present
            context.getSchemaRegistry().register(currentType, context.getJsonViews(), enclosingSchema);
        }
    }

    private Schema readKlass(ClassInfo currentClass,
            Type currentType,
            Schema currentSchema) {

        AnnotationInstance annotation = TypeUtil.getSchemaAnnotation(context, currentClass);
        Schema classSchema;

        if (annotation != null) {
            // Because of implementation= field, *may* return a new schema rather than modify.
            classSchema = SchemaFactory.readSchema(context, currentSchema, annotation, currentClass);
        } else if (isA(currentType, ENUM_TYPE)) {
            classSchema = SchemaFactory.enumToSchema(context, currentType);
        } else {
            classSchema = currentSchema;
        }

        return classSchema;
    }

    private void resolveSpecial(DataObjectDeque.PathEntry root, Type type, ClassInfo standin) {
        ParameterizedType standinType = standin.interfaceTypes().get(0).asParameterizedType();

        if (typeArgumentMismatch(type, standin)) {
            /*
             * The type's generic arguments are not in alignment with the type
             * parameters of the stand-in collection type. For the purposes
             * of obtaining the stand-in collection's schema, we discard the
             * original type and determine the correct type parameter for the
             * stand-in.
             */
            type = TypeResolver.resolveParameterizedAncestor(context, type, standinType)
                    .map(Type.class::cast)
                    .orElse(type);
        }

        Map<String, TypeResolver> fieldResolution = TypeResolver.getAllFields(context, type, standin,
                root.getAnnotationTarget());
        rootSchema = preProcessSpecial(type, fieldResolution.values().iterator().next(), root);
    }

    private boolean typeArgumentMismatch(Type type, ClassInfo standin) {
        if (type.kind() != Kind.PARAMETERIZED_TYPE) {
            return true;
        }

        return standin.typeParameters().size() < type.asParameterizedType().arguments().size();
    }

    private Schema preProcessSpecial(Type type, TypeResolver typeResolver, DataObjectDeque.PathEntry currentPathEntry) {
        return AnnotationTargetProcessor.process(context, objectStack, typeResolver, currentPathEntry, type);
    }

    private boolean isA(Type testSubject, Type test) {
        return TypeUtil.isA(context, testSubject, test);
    }

    private ClassInfo initialType(Type type) {
        if (isA(type, ITERABLE_TYPE)) {
            return iterableStandin;
        }

        if (isA(type, MAP_TYPE)) {
            return mapStandin;
        }

        if (isA(type, STREAM_TYPE)) {
            return streamStandin;
        }

        return index.getClass(type);
    }

}
