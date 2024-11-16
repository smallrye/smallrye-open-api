package io.smallrye.openapi.spring;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

class SpringSupport {

    private SpringSupport() {
    }

    static Set<PathItem.HttpMethod> getHttpMethods(MethodInfo methodInfo) {
        Set<PathItem.HttpMethod> methods = new LinkedHashSet<>();

        // Try @XXXMapping annotations
        for (DotName validMethodAnnotations : SpringConstants.HTTP_METHODS) {
            if (methodInfo.hasAnnotation(validMethodAnnotations)) {
                String toHttpMethod = toHttpMethod(validMethodAnnotations);
                methods.add(PathItem.HttpMethod.valueOf(toHttpMethod));
            }
        }

        // Try @RequestMapping
        if (methodInfo.hasAnnotation(SpringConstants.REQUEST_MAPPING)) {
            AnnotationInstance requestMappingAnnotation = methodInfo.annotation(SpringConstants.REQUEST_MAPPING);
            AnnotationValue methodValue = requestMappingAnnotation.value("method");

            if (methodValue != null) {
                String[] enumArray = methodValue.asEnumArray();
                for (String enumValue : enumArray) {
                    if (enumValue != null) {
                        methods.add(PathItem.HttpMethod.valueOf(enumValue.toUpperCase()));
                    }
                }
            } else {
                // Default ?
            }
        }

        return methods;
    }

    private static String toHttpMethod(DotName dotname) {
        String className = dotname.withoutPackagePrefix();
        className = className.replace("Mapping", "");
        return className.toUpperCase();
    }
}
