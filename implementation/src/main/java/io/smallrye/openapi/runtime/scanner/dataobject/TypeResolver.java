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

import java.lang.reflect.Modifier;
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
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.JacksonConstants;
import io.smallrye.openapi.api.constants.JaxbConstants;
import io.smallrye.openapi.api.constants.JsonbConstants;
import io.smallrye.openapi.api.constants.MPOpenApiConstants;
import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class TypeResolver {

    private static final Logger LOG = Logger.getLogger(TypeResolver.class);
    private static final Type BOOLEAN_TYPE = Type.create(DotName.createSimple(Boolean.class.getName()), Type.Kind.CLASS);

    private final Deque<Map<String, Type>> resolutionStack;
    private final String propertyName;
    private FieldInfo field;
    private MethodInfo readMethod;
    private MethodInfo writeMethod;
    private Type leaf;

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
    private static Comparator<AnnotationTarget> targetComparator = (t1, t2) -> {
        int result;

        // Annotated elements sort to the top of the priority queue
        if ((result = compareAnnotation(t1, t2, MPOpenApiConstants.SCHEMA)) != 0) {
            return result;
        }
        if ((result = compareAnnotation(t1, t2, JsonbConstants.JSONB_PROPERTY)) != 0) {
            return result;
        }
        if ((result = compareAnnotation(t1, t2, JacksonConstants.JSON_PROPERTY)) != 0) {
            return result;
        }
        if ((result = compareAnnotation(t1, t2, JaxbConstants.XML_ELEMENT)) != 0) {
            return result;
        }
        if ((result = compareAnnotation(t1, t2, JaxbConstants.XML_ATTRIBUTE)) != 0) {
            return result;
        }
        if (t1.kind() == Kind.FIELD) {
            return -1;
        }
        if (t2.kind() == Kind.FIELD) {
            return +1;
        }
        if (t1.asMethod().name().startsWith("get") && !t2.asMethod().name().startsWith("get")) {
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

    private TypeResolver(String propertyName, FieldInfo field, Deque<Map<String, Type>> resolutionStack) {
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
                MPOpenApiConstants.SCHEMA,
                OpenApiConstants.PROP_NAME)) != null) {
            return name;
        }

        if ((name = TypeUtil.getAnnotationValue(target,
                JsonbConstants.JSONB_PROPERTY,
                OpenApiConstants.PROP_VALUE)) != null) {
            return name;
        }

        if ((name = TypeUtil.getAnnotationValue(target,
                JacksonConstants.JSON_PROPERTY,
                OpenApiConstants.PROP_VALUE)) != null) {
            return name;
        }

        if ((name = TypeUtil.getAnnotationValue(target,
                JaxbConstants.XML_ELEMENT,
                OpenApiConstants.PROP_NAME)) != null) {
            return name;
        }

        if ((name = TypeUtil.getAnnotationValue(target,
                JaxbConstants.XML_ATTRIBUTE,
                OpenApiConstants.PROP_NAME)) != null) {
            return name;
        }

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
            this.leaf = writeMethod.parameters().get(0);
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

    /**
     * Resolve a type against this {@link TypeResolver}'s resolution stack
     *
     * @param fieldType type to resolve
     * @return resolved type (if found)
     */
    public Type getResolvedType(Type fieldType) {
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

    public static Map<String, TypeResolver> getAllFields(AugmentedIndexView index, Type leaf, ClassInfo leafKlazz) {
        Map<ClassInfo, Type> chain = JandexUtil.inheritanceChain(index, leafKlazz, leaf);
        Map<String, TypeResolver> properties = new LinkedHashMap<>();
        Deque<Map<String, Type>> stack = new ArrayDeque<>();

        for (Map.Entry<ClassInfo, Type> entry : chain.entrySet()) {
            ClassInfo currentClass = entry.getKey();
            Type currentType = entry.getValue();

            if (currentType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                Map<String, Type> resMap = buildParamTypeResolutionMap(currentClass, currentType.asParameterizedType());
                stack.push(resMap);
            }

            // Store all field properties
            currentClass.fields()
                    .stream()
                    .filter(field -> acceptField(field))
                    .forEach(field -> scanField(properties, field, stack));

            currentClass.methods()
                    .stream()
                    .filter(method -> acceptMethod(method))
                    .forEach(method -> scanMethod(properties, method, stack));

            currentClass.interfaceTypes()
                    .stream()
                    .map(index::getClass)
                    .filter(Objects::nonNull)
                    .flatMap(clazz -> clazz.methods().stream())
                    .forEach(method -> scanMethod(properties, method, stack));
        }

        return sorted(properties, chain.keySet());
    }

    private static boolean acceptMethod(MethodInfo method) {
        return !Modifier.isStatic(method.flags()) && !method.name().equals("getClass");
    }

    private static boolean acceptField(FieldInfo field) {
        return !Modifier.isStatic(field.flags());
    }

    /**
     * Determines if a field is a bean property. This is the case if (1) no field having the same name
     * has yet be scanned earlier (lower) in the inheritance chain or (2) if getter/setter methods were
     * previously found but no field has yet been found. If case (2), the field must be either public or
     * protected and it is assumed that the getter/setter methods scanned lower in the inheritance chain
     * operate on the field which is in a super class.
     *
     * @param properties current map of properties discovered
     * @param field the field to scan
     * @param stack type resolution stack for parameterized types
     */
    private static void scanField(Map<String, TypeResolver> properties, FieldInfo field, Deque<Map<String, Type>> stack) {
        String propertyName = field.name();

        // Consider only using fields that are public?
        if (properties.containsKey(propertyName)) {
            TypeResolver resolver = properties.get(propertyName);

            if (resolver.getField() == null && (Modifier.isPublic(field.flags()) || Modifier.isProtected(field.flags()))) {
                /*
                 * Field is declared in parent class and the getter/setter was
                 * declared/overridden in child class
                 */
                resolver.setField(field);
            }
        } else {
            TypeResolver resolver = new TypeResolver(propertyName, field, new ArrayDeque<>(stack));
            properties.put(propertyName, resolver);
        }
    }

    /**
     * Determines if a method is a bean property method. The method must conform to the Java bean
     * conventions for getter or setter methods.
     *
     * @param properties current map of properties discovered
     * @param field the method to scan
     * @param stack type resolution stack for parameterized types
     */
    private static void scanMethod(Map<String, TypeResolver> properties, MethodInfo method, Deque<Map<String, Type>> stack) {
        Type returnType = method.returnType();
        Type propertyType = null;

        if (isAccessor(method)) {
            propertyType = returnType;
        } else if (isMutator(method)) {
            propertyType = method.parameters().get(0);
        }

        if (propertyType != null) {
            updateTypeResolvers(properties, stack, method, propertyType);
        }
    }

    /**
     * Adds (or updates) a TypeResolver with the method if (1) the method represents
     * a property not already in the properties map or (2) if the method has the same
     * type as an existing property having the same name and the new method has a
     * higher priority than the current method of the same type (getter or setter).
     *
     * @param properties current map of properties discovered
     * @param stack type resolution stack for parameterized types
     * @param method the method to add/update in properties
     * @param propertyType the type of the property associated with the method
     */
    private static void updateTypeResolvers(Map<String, TypeResolver> properties,
            Deque<Map<String, Type>> stack,
            MethodInfo method,
            Type propertyType) {
        String methodName = method.name();
        boolean isWriteMethod = isMutator(method);
        String propertyName;
        int nameStart;

        if (isWriteMethod) {
            nameStart = 3;
        } else {
            nameStart = methodName.startsWith("is") ? 2 : 3;
        }

        if (methodName.length() == nameStart) {
            // The method's name is "get", "set", or "is" without the property name
            return;
        }

        propertyName = Character.toLowerCase(methodName.charAt(nameStart)) + methodName.substring(nameStart + 1);
        TypeResolver resolver;

        if (properties.containsKey(propertyName)) {
            resolver = properties.get(propertyName);

            // Only store the accessor/mutator methods if the type of property matches
            if (!TypeUtil.equalTypes(resolver.getUnresolvedType(), propertyType)) {
                return;
            }
        } else {
            resolver = new TypeResolver(propertyName, null, new ArrayDeque<>(stack));
            properties.put(propertyName, resolver);
        }

        if (isWriteMethod) {
            if (isHigherPriority(method, resolver.getWriteMethod())) {
                resolver.setWriteMethod(method);
            }
        } else {
            if (isHigherPriority(method, resolver.getReadMethod())) {
                resolver.setReadMethod(method);
            }
        }
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
        Type returnType = method.returnType();

        if (!method.parameters().isEmpty() || Type.Kind.VOID.equals(returnType.kind())) {
            return false;
        }

        String methodName = method.name();

        if (methodName.startsWith("get")) {
            return true;
        }

        return methodName.startsWith("is") && TypeUtil.equalTypes(returnType, BOOLEAN_TYPE);
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

        if (method.parameters().size() != 1 || !Type.Kind.VOID.equals(returnType.kind())) {
            return false;
        }

        return method.name().startsWith("set");
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

                    int cIndex1 = chain.indexOf(c1);
                    int cIndex2 = chain.indexOf(c2);

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

        if ((propertyOrder = clazz.classAnnotation(JsonbConstants.JSONB_PROPERTY_ORDER)) != null) {
            orderArray = propertyOrder.value();
        } else if ((propertyOrder = clazz.classAnnotation(JaxbConstants.XML_TYPE)) != null) {
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
            LOG.errorv("Unanticipated mismatch between type arguments and type variables \n" +
                    "Args: {0}\n Vars:{1}", arguments, typeVariables);
        }

        Map<String, Type> resolutionMap = new LinkedHashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            TypeVariable typeVar = typeVariables.get(i);
            Type arg = arguments.get(i);
            resolutionMap.put(typeVar.identifier(), arg);
        }

        return resolutionMap;
    }

}
