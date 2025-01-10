package io.smallrye.openapi.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.dataobject.TypeResolver;
import io.smallrye.openapi.runtime.scanner.spi.AbstractParameterProcessor;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.scanner.spi.FrameworkParameter;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Copied from JAX-RS.
 * This still needs work. As we add test cases we will clean this up
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SpringParameterProcessor extends AbstractParameterProcessor {

    /**
     * Sonar validation is disabled on this expression because there is no danger of denial of
     * service attacks input derived from the developer of the host application.
     */
    static final Pattern TEMPLATE_PARAM_PATTERN = Pattern
            .compile("\\{[ \\t]*(\\w[\\w\\.-]*)[ \\t]*:[ \\t]*((?:[^{}]|\\{[^{}]+\\})+)\\}"); //NOSONAR

    private SpringParameterProcessor(AnnotationScannerContext scannerContext,
            String contextPath,
            Function<AnnotationInstance, Parameter> reader) {
        super(scannerContext, contextPath, reader, scannerContext.getExtensions());
    }

    /**
     * Process parameter annotations for the given class and method.This method operates
     * in two phases. First, class-level parameters are processed and saved in the
     * {@link ResourceParameters}. Second, method-level parameters are processed. Form parameters
     * are only applicable to the method-level in this component.
     *
     * @param context the AnnotationScannerContext
     * @param contextPath context path for the resource class and method
     * @param resourceClass the class info
     * @param resourceMethod the Spring resource method, annotated with one of the
     *        Spring HTTP annotations
     * @param reader callback method for a function producing {@link Parameter} from a
     *        {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter}
     * @param extensions scanner extensions
     * @return scanned parameters and modified path contained in a {@link ResourceParameters}
     *         object
     */
    public static ResourceParameters process(AnnotationScannerContext context,
            String contextPath,
            ClassInfo resourceClass,
            MethodInfo resourceMethod,
            Function<AnnotationInstance, Parameter> reader) {

        SpringParameterProcessor processor = new SpringParameterProcessor(context, contextPath, reader);
        return processor.process(resourceClass, resourceMethod);
    }

    @Override
    protected Pattern getTemplateParameterPattern() {
        return TEMPLATE_PARAM_PATTERN;
    }

    @Override
    protected FrameworkParameter getMatrixParameter() {
        return SpringParameter.MATRIX_PARAM.parameter;
    }

    @Override
    protected void readAnnotatedType(AnnotationInstance annotation, AnnotationInstance beanParamAnnotation,
            boolean overriddenParametersOnly) {
        DotName name = annotation.name();

        if (isReadableParameterAnnotation(name)) {
            readParameterAnnotation(annotation, overriddenParametersOnly);
        } else {
            FrameworkParameter frameworkParam = SpringParameter.forName(name);

            if (frameworkParam != null) {
                readSpringParameter(frameworkParam, annotation, beanParamAnnotation, overriddenParametersOnly);
            }
        }
    }

    private void readSpringParameter(FrameworkParameter frameworkParam,
            AnnotationInstance annotation,
            AnnotationInstance beanParamAnnotation,
            boolean overriddenParametersOnly) {
        AnnotationTarget target = annotation.target();
        Type targetType = getType(target);

        if (frameworkParam.style == Style.FORM) {
            // Store the @FormParam for later processing
            formParams.put(paramName(annotation), annotation);
            readFrameworkParameter(annotation, frameworkParam, overriddenParametersOnly);
        } else if (frameworkParam.style == Style.MATRIX) {
            // Store the @MatrixParam for later processing
            List<String> pathSegments = beanParamAnnotation != null
                    ? lastPathSegmentsOf(beanParamAnnotation.target())
                    : lastPathSegmentsOf(target);

            for (String pathSegment : pathSegments) {
                matrixParams
                        .computeIfAbsent(pathSegment, k -> new HashMap<>())
                        .put(paramName(annotation), annotation);
            }
        } else if (frameworkParam.location != null) {
            readFrameworkParameter(annotation, frameworkParam, overriddenParametersOnly);
        } else if (target != null && annotatesHttpGET(target)) {
            // This is a SpringDoc @ParameterObject
            setMediaType(frameworkParam);
            targetType = TypeUtil.unwrapType(targetType);

            if (targetType != null) {
                ClassInfo beanParam = index.getClassByName(targetType.name());

                /*
                 * Since the properties of the bean are probably not annotated (supported in Spring),
                 * here we process them with a generated Spring @RequestParam annotation attached.
                 */
                for (var entry : TypeResolver.getAllFields(scannerContext, targetType, beanParam, null).entrySet()) {
                    var syntheticQuery = AnnotationInstance.builder(SpringConstants.QUERY_PARAM)
                            .buildWithTarget(entry.getValue().getAnnotationTarget());
                    readAnnotatedType(syntheticQuery, beanParamAnnotation, overriddenParametersOnly);
                }
            }
        }
    }

    static boolean annotatesHttpGET(AnnotationTarget target) {
        MethodInfo resourceMethod = targetMethod(target);

        if (resourceMethod != null) {
            return SpringSupport.getHttpMethods(resourceMethod).contains(PathItem.HttpMethod.GET);
        }

        return false;
    }

    @Override
    protected Set<DotName> getDefaultAnnotationNames() {
        return Collections.singleton(SpringConstants.QUERY_PARAM);
    }

    @Override
    protected String getDefaultAnnotationProperty() {
        return "defaultValue";
    }

    @Override
    protected List<String> pathsOf(AnnotationTarget target) {
        AnnotationInstance path = null;
        Set<DotName> paths = SpringConstants.HTTP_METHODS;

        for (DotName possiblePath : paths) {
            AnnotationInstance annotation = scannerContext.annotations()
                    .getAnnotation(target, possiblePath);

            if (mappingHasPath(annotation)) {
                path = annotation;
            }
        }

        // Also support @RequestMapping
        AnnotationInstance annotation = scannerContext.annotations()
                .getAnnotation(target, SpringConstants.REQUEST_MAPPING);

        if (mappingHasPath(annotation)) {
            path = annotation;
        }

        if (path != null) {
            List<String> pathValues = requestMappingValuesToPath(path);

            return pathValues.stream()
                    .map(pathValue -> {
                        if (pathValue.startsWith("/")) {
                            pathValue = pathValue.substring(1);
                        }

                        if (pathValue.endsWith("/")) {
                            pathValue = pathValue.substring(0, pathValue.length() - 1);
                        }

                        return pathValue;
                    })
                    .collect(Collectors.toList());
        }

        return List.of("");
    }

    static boolean mappingHasPath(AnnotationInstance mappingAnnotation) {
        return mappingAnnotation != null && (mappingAnnotation.value() != null || mappingAnnotation.value("path") != null);
    }

    /**
     * Creates a String path from the RequestMapping value
     *
     * @param requestMappingAnnotation
     * @return
     */
    static List<String> requestMappingValuesToPath(AnnotationInstance requestMappingAnnotation) {
        AnnotationValue value = getRequestMappingPathAnnotation(requestMappingAnnotation);
        if (value == null) {
            return Collections.emptyList();
        }
        return List.of(value.asStringArray());
    }

    static AnnotationValue getRequestMappingPathAnnotation(AnnotationInstance requestMappingAnnotation) {
        AnnotationValue value = requestMappingAnnotation.value();
        if (value != null) {
            return value;
        } else {
            return requestMappingAnnotation.value("path");
        }
    }

    @Override
    protected boolean isSubResourceLocator(MethodInfo method) {
        return method.returnType().kind() == Type.Kind.CLASS &&
                isResourceMethod(method) &&
                method.annotations().stream()
                        .map(AnnotationInstance::name)
                        .noneMatch(SpringConstants.HTTP_METHODS::contains);
    }

    @Override
    protected boolean isResourceMethod(MethodInfo method) {
        return method.annotations()
                .stream()
                .map(AnnotationInstance::name)
                .anyMatch(SpringConstants.HTTP_METHODS::contains);
    }

    @Override
    protected boolean isParameter(DotName annotationName) {
        if (SpringParameter.isParameter(annotationName)) {
            return true;
        }
        if (Names.PARAMETER.equals(annotationName)) {
            return true;
        }
        return Names.PARAMETERS.equals(annotationName);
    }
}
