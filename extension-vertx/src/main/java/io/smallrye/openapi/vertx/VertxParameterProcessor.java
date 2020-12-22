package io.smallrye.openapi.vertx;

import static org.jboss.jandex.AnnotationTarget.Kind.CLASS;
import static org.jboss.jandex.AnnotationTarget.Kind.METHOD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.runtime.io.parameter.ParameterConstant;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
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
            Function<AnnotationInstance, Parameter> reader,
            List<AnnotationScannerExtension> extensions) {
        super(scannerContext, reader, extensions);
    }

    /**
     * Process parameter annotations for the given class and method.This method operates
     * in two phases. First, class-level parameters are processed and saved in the
     * {@link ResourceParameters}. Second, method-level parameters are processed. Form parameters
     * are only applicable to the method-level in this component.
     *
     * @param context the AnnotationScannerContext
     * @param resourceClass the class info
     * @param resourceMethod the Vert.x resource method, annotated with one of the
     *        Vert.x HTTP annotations
     * @param reader callback method for a function producing {@link Parameter} from a
     *        {@link org.eclipse.microprofile.openapi.annotations.parameters.Parameter}
     * @param extensions scanner extensions
     * @return scanned parameters and modified path contained in a {@link ResourceParameters}
     *         object
     */
    public static ResourceParameters process(AnnotationScannerContext context,
            ClassInfo resourceClass,
            MethodInfo resourceMethod,
            Function<AnnotationInstance, Parameter> reader,
            List<AnnotationScannerExtension> extensions) {

        VertxParameterProcessor processor = new VertxParameterProcessor(context, reader, extensions);
        return processor.process(resourceClass, resourceMethod, reader);
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

        if (ParameterConstant.DOTNAME_PARAMETER.equals(name) && readerFunction != null) {
            Parameter oaiParam = readerFunction.apply(annotation);
            readParameter(new ParameterContextKey(oaiParam.getName(), oaiParam.getIn(), styleOf(oaiParam)),
                    oaiParam,
                    null,
                    null, /* defaultValue */
                    annotation.target(),
                    overriddenParametersOnly);
        } else if (VertxConstants.PARAM.equals(name) && annotation.value() != null) {
            String parameterName = annotation.value().asString();
            String path = null;
            MethodInfo resourceMethod = null;

            if (annotation.target().kind() == METHOD) {
                resourceMethod = annotation.target().asMethod();
            } else if (annotation.target().kind() == Kind.METHOD_PARAMETER) {
                resourceMethod = annotation.target().asMethodParameter().method();
            }

            if (resourceMethod != null) {
                AnnotationInstance routeAnnotation = resourceMethod.annotation(VertxConstants.ROUTE);
                AnnotationValue pathValue = routeAnnotation.value("path");
                path = resourceMethod.name(); // default to methodName
                if (pathValue != null) {
                    path = pathValue.asString();
                }
            }

            if (path != null && path.contains(":" + parameterName)) {
                FrameworkParameter vertxParameter = VertxParameter.PATH_PARAM.parameter;
                readAnnotatedType(vertxParameter, annotation, beanParamAnnotation, overriddenParametersOnly);
            } else {
                FrameworkParameter vertxParameter = VertxParameter.QUERY_PARAM.parameter;
                readAnnotatedType(vertxParameter, annotation, beanParamAnnotation, overriddenParametersOnly);
            }

        } else if (VertxConstants.HEADER_PARAM.equals(name) && annotation.value() != null) {
            FrameworkParameter vertxParameter = VertxParameter.HEADER_PARAM.parameter;
            readAnnotatedType(vertxParameter, annotation, beanParamAnnotation, overriddenParametersOnly);
        }

    }

    private void readAnnotatedType(FrameworkParameter frameworkParam, AnnotationInstance annotation,
            AnnotationInstance beanParamAnnotation, boolean overriddenParametersOnly) {
        if (frameworkParam != null) {
            AnnotationTarget target = annotation.target();
            Type targetType = getType(target);

            if (frameworkParam.style == Style.FORM) {
                // Store the @FormParam for later processing
                formParams.put(paramName(annotation), annotation);
            } else if (frameworkParam.style == Style.MATRIX) {
                // Store the @MatrixParam for later processing
                String pathSegment = beanParamAnnotation != null
                        ? lastPathSegmentOf(beanParamAnnotation.target())
                        : lastPathSegmentOf(target);

                matrixParams.computeIfAbsent(pathSegment, k -> new HashMap<>())
                        .put(paramName(annotation), annotation);

                // Do this in Vert.x ?
                //}else if (frameworkParam.location == In.PATH && targetType != null
                //      && VertxConstants.REQUEST_MAPPING.equals(targetType.name())) {
                //    String pathSegment = JandexUtil.value(annotation, ParameterConstant.PROP_VALUE);

                //    if (!matrixParams.containsKey(pathSegment)) {
                //        matrixParams.put(pathSegment, new HashMap<>());
                //   }
            } else if (frameworkParam.location != null) {
                readParameter(
                        new ParameterContextKey(paramName(annotation), frameworkParam.location, frameworkParam.defaultStyle),
                        null,
                        frameworkParam,
                        null, /* defaultValue */
                        target,
                        overriddenParametersOnly);
            } else if (target != null) {
                // This is a @BeanParam or a RESTEasy @MultipartForm
                setMediaType(frameworkParam);

                if (TypeUtil.isOptional(targetType)) {
                    targetType = TypeUtil.getOptionalType(targetType);
                }

                if (targetType != null) {
                    ClassInfo beanParam = index.getClassByName(targetType.name());
                    readParameters(beanParam, annotation, overriddenParametersOnly);
                }
            }
        }
    }

    @Override
    protected String pathOf(AnnotationTarget target) {
        AnnotationInstance path = null;
        String defaultPathValue = null;
        if (target.kind().equals(CLASS)) {
            DotName possiblePath = VertxConstants.ROUTE_BASE;
            AnnotationInstance classAnnotation = target.asClass().classAnnotation(possiblePath);
            if (classAnnotation != null && classAnnotation.value("path") != null) {
                path = classAnnotation;
            }
        } else if (target.kind().equals(METHOD)) {
            defaultPathValue = target.asMethod().name();
            DotName possiblePath = VertxConstants.ROUTE;
            AnnotationInstance methodAnnotation = target.asMethod().annotation(possiblePath);
            if (methodAnnotation != null) {
                path = methodAnnotation;
            }
        }

        if (path != null) {
            String pathValue = routePathValuesToPath(path, defaultPathValue);
            if (pathValue != null) {
                if (pathValue.startsWith("/")) {
                    pathValue = pathValue.substring(1);
                }

                if (pathValue.endsWith("/")) {
                    pathValue = pathValue.substring(0, pathValue.length() - 1);
                }

                // Replace :var with {var}
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
                return pathValue;
            }
        }

        return "";
    }

    /**
     * Creates a String path from the Route path value
     *
     * @param routeAnnotation
     * @return
     */
    static String routePathValuesToPath(AnnotationInstance routeAnnotation, String defaultValue) {
        AnnotationValue value = routeAnnotation.value("path");
        if (value != null) {
            return value.asString();
        }
        return defaultValue;
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
        if (ParameterConstant.DOTNAME_PARAMETER.equals(annotationName)) {
            return true;
        }
        return ParameterConstant.DOTNAME_PARAMETERS.equals(annotationName);
    }
}
