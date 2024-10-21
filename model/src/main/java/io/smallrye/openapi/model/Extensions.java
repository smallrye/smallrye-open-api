package io.smallrye.openapi.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.Extensible;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.RequestBody;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;

public final class Extensions {

    public static final String PRIVATE_EXT_PREFIX = "x-smallrye-private-";
    private static final String EXT_PROFILE_PREFIX = "x-smallrye-profile-";

    private Extensions() {
        // No instances
    }

    private static Map<String, Object> extensions(Extensible<?> extensible) {
        Map<String, Object> extensions;

        if (extensible instanceof BaseExtensibleModel) {
            extensions = ((BaseExtensibleModel<?>) extensible).getAllExtensions();
        } else {
            extensions = extensible.getExtensions();
        }

        return extensions == null ? Collections.emptyMap() : extensions;
    }

    private static Set<String> extensionNames(Extensible<?> extensible) {
        return Set.copyOf(extensions(extensible).keySet());
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Extensible<?> extensible, String name, Class<T> type) {
        Object value;

        if (extensible instanceof BaseExtensibleModel) {
            value = ((BaseExtensibleModel<?>) extensible).getExtension(name);
        } else {
            value = extensions(extensible).get(name);
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        return null;
    }

    private static <T> void set(Extensible<?> extensible, String name, T value) {
        if (value != null) {
            extensible.addExtension(name, value);
        } else {
            extensible.removeExtension(name);
        }
    }

    public static Set<String> getProfiles(Extensible<?> extensible) {
        Set<String> profiles = new HashSet<>(2);

        for (String name : extensionNames(extensible)) {
            if (name.startsWith(EXT_PROFILE_PREFIX)) {
                profiles.add(name.substring(EXT_PROFILE_PREFIX.length()));
            }
        }

        return profiles;
    }

    public static void removeProfiles(Extensible<?> extensible) {
        for (String name : extensionNames(extensible)) {
            if (name.startsWith(EXT_PROFILE_PREFIX)) {
                extensible.removeExtension(name);
            }
        }
    }

    //////

    public static String getName(Extensible<?> extensible) {
        return get(extensible, PRIVATE_EXT_PREFIX + "name", String.class);
    }

    public static void setName(Extensible<?> extensible, String name) {
        set(extensible, PRIVATE_EXT_PREFIX + "name", name);
    }

    //////

    public static boolean isHidden(Extensible<?> extensible) {
        return Boolean.TRUE.equals(get(extensible, PRIVATE_EXT_PREFIX + "hidden", Boolean.class));
    }

    public static void setHidden(Extensible<?> extensible, Boolean hidden) {
        set(extensible, PRIVATE_EXT_PREFIX + "hidden", hidden);
    }

    //////

    public static String getResponseCode(APIResponse response) {
        return get(response, PRIVATE_EXT_PREFIX + "response-code", String.class);
    }

    public static void setResponseCode(APIResponse response, String responseCode) {
        set(response, PRIVATE_EXT_PREFIX + "response-code", responseCode);
    }

    //////

    public static Boolean getRequiredDefault(RequestBody requestBody) {
        return get(requestBody, PRIVATE_EXT_PREFIX + "required-default", Boolean.class);
    }

    /**
     * Sets the value to use for {@code required} if {@link #setRequired(Boolean)} has not been called.
     * <p>
     * If this method is called, {@link #getRequired()} will return this value unless {@link #setRequired(Boolean)} is called.
     *
     * @param requiredDefault the default value for {@code required}
     * @return this instance
     */
    public static void setRequiredDefault(RequestBody requestBody, Boolean requiredDefault) {
        set(requestBody, PRIVATE_EXT_PREFIX + "required-default", requiredDefault);
    }

    //////

    /**
     * Returns whether {@link #setRequired(Boolean)} has been called on a request body.
     *
     * @param requestBody the request body
     * @return {@code true} if {@code setRequired} has been called
     */
    public static boolean getIsRequiredSet(RequestBody requestBody) {
        return Boolean.TRUE.equals(get(requestBody, PRIVATE_EXT_PREFIX + "is-required-set", Boolean.class));
    }

    public static void setIsRequiredSet(RequestBody requestBody, Boolean requiredDefault) {
        set(requestBody, PRIVATE_EXT_PREFIX + "is-required-set", requiredDefault);
    }

    //////

    /**
     * Implementation specific, set a reference to the Java method parameter, so that we can bind back to it later if needed
     *
     * @return reference to the method parameter that we scanned this on
     */
    public static String getParamRef(Parameter parameter) {
        return get(parameter, PRIVATE_EXT_PREFIX + "param-ref", String.class);
    }

    public static void setParamRef(Parameter parameter, AnnotationTarget source) {
        String ref = createUniqueAnnotationTargetRef(source);
        set(parameter, PRIVATE_EXT_PREFIX + "param-ref", ref);
    }

    //////

    /**
     * Implementation specific, set a reference to the Java method, so that we can bind back to it later if needed
     *
     * @return reference to the method that we scanned this on
     */
    public static String getMethodRef(Operation operation) {
        return get(operation, PRIVATE_EXT_PREFIX + "method-ref", String.class);
    }

    public static void setMethodRef(Operation operation, ClassInfo resourceClass, MethodInfo method) {
        String ref = createUniqueMethodReference(resourceClass, method);
        set(operation, PRIVATE_EXT_PREFIX + "method-ref", ref);
    }

    //////

    @SuppressWarnings("unchecked")
    public static List<Schema> getTypeObservers(Schema schema) {
        return get(schema, PRIVATE_EXT_PREFIX + "schema-type-observers", List.class);
    }

    public static void setTypeObservers(Schema schema, List<Schema> observers) {
        set(schema, PRIVATE_EXT_PREFIX + "schema-type-observers", observers);
    }

    //////

    public static boolean isPrivateExtension(String name) {
        return name.startsWith(PRIVATE_EXT_PREFIX);
    }

    public static <E> E getPrivateExtension(Extensible<?> extensible, String name, Class<E> type) {
        return get(extensible, PRIVATE_EXT_PREFIX + name, type);
    }

    public static void setPrivateExtension(Extensible<?> extensible, String name, Object value) {
        extensible.addExtension(PRIVATE_EXT_PREFIX + name, value);
    }

    ///////

    private static String createUniqueAnnotationTargetRef(AnnotationTarget annotationTarget) {
        switch (annotationTarget.kind()) {
            case FIELD:
                return createUniqueFieldRef(annotationTarget.asField());
            case METHOD:
                ClassInfo classInfo = annotationTarget.asMethod().declaringClass();
                return createUniqueMethodReference(classInfo, annotationTarget.asMethod());
            case METHOD_PARAMETER:
                return createUniqueMethodParameterRef(annotationTarget.asMethodParameter());
            default:
                return null;
        }
    }

    private static String createUniqueFieldRef(FieldInfo fieldInfo) {
        ClassInfo classInfo = fieldInfo.declaringClass();
        return "f" + classInfo.hashCode() + "_" + fieldInfo.hashCode();
    }

    private static String createUniqueMethodReference(ClassInfo classInfo, MethodInfo methodInfo) {
        return "m" + classInfo.hashCode() + "_" + methodInfo.hashCode();
    }

    private static String createUniqueMethodParameterRef(MethodParameterInfo methodParameter) {
        final MethodInfo methodInfo = methodParameter.method();
        final ClassInfo classInfo = methodInfo.declaringClass();
        return "p" + classInfo.hashCode() + "_" + methodInfo.hashCode() + "_" + methodParameter.position();
    }

}
