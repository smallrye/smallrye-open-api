package io.smallrye.openapi.runtime.scanner.dataobject;

import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;

import io.smallrye.openapi.api.constants.JacksonConstants;
import io.smallrye.openapi.api.constants.JaxbConstants;
import io.smallrye.openapi.api.constants.JsonbConstants;
import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class TypeResolver {

    private static final Type BOOLEAN_TYPE = Type.create(DotName.createSimple(Boolean.class.getName()), Type.Kind.CLASS);
    static final String METHOD_PREFIX_GET = "get";
    static final String METHOD_PREFIX_IS = "is";
    static final String METHOD_PREFIX_SET = "set";

    private final UnaryOperator<String> nameTranslator;
    private final Deque<Map<String, Type>> resolutionStack;
    private final String propertyName;
    private FieldInfo field;
    private MethodInfo readMethod;
    private MethodInfo writeMethod;
    private boolean ignored = false;
    private boolean exposed = false;
    private boolean readOnly = false;
    private boolean writeOnly = false;
    private Type leaf;
    private final List<AnnotationTarget> constraintTargets = new ArrayList<>();
    private String propertyNamePrefix;
    private String propertyNameSuffix;

    /**
     * A comparator to order the field, write method, and read method in the {@link #targets}
     * {@link PriorityQueue}. The priority is:
     * <ol>
     * <li>Items annotated with MP Open API {@link org.eclipse.microprofile.openapi.annotations.media.Schema @Schema}
     * <li>Items annotated with JSON-B {@link javax.json.bind.annotation.JsonbProperty @JsonbProperty}
     * <li>Items annotated with Jackson {@link com.fasterxml.jackson.annotation.JsonProperty @JsonProperty}
     * <li>Items annotated with JAXB {@link javax.xml.bind.annotation.XmlElement @XmlElement}
     * <li>Items annotated with JAXB {@link javax.xml.bind.annotation.XmlAttribute @XmlAttribute}
     * <li>Fields
     * <li>Getter/accessor methods
     * <li>Setter/mutator methods
     * </ol>
     *
     */
    private static final Comparator<AnnotationTarget> targetComparator = (t1, t2) -> {
        int result;

        // Annotated elements sort to the top of the priority queue
        if ((result = compareAnnotation(t1, t2, SchemaConstant.DOTNAME_SCHEMA)) != 0) {
            return result;
        }
        for (DotName jsonbProperty : JsonbConstants.JSONB_PROPERTY) {
            if ((result = compareAnnotation(t1, t2, jsonbProperty)) != 0) {
                return result;
            }
        }
        if ((result = compareAnnotation(t1, t2, JacksonConstants.JSON_PROPERTY)) != 0) {
            return result;
        }
        for (DotName xmlElement : JaxbConstants.XML_ELEMENT) {
            if ((result = compareAnnotation(t1, t2, xmlElement)) != 0) {
                return result;
            }
        }
        for (DotName xmlAttribute : JaxbConstants.XML_ATTRIBUTE) {
            if ((result = compareAnnotation(t1, t2, xmlAttribute)) != 0) {
                return result;
            }
        }
        if (t1.kind() == Kind.FIELD) {
            return -1;
        }
        if (t2.kind() == Kind.FIELD) {
            return +1;
        }
        if (isAccessor(t1.asMethod()) && !isAccessor(t2.asMethod())) {
            return -1;
        }

        return 0;
    };

    /**
     * Queue of this resolvers field, write method, and read method. The highest priority "best"
     * target is in the first position, using the order determined by
     * {@link TypeResolver#targetComparator targetComparator}.
     */
    private Queue<AnnotationTarget> targets = new PriorityQueue<>(targetComparator);

    private static int compareAnnotation(AnnotationTarget t1, AnnotationTarget t2, DotName annotationName) {
        boolean hasAnno1 = TypeUtil.hasAnnotation(t1, annotationName);
        boolean hasAnno2 = TypeUtil.hasAnnotation(t2, annotationName);

        // Element with @Schema is top priority
        if (hasAnno1) {
            if (!hasAnno2) {
                return -1;
            }
        } else {
            if (hasAnno2) {
                return 1;
            }
        }

        return 0;
    }

    private TypeResolver(UnaryOperator<String> nameTranslator, String propertyName, FieldInfo field,
            Deque<Map<String, Type>> resolutionStack) {
        this.nameTranslator = nameTranslator;
        this.propertyName = propertyName;
        this.field = field;
        this.resolutionStack = resolutionStack;

        if (field != null) {
            this.leaf = field.type();
            targets.add(field);
        } else {
            this.leaf = null;
        }
    }

    /**
     * Get the declaring class of the annotation target.
     *
     * @return the {@link ClassInfo} of the class that declares the optimal annotation target for the type represented by the
     *         current instance.
     */
    public ClassInfo getDeclaringClass() {
        return TypeUtil.getDeclaringClass(getAnnotationTarget());
    }

    /**
     * Get the annotation target that represents this instance's schema property.
     *
     * @return the optimal annotation target for the type represented by the current instance.
     */
    public AnnotationTarget getAnnotationTarget() {
        return targets.peek();
    }

    public Type getUnresolvedType() {
        return this.leaf;
    }

    /**
     * Determine the name of the instance's property. The name may be overridden by
     * one of several annotations, selected in the following order:
     *
     * <ol>
     * <li>MP Open API <code>@Schema</code>
     * <li>JSON-B <code>@JsonbProperty</code>
     * <li>Jackson <code>@JsonProperty</code>
     * <li>JAXB <code>@XmlElement</code>
     * <li>JAXB <code>@XmlAttribute</code>
     * </ol>
     *
     * If no elements have been selected, the default Java bean property name will
     * be returned.
     *
     * @return name of property
     */
    public String getPropertyName() {
        AnnotationTarget target = getAnnotationTarget();
        String name;

        if ((name = TypeUtil.getAnnotationValue(target,
                SchemaConstant.DOTNAME_SCHEMA,
                SchemaConstant.PROP_NAME)) != null) {
            return wrap(name);
        }

        if ((name = TypeUtil.getAnnotationValue(target,
                JsonbConstants.JSONB_PROPERTY,
                JsonbConstants.PROP_VALUE)) != null) {
            return wrap(name);
        }

        if ((name = TypeUtil.getAnnotationValue(target,
                JacksonConstants.JSON_PROPERTY,
                JacksonConstants.PROP_VALUE)) != null) {
            return wrap(name);
        }

        return nameTranslator.apply(wrap(this.propertyName));
    }

    private String wrap(String name) {
        if (this.propertyNamePrefix == null && this.propertyNameSuffix == null) {
            return name;
        }
        StringBuilder wrapped = new StringBuilder();
        if (this.propertyNamePrefix != null) {
            wrapped.append(this.propertyNamePrefix);
        }
        wrapped.append(name);
        if (this.propertyNameSuffix != null) {
            wrapped.append(this.propertyNameSuffix);
        }
        return wrapped.toString();
    }

    public String getBeanPropertyName() {
        return this.propertyName;
    }

    /**
     * Retrieves the field associated with this property. May be null.
     *
     * @return field property
     */
    public FieldInfo getField() {
        return field;
    }

    private void setField(FieldInfo field) {
        this.field = field;
    }

    /**
     * Retrieves the read method (getter) associated with this property.
     * May be null.
     *
     * @return the property's read method (getter)
     */
    public MethodInfo getReadMethod() {
        return readMethod;
    }

    /**
     * Sets the read method for the property. May replace a previously-set method
     * in the case where an interface defines an annotated method with more
     * information than the implementation of the method.
     *
     * @param readMethod the property's read method (getter/accessor)
     */
    private void setReadMethod(MethodInfo readMethod) {
        if (this.readMethod != null) {
            targets.remove(this.readMethod);
        }

        this.readMethod = readMethod;

        if (readMethod != null) {
            this.leaf = readMethod.returnType();
            targets.add(readMethod);
        }
    }

    /**
     * Retrieves the write method (setter) associated with this property.
     * May be null.
     *
     * @return the property's write method (setter)
     */
    public MethodInfo getWriteMethod() {
        return writeMethod;
    }

    /**
     * Sets the write method for the property. May replace a previously-set method
     * in the case where an interface defines an annotated method with more
     * information than the implementation of the method.
     *
     * @param writeMethod the property's write method (setter/mutator)
     */
    private void setWriteMethod(MethodInfo writeMethod) {
        if (this.writeMethod != null) {
            targets.remove(this.writeMethod);
        }

        this.writeMethod = writeMethod;

        if (writeMethod != null) {
            this.leaf = writeMethod.parameterType(0);
            targets.add(writeMethod);
        }
    }

    /**
     * Resolve the type that was used to initially construct this {@link TypeResolver}
     *
     * @return the resolved type (if found)
     */
    public Type resolveType() {
        return getResolvedType(leaf);
    }

    public boolean isIgnored() {
        return ignored || (readOnly && readMethod == null) || (writeOnly && writeMethod == null);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isWriteOnly() {
        return writeOnly;
    }

    private boolean isExposedByDefault() {
        return !isIgnored() && !exposed;
    }

    /**
     * Resolve a type against this {@link TypeResolver}'s resolution stack
     *
     * @param fieldType type to resolve
     * @return resolved type (if found)
     */
    private Type getResolvedType(Type fieldType) {
        Type current = TypeUtil.resolveWildcard(fieldType);

        for (Map<String, Type> map : resolutionStack) {
            String varName = null;

            switch (current.kind()) {
                case TYPE_VARIABLE:
                    varName = current.asTypeVariable().identifier();
                    break;
                case UNRESOLVED_TYPE_VARIABLE:
                    varName = current.asUnresolvedTypeVariable().identifier();
                    break;
                default:
                    break;
            }

            // Look in next entry map-set if the name is present.
            if (varName != null && map.containsKey(varName)) {
                current = map.get(varName);
            }
        }
        return current;
    }

    private static Type[] resolveArguments(ParameterizedType type, UnaryOperator<Type> resolver) {
        return type.arguments().stream().map(resolver).toArray(Type[]::new);
    }

    /**
     * Resolve a parameterized type against this {@link TypeResolver}'s resolution stack.
     * If any of the type's arguments are wild card types, the resolution will fall back
     * to the basic {@link #getResolvedType(Type)} method, resolving none of
     * the arguments.
     *
     * @param type type to resolve
     * @return resolved type (if found)
     */
    private Type getResolvedType(ParameterizedType type) {
        if (type.arguments().stream().noneMatch(arg -> arg.kind() == Type.Kind.WILDCARD_TYPE)) {
            return ParameterizedType.create(type.name(), resolveArguments(type, this::resolve), null);
        }

        return getResolvedType((Type) type);
    }

    /**
     * Resolve a parameterized type against this {@link TypeResolver}'s resolution stack.
     * If any of the type's arguments are wild card types, the resolution will fall back
     * to the basic {@link #getResolvedType(Type)} method, resolving none of
     * the arguments.
     *
     * @param type type to resolve
     * @return resolved type (if found)
     */
    public Type resolve(Type type) {
        Type resolvedType;

        if (type == null) {
            resolvedType = null;
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            resolvedType = getResolvedType(type.asParameterizedType());
        } else {
            resolvedType = getResolvedType(type);
        }

        return resolvedType;
    }

    public List<AnnotationTarget> getConstraintTargets() {
        return constraintTargets;
    }

    /**
     * Create a new TypeResolver for the given ClassInfo clazz and type. If the Type leaf if not
     * available, the type of the clazz will be used, without parameters.
     * 
     * @param context current scanner context
     * @param clazz class to be resolved
     * @param leaf the type of clazz where referenced
     * @return a new TypeResolver
     */
    public static TypeResolver forClass(AnnotationScannerContext context, ClassInfo clazz, Type leaf) {
        final IndexView index = context.getAugmentedIndex();
        Type clazzType = leaf != null ? leaf : Type.create(clazz.name(), Type.Kind.CLASS);
        Map<ClassInfo, Type> chain = JandexUtil.inheritanceChain(index, clazz, clazzType);
        Deque<Map<String, Type>> stack = new ArrayDeque<>();
        boolean allOfMatch = false;

        for (Map.Entry<ClassInfo, Type> entry : chain.entrySet()) {
            ClassInfo currentClass = entry.getKey();
            Type currentType = entry.getValue();

            if (currentType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                Map<String, Type> resMap = buildParamTypeResolutionMap(currentClass, currentType.asParameterizedType());
                stack.push(resMap);
            }

            if (allOfMatch || (!currentType.equals(clazzType) && TypeUtil.isIncludedAllOf(clazz, currentType))) {
                allOfMatch = true;
            }
        }

        return new TypeResolver(getPropertyNameTranslator(context, clazz), null, null, stack);
    }

    public static Map<String, TypeResolver> getAllFields(AnnotationScannerContext context, Type leaf,
            ClassInfo leafKlazz, AnnotationTarget reference) {
        final AugmentedIndexView index = context.getAugmentedIndex();
        Map<ClassInfo, Type> chain = JandexUtil.inheritanceChain(index, leafKlazz, leaf);
        Map<String, TypeResolver> properties = new LinkedHashMap<>();
        Deque<Map<String, Type>> stack = new ArrayDeque<>();
        boolean skipPropertyScan = false;
        List<ClassInfo> descendants = new ArrayList<>(chain.size());

        for (Map.Entry<ClassInfo, Type> entry : chain.entrySet()) {
            ClassInfo currentClass = entry.getKey();
            Type currentType = entry.getValue();

            if (currentType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                Map<String, Type> resMap = buildParamTypeResolutionMap(currentClass, currentType.asParameterizedType());
                stack.push(resMap);
            }

            if (skipPropertyScan || (!currentType.equals(leaf) && TypeUtil.isIncludedAllOf(leafKlazz, currentType))
                    || TypeUtil.knownJavaType(currentClass.name())) {
                /*
                 * Do not attempt to introspect fields of Java/JDK types or if the @Schema
                 * annotation indicates the use of a `ref` for superclass fields.
                 */
                skipPropertyScan = true;
                continue;
            }

            // Store all field properties
            JandexUtil.fields(context, currentClass)
                    .stream()
                    .filter(TypeResolver::acceptField)
                    .forEach(field -> scanField(context, properties, field, stack, reference, descendants));

            methods(context, currentClass)
                    .stream()
                    .filter(TypeResolver::acceptMethod)
                    .forEach(method -> scanMethod(context, properties, method, stack, reference, descendants));

            JandexUtil.interfaces(index, currentClass)
                    .stream()
                    .filter(type -> !TypeUtil.knownJavaType(type.name()))
                    .map(index::getClass)
                    .filter(Objects::nonNull)
                    .flatMap(clazz -> methods(context, clazz).stream())
                    .forEach(method -> scanMethod(context, properties, method, stack, reference, descendants));

            descendants.add(currentClass);
        }

        if (!context.getConfig().privatePropertiesEnable()) {
            properties.values()
                    .stream()
                    .filter(TypeResolver::isExposedByDefault)
                    .filter(resolver -> isNonPublicOrAbsent(resolver.field))
                    .filter(resolver -> isNonPublicOrAbsent(resolver.readMethod))
                    .filter(resolver -> isNonPublicOrAbsent(resolver.writeMethod))
                    .forEach(property -> property.ignored = true);
        }

        return sorted(properties, chain.keySet());
    }

    private static List<MethodInfo> methods(AnnotationScannerContext context, ClassInfo currentClass) {
        if (context.getConfig().sortedPropertiesEnable()) {
            return currentClass.methods();
        }
        return currentClass.unsortedMethods();
    }

    private static boolean acceptMethod(MethodInfo method) {
        if (Modifier.isStatic(method.flags())) {
            return false;
        }
        String name = method.name();
        // Ignore constructors
        return !(name.equals("<init>") || name.equals("getClass"));
    }

    private static boolean acceptField(FieldInfo field) {
        return !Modifier.isStatic(field.flags()) && !field.isSynthetic();
    }

    private static boolean isNonPublicOrAbsent(FieldInfo field) {
        return field == null || !Modifier.isPublic(field.flags());
    }

    private static boolean isNonPublicOrAbsent(MethodInfo method) {
        return method == null || !Modifier.isPublic(method.flags());
    }

    /**
     * Determine if the target should be exposed in the API or ignored. Explicitly un-hiding a property
     * via the <code>@Schema</code> annotation overrides configuration to ignore the same property
     * higher up the class/interface hierarchy.
     * 
     * @param target the field or method to be checked for ignoring or exposure in the API
     * @param reference an annotated member (field or method) that referenced the type of target's declaring class
     * @param descendants list of classes that descend from the class containing target
     * @param ignoreResolver resolver to determine if the field is ignored
     */
    private void processVisibility(AnnotationTarget target, AnnotationTarget reference, List<ClassInfo> descendants,
            IgnoreResolver ignoreResolver) {
        if (this.exposed || this.ignored) {
            // @Schema with hidden = false OR ignored somehow by a member lower in the class hierarchy
            return;
        }

        if (this.isUnhidden(target)) {
            // @Schema with hidden = false and not already ignored by a member lower in the class hierarchy
            this.exposed = true;
            return;
        }

        switch (getVisibility(target, reference, descendants, ignoreResolver)) {
            case EXPOSED:
                this.exposed = true;
                break;
            case IGNORED:
                if (target.kind() == Kind.METHOD) {
                    if (isAccessor(target.asMethod())) {
                        this.writeOnly = true;
                    } else {
                        this.readOnly = true;
                    }

                    if (this.readOnly && this.writeOnly) {
                        this.ignored = true;
                    }
                } else {
                    this.ignored = true;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Retrieve any property visibility configured on the target or overridden by descendant classes.
     * 
     * @param target the field or method to be checked for ignoring or exposure in the API
     * @param reference an annotated member (field or method) that referenced the type of target's declaring class
     * @param descendants list of classes that descend from the class containing target
     * @param ignoreResolver resolver to determine if the field is ignored
     */
    private IgnoreResolver.Visibility getVisibility(AnnotationTarget target, AnnotationTarget reference,
            List<ClassInfo> descendants,
            IgnoreResolver ignoreResolver) {
        // First check if a descendant class has hidden/exposed the property
        IgnoreResolver.Visibility visibility = ignoreResolver.getDescendantVisibility(propertyName, descendants);

        if (visibility == IgnoreResolver.Visibility.UNSET) {
            visibility = ignoreResolver.isIgnore(target, reference);
        }

        return visibility;
    }

    /**
     * Determines whether the target is set to hidden = false via the <code>@Schema</code>
     * annotation (explicit or default value).
     * 
     * @return true if the field is un-hidden, false otherwise
     */
    boolean isUnhidden(AnnotationTarget target) {
        if (target != null) {
            AnnotationInstance schemaAnnotation = TypeUtil.getSchemaAnnotation(target);

            if (schemaAnnotation != null) {
                Boolean hidden = JandexUtil.value(schemaAnnotation, SchemaConstant.PROP_HIDDEN);

                if (hidden == null || !hidden.booleanValue()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines if a field is a bean property. This is the case if (1) no field having the same name
     * has yet be scanned earlier (lower) in the inheritance chain or (2) if getter/setter methods were
     * previously found but no field has yet been found. If case (2), the field must be either public or
     * protected and it is assumed that the getter/setter methods scanned lower in the inheritance chain
     * operate on the field which is in a super class.
     *
     * @param context current scanner context
     * @param properties current map of properties discovered
     * @param field the field to scan
     * @param stack type resolution stack for parameterized types
     * @param reference an annotated member (field or method) that referenced the type of field's declaring class
     * @param descendants list of classes that descend from the class containing field
     */
    private static void scanField(AnnotationScannerContext context, Map<String, TypeResolver> properties, FieldInfo field,
            Deque<Map<String, Type>> stack,
            AnnotationTarget reference,
            List<ClassInfo> descendants) {
        final String propertyName = field.name();
        final Type fieldType = field.type();
        final ClassInfo fieldClass = context.getAugmentedIndex().getClass(fieldType);
        final boolean unwrapped;
        final TypeResolver resolver;

        if (field.hasAnnotation(JacksonConstants.JSON_UNWRAPPED) && fieldClass != null) {
            unwrapped = true;
            properties.putAll(unwrapProperties(context, field, fieldType, fieldClass));
        } else {
            unwrapped = false;
        }

        // Consider only using fields that are public?
        if (properties.containsKey(propertyName)) {
            resolver = properties.get(propertyName);

            if (resolver.getField() == null && (Modifier.isPublic(field.flags()) || Modifier.isProtected(field.flags()))) {
                /*
                 * Field is declared in parent class and the getter/setter was
                 * declared/overridden in child class
                 */
                resolver.setField(field);
            }
        } else {
            resolver = new TypeResolver(getPropertyNameTranslator(context, field), propertyName, field,
                    new ArrayDeque<>(stack));
            properties.put(propertyName, resolver);
        }

        if (BeanValidationScanner.hasConstraints(field)) {
            resolver.constraintTargets.add(field);
        }

        if (unwrapped) {
            // Ignored for getters/setters
            resolver.ignored = true;
        } else {
            resolver.processVisibility(field, reference, descendants, context.getIgnoreResolver());
        }
    }

    private static Map<String, TypeResolver> unwrapProperties(AnnotationScannerContext context,
            AnnotationTarget member,
            Type memberType,
            ClassInfo memberClass) {

        Map<String, TypeResolver> unwrappedProperties = getAllFields(context, memberType, memberClass, member);
        AnnotationInstance jsonUnwrapped = TypeUtil.getAnnotation(member, JacksonConstants.JSON_UNWRAPPED);
        String unwrapPrefix = JandexUtil.value(jsonUnwrapped, "prefix");
        String unwrapSuffix = JandexUtil.value(jsonUnwrapped, "suffix");

        return unwrappedProperties.entrySet()
                .stream()
                .map(p -> applyPrefixSuffix(p, unwrapPrefix, unwrapSuffix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static Map.Entry<String, TypeResolver> applyPrefixSuffix(Map.Entry<String, TypeResolver> property, String prefix,
            String suffix) {
        TypeResolver unwrappedResolver = property.getValue();
        StringBuilder key = new StringBuilder();

        if (prefix != null) {
            unwrappedResolver.propertyNamePrefix = prefix;
            key.append(prefix);
        }

        key.append(property.getKey());

        if (suffix != null) {
            unwrappedResolver.propertyNameSuffix = suffix;
            key.append(suffix);
        }

        return new SimpleEntry<>(key.toString(), unwrappedResolver);
    }

    /**
     * Determines if a method is a bean property method. The method must conform to the Java bean
     * conventions for getter or setter methods.
     *
     * @param context current scanner context
     * @param properties current map of properties discovered
     * @param method the method to scan
     * @param stack type resolution stack for parameterized types
     * @param reference an annotated member (field or method) that referenced the type of method's declaring class
     * @param descendants list of classes that descend from the class containing field
     */
    private static void scanMethod(AnnotationScannerContext context, Map<String, TypeResolver> properties, MethodInfo method,
            Deque<Map<String, Type>> stack,
            AnnotationTarget reference,
            List<ClassInfo> descendants) {
        Type returnType = method.returnType();
        Type propertyType = null;

        if (isAccessor(method)) {
            propertyType = returnType;
        } else if (isMutator(method)) {
            propertyType = method.parameterType(0);
        }

        if (propertyType != null) {
            TypeResolver resolver = updateTypeResolvers(context, properties, stack, method, propertyType);
            if (resolver != null) {
                resolver.processVisibility(method, reference, descendants, context.getIgnoreResolver());
            }
        }
    }

    /**
     * Adds (or updates) a TypeResolver with the method if (1) the method represents
     * a property not already in the properties map or (2) if the method has the same
     * type as an existing property having the same name and the new method has a
     * higher priority than the current method of the same type (getter or setter).
     *
     * @param context current scanner context
     * @param properties current map of properties discovered
     * @param stack type resolution stack for parameterized types
     * @param method the method to add/update in properties
     * @param propertyType the type of the property associated with the method
     */
    private static TypeResolver updateTypeResolvers(AnnotationScannerContext context,
            Map<String, TypeResolver> properties,
            Deque<Map<String, Type>> stack,
            MethodInfo method,
            Type propertyType) {

        final String propertyName = propertyName(method);

        if (propertyName == null) {
            return null;
        }

        TypeResolver resolver;

        if (properties.containsKey(propertyName)) {
            resolver = properties.get(propertyName);

            // Only store the accessor/mutator methods if the type of property matches
            if (!TypeUtil.equalTypes(resolver.getUnresolvedType(), propertyType)) {
                return resolver;
            }
        } else {
            resolver = new TypeResolver(getPropertyNameTranslator(context, method), propertyName, null,
                    new ArrayDeque<>(stack));
            properties.put(propertyName, resolver);
        }

        final boolean isWriteMethod = isMutator(method);

        if (isWriteMethod) {
            if (isHigherPriority(method, resolver.getWriteMethod())) {
                resolver.setWriteMethod(method);
            }
        } else {
            if (isHigherPriority(method, resolver.getReadMethod())) {
                resolver.setReadMethod(method);
            }
        }

        if (BeanValidationScanner.hasConstraints(method)) {
            resolver.constraintTargets.add(method);
        }

        return resolver;
    }

    /**
     * Derive the name of the property using the name of the given method. The method
     * is assumed to be a "getter" (also supporting accessors for boolean values starting
     * with "is") or a "setter".
     * 
     * @param method either an accessor (getter) or mutator (setter) for the property
     * @return the property name
     */
    static String propertyName(MethodInfo method) {
        final String methodName = method.name();
        final int nameStart = methodNamePrefix(method).length();
        final String propertyName;

        if (methodName.length() == nameStart) {
            // The method's name is "get", "set", or "is" without the property name
            return null;
        }

        if (nameStart > 0) {
            StringBuilder nameBuffer = new StringBuilder(methodName.length());
            nameBuffer.append(methodName);
            nameBuffer.setCharAt(nameStart, Character.toLowerCase(methodName.charAt(nameStart)));

            propertyName = nameBuffer.substring(nameStart);
        } else {
            propertyName = methodName;
        }

        return propertyName;
    }

    /**
     * Returns whether a method follows the Java bean convention for an accessor
     * method (getter). The method name typically begins with "get", but may also
     * begin with "is" when the return type is boolean.
     *
     * @param method the method to check
     * @return true if the method is a Java bean getter, otherwise false
     */
    private static boolean isAccessor(MethodInfo method) {
        if (!JandexUtil.isSupplier(method)) {
            return false;
        }

        String namePrefix = methodNamePrefix(method);

        if (METHOD_PREFIX_GET.equals(namePrefix)) {
            return true;
        }

        if (METHOD_PREFIX_IS.equals(namePrefix) && TypeUtil.equalTypes(method.returnType(), BOOLEAN_TYPE)) {
            return true;
        }

        return TypeUtil.hasAnnotation(method, SchemaConstant.DOTNAME_SCHEMA);
    }

    /**
     * Returns whether a method follows the Java bean convention for a mutator
     * method (setter).
     *
     * @param method the method to check
     * @return true if the method is a Java bean setter, otherwise false
     */
    private static boolean isMutator(MethodInfo method) {
        Type returnType = method.returnType();

        if (method.parametersCount() != 1 || !Type.Kind.VOID.equals(returnType.kind())) {
            return false;
        }

        return METHOD_PREFIX_SET.equals(methodNamePrefix(method))
                || TypeUtil.hasAnnotation(method, SchemaConstant.DOTNAME_SCHEMA);
    }

    private static String methodNamePrefix(MethodInfo method) {
        String methodName = method.name();

        if (methodName.startsWith(METHOD_PREFIX_GET)) {
            return METHOD_PREFIX_GET;
        }

        if (methodName.startsWith(METHOD_PREFIX_IS)) {
            return METHOD_PREFIX_IS;
        }

        if (methodName.startsWith(METHOD_PREFIX_SET)) {
            return METHOD_PREFIX_SET;
        }

        return "";
    }

    private static boolean isHigherPriority(MethodInfo newMethod, MethodInfo oldMethod) {
        if (oldMethod == null) {
            return true;
        }

        if (Modifier.isInterface(newMethod.declaringClass().flags())) {
            return targetComparator.compare(newMethod, oldMethod) < 0;
        }

        return false;
    }

    private static UnaryOperator<String> getPropertyNameTranslator(AnnotationScannerContext context, AnnotationTarget target) {
        ClassInfo clazz = target.kind() == Kind.CLASS ? target.asClass() : TypeUtil.getDeclaringClass(target);
        AnnotationInstance jacksonNaming = clazz.classAnnotation(JacksonConstants.JSON_NAMING);
        UnaryOperator<String> translator;

        if (jacksonNaming != null) {
            Type namingClass = JandexUtil.value(jacksonNaming, JacksonConstants.PROP_VALUE);

            if (namingClass != null) {
                translator = PropertyNamingStrategyFactory.getStrategy(namingClass.name().toString(), context.getClassLoader());
            } else {
                // Per Jackson @JsonNaming JavaDoc
                translator = PropertyNamingStrategyFactory.getStrategy(JsonbConstants.IDENTITY, context.getClassLoader());
            }
        } else {
            translator = context.getPropertyNameTranslator();
        }

        return translator;
    }

    /**
     * Orders the properties map based on annotations declared on classes in the
     * ancestry chain or based on where in the class hierarchy the property is declared.
     *
     * Properties will be order as follows:
     * <ol>
     * <li>Properties with an order specified in a super class (highest first)
     * <li>Properties with an order specified in the child class
     * <li>Properties declared in a super class (highest first)
     * <li>Properties declared in the child class
     * </ol>
     *
     * @param properties current map of properties discovered
     * @param chainKeys inheritance chain, child classes first
     * @return ordered map of properties
     */
    private static Map<String, TypeResolver> sorted(Map<String, TypeResolver> properties, Set<ClassInfo> chainKeys) {
        List<ClassInfo> chain = new ArrayList<>(chainKeys);
        Collections.reverse(chain);
        List<String> order = chain.stream()
                .map(TypeResolver::propertyOrder)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        List<DotName> chainClassNames = chain.stream().map(ClassInfo::name).collect(Collectors.toList());

        return properties.entrySet()
                .stream()
                .sorted((e1, e2) -> {
                    TypeResolver r1 = e1.getValue();
                    TypeResolver r2 = e2.getValue();
                    ClassInfo c1 = r1.getDeclaringClass();
                    ClassInfo c2 = r2.getDeclaringClass();

                    int pIndex1 = order.indexOf(r1.getPropertyName());
                    if (pIndex1 < 0) {
                        // The order was specified by the original property name, not the customized name (or not at all)
                        pIndex1 = order.indexOf(e1.getKey());
                    }

                    int pIndex2 = order.indexOf(r2.getPropertyName());
                    if (pIndex2 < 0) {
                        // The order was specified by the original property name, not the customized name (or not at all)
                        pIndex2 = order.indexOf(e2.getKey());
                    }

                    if (pIndex1 > -1) {
                        if (pIndex2 < 0) {
                            return -1;
                        }
                        return Integer.compare(pIndex1, pIndex2);
                    }

                    if (pIndex2 > -1) {
                        return 1;
                    }

                    int cIndex1 = chainClassNames.indexOf(c1.name());
                    int cIndex2 = chainClassNames.indexOf(c2.name());

                    return Integer.compare(cIndex1, cIndex2);
                })
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    /**
     * Retrieves the property order from annotations declare on the class, if available.
     *
     * @param clazz the class to check for property ordering
     * @return a list of property names, in the order declared, or an empty list if none
     */
    private static List<String> propertyOrder(ClassInfo clazz) {
        AnnotationInstance propertyOrder;
        AnnotationValue orderArray = null;

        if ((propertyOrder = JandexUtil.getClassAnnotation(clazz, JsonbConstants.JSONB_PROPERTY_ORDER)) != null) {
            orderArray = propertyOrder.value();
        } else if ((propertyOrder = JandexUtil.getClassAnnotation(clazz, JaxbConstants.XML_TYPE)) != null) {
            orderArray = propertyOrder.value("propOrder");
        } else if ((propertyOrder = clazz.classAnnotation(JacksonConstants.JSON_PROPERTY_ORDER)) != null) {
            orderArray = propertyOrder.value();
        }

        if (orderArray != null) {
            return Arrays.asList(orderArray.asStringArray());
        }

        return Collections.emptyList();
    }

    private static Map<String, Type> buildParamTypeResolutionMap(ClassInfo klazz, ParameterizedType parameterizedType) {
        List<TypeVariable> typeVariables = klazz.typeParameters();
        List<Type> arguments = parameterizedType.arguments();

        if (arguments.size() != typeVariables.size()) {
            DataObjectLogging.logger.classNotAvailable(typeVariables, arguments);
        }

        Map<String, Type> resolutionMap = new LinkedHashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            TypeVariable typeVar = typeVariables.get(i);
            Type arg = arguments.get(i);
            resolutionMap.put(typeVar.identifier(), arg);
        }

        return resolutionMap;
    }

    public static ParameterizedType resolveParameterizedAncestor(AnnotationScannerContext context, ParameterizedType pType,
            Type seekType) {
        ParameterizedType cursor = pType;
        boolean seekContinue = true;

        while (context.getAugmentedIndex().containsClass(cursor) && seekContinue) {
            ClassInfo cursorClass = context.getIndex().getClassByName(cursor.name());
            Map<String, Type> resolutionMap = buildParamTypeResolutionMap(cursorClass, cursor);
            List<Type> interfaces = getInterfacesOfType(context, cursorClass, seekType);

            for (Type implementedType : interfaces) {
                // Follow interface hierarchy toward `seekType` instead of parent class
                cursor = createParameterizedType(implementedType, resolutionMap);

                if (implementedType.name().equals(seekType.name())) {
                    // The searched-for type is implemented directly
                    seekContinue = false;
                    break;
                }
            }

            if (interfaces.isEmpty()) {
                Type superType = cursorClass.superClassType();

                if (TypeUtil.isA(context, superType, seekType)) {
                    cursor = createParameterizedType(superType, resolutionMap);
                } else {
                    seekContinue = false;
                }
            }
        }

        return cursor;
    }

    private static List<Type> getInterfacesOfType(AnnotationScannerContext context, ClassInfo clazz, Type seekType) {
        return clazz.interfaceTypes().stream().filter(t -> TypeUtil.isA(context, t, seekType)).collect(Collectors.toList());
    }

    private static ParameterizedType createParameterizedType(Type targetType, Map<String, Type> resolutionMap) {
        if (targetType.kind() != Type.Kind.PARAMETERIZED_TYPE) {
            return ParameterizedType.create(targetType.name(), Type.EMPTY_ARRAY, null);
        }

        Type[] resolvedArgs = resolveArguments(targetType.asParameterizedType(), t -> resolveType(t, resolutionMap));
        return ParameterizedType.create(targetType.name(), resolvedArgs, null);
    }

    private static Type resolveType(Type type, Map<String, Type> resolutionMap) {
        switch (type.kind()) {
            case PARAMETERIZED_TYPE:
                return createParameterizedType(type, resolutionMap);
            case TYPE_VARIABLE:
                String id = type.asTypeVariable().identifier();
                return resolutionMap.getOrDefault(id, type);
            default:
                return type;
        }
    }
}
