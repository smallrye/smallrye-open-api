package io.smallrye.openapi.vertx;

import static org.jboss.jandex.AnnotationTarget.Kind.CLASS;
import static org.jboss.jandex.AnnotationTarget.Kind.METHOD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.io.Names;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.spi.AbstractParameterProcessor;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.scanner.spi.FrameworkParameter;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 * Copied from JAX-RS. Still need clean up
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class VertxParameterProcessor extends AbstractParameterProcessor {

    /**
     * Sonar validation is disabled on this expression because there is no danger of denial of
     * service attacks input derived from the developer of the host application.
     */
    static final Pattern TEMPLATE_PARAM_PATTERN = Pattern
            .compile(":[ \\t]*(\\w[\\w\\.-]*)[ \\t]*:[ \\t]*((?:[^{}]|\\{[^{}]+\\})+)"); //NOSONAR

    private VertxParameterProcessor(AnnotationScannerContext scannerContext,
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
     * @param resourceMethod the Vert.x resource method, annotated with one of the
     *        Vert.x HTTP annotations
     * @param reader callback method for a function producing {@link Parameter} from a
     *        {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter}
     * @return scanned parameters and modified path contained in a {@link ResourceParameters}
     *         object
     */
    public static ResourceParameters process(AnnotationScannerContext context,
            String contextPath,
            ClassInfo resourceClass,
            MethodInfo resourceMethod,
            Function<AnnotationInstance, Parameter> reader) {

        VertxParameterProcessor processor = new VertxParameterProcessor(context, contextPath, reader);
        return processor.process(resourceClass, resourceMethod);
    }

    @Override
    protected Pattern getTemplateParameterPattern() {
        return TEMPLATE_PARAM_PATTERN;
    }

    @Override
    protected FrameworkParameter getMatrixParameter() {
        return null;
    }

    @Override
    protected void readAnnotatedType(AnnotationInstance annotation, AnnotationInstance beanParamAnnotation,
            boolean overriddenParametersOnly) {

        DotName name = annotation.name();

        if (isReadableParameterAnnotation(name)) {
            readParameterAnnotation(annotation, overriddenParametersOnly);
        } else if (VertxConstants.PARAM.equals(name)) {
            // @Param only targets method parameters
            MethodInfo resourceMethod = annotation.target().asMethodParameter().method();
            String parameterName = paramName(annotation);
            String path = scannerContext.annotations().getAnnotationValue(
                    resourceMethod,
                    Collections.singletonList(VertxConstants.ROUTE),
                    "path",
                    resourceMethod.name());

            if (path.contains(":" + parameterName)) {
                FrameworkParameter vertxParameter = VertxParameter.PATH_PARAM.parameter;
                readAnnotatedType(vertxParameter, annotation, beanParamAnnotation, overriddenParametersOnly);
            } else {
                FrameworkParameter vertxParameter = VertxParameter.QUERY_PARAM.parameter;
                readAnnotatedType(vertxParameter, annotation, beanParamAnnotation, overriddenParametersOnly);
            }
        } else if (VertxConstants.HEADER_PARAM.equals(name)) {
            FrameworkParameter vertxParameter = VertxParameter.HEADER_PARAM.parameter;
            readAnnotatedType(vertxParameter, annotation, beanParamAnnotation, overriddenParametersOnly);
        }
    }

    private void readAnnotatedType(FrameworkParameter frameworkParam, AnnotationInstance annotation,
            AnnotationInstance beanParamAnnotation, boolean overriddenParametersOnly) {

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
            boolean isPathSegmentsEmpty = pathSegments == null || pathSegments.isEmpty();
            String pathSegment = !isPathSegmentsEmpty ? pathSegments.get(0) : null;

            matrixParams.computeIfAbsent(pathSegment, k -> new HashMap<>())
                    .put(paramName(annotation), annotation);
        } else if (frameworkParam.location != null) {
            readFrameworkParameter(annotation, frameworkParam, overriddenParametersOnly);
        } else if (target != null) {
            // This is a @BeanParam or a RESTEasy @MultipartForm
            setMediaType(frameworkParam);
            targetType = TypeUtil.unwrapType(targetType);

            if (targetType != null) {
                ClassInfo beanParam = index.getClassByName(targetType.name());
                readParameters(beanParam, annotation, overriddenParametersOnly);
            }
        }
    }

    @Override
    protected List<String> pathsOf(AnnotationTarget target) {
        String pathValue = null;

        if (target.kind().equals(CLASS)) {
            pathValue = scannerContext.annotations().getAnnotationValue(
                    target,
                    Collections.singletonList(VertxConstants.ROUTE_BASE),
                    "path");
        } else if (target.kind().equals(METHOD)) {
            pathValue = scannerContext.annotations().getAnnotationValue(
                    target,
                    Collections.singletonList(VertxConstants.ROUTE),
                    "path",
                    target.asMethod().name());
        }

        if (pathValue == null) {
            return List.of("");
        }

        if (pathValue.startsWith("/")) {
            pathValue = pathValue.substring(1);
        }

        if (pathValue.endsWith("/")) {
            pathValue = pathValue.substring(0, pathValue.length() - 1);
        }

        // Replace :var with {var} - NOSONAR
        if (pathValue.contains(":")) {
            List<String> parts = Arrays.asList(pathValue.split("/"));
            List<String> partsConverted = new ArrayList<>();
            for (String part : parts) {
                if (part.startsWith(":")) {
                    part = "{" + part.substring(1) + "}";
                }
                partsConverted.add(part);
            }
            pathValue = String.join("/", partsConverted.toArray(new String[] {}));
        }

        return List.of(pathValue);
    }

    @Override
    protected boolean isSubResourceLocator(MethodInfo method) {
        return false;
    }

    @Override
    protected boolean isResourceMethod(MethodInfo method) {
        return method.annotations()
                .stream()
                .map(AnnotationInstance::name)
                .anyMatch(VertxConstants.ROUTE::equals);
    }

    @Override
    protected boolean isParameter(DotName annotationName) {
        if (VertxParameter.isParameter(annotationName)) {
            return true;
        }
        if (Names.PARAMETER.equals(annotationName)) {
            return true;
        }
        return Names.PARAMETERS.equals(annotationName);
    }
}
