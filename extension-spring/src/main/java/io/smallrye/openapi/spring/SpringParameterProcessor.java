package io.smallrye.openapi.spring;

import static org.jboss.jandex.AnnotationTarget.Kind.CLASS;
import static org.jboss.jandex.AnnotationTarget.Kind.METHOD;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
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
     * @param resourceMethod the Spring resource method, annotated with one of the
     *        Spring HTTP annotations
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

        SpringParameterProcessor processor = new SpringParameterProcessor(context, reader, extensions);
        return processor.process(resourceClass, resourceMethod, reader);
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

        if (ParameterConstant.DOTNAME_PARAMETER.equals(name) && readerFunction != null) {
            Parameter oaiParam = readerFunction.apply(annotation);

            readParameter(new ParameterContextKey(oaiParam.getName(), oaiParam.getIn(), styleOf(oaiParam)),
                    oaiParam,
                    null,
                    null,
                    annotation.target(),
                    overriddenParametersOnly);
        } else {
            FrameworkParameter frameworkParam = SpringParameter.forName(name);

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

                    if (!matrixParams.containsKey(pathSegment)) {
                        matrixParams.put(pathSegment, new HashMap<>());
                    }

                    matrixParams.get(pathSegment).put(paramName(annotation), annotation);
                    // Do this in Spring ?
                    //}else if (frameworkParam.location == In.PATH && targetType != null
                    //      && SpringConstants.REQUEST_MAPPING.equals(targetType.name())) {
                    //    String pathSegment = JandexUtil.value(annotation, ParameterConstant.PROP_VALUE);

                    //    if (!matrixParams.containsKey(pathSegment)) {
                    //        matrixParams.put(pathSegment, new HashMap<>());
                    //   }
                } else if (frameworkParam.location != null) {
                    readParameter(
                            new ParameterContextKey(paramName(annotation), frameworkParam.location,
                                    frameworkParam.defaultStyle),
                            null,
                            frameworkParam,
                            getDefaultValue(target),
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
    }

    @Override
    protected DotName getDefaultAnnotationName() {
        return SpringConstants.QUERY_PARAM;
    }

    @Override
    protected String getDefaultAnnotationProperty() {
        return "defaultValue";
    }

    @Override
    protected String pathOf(AnnotationTarget target) {
        AnnotationInstance path = null;
        Set<DotName> paths = SpringConstants.HTTP_METHODS;

        if (target.kind().equals(CLASS)) {
            for (DotName possiblePath : paths) {
                AnnotationInstance classAnnotation = target.asClass().classAnnotation(possiblePath);
                if (classAnnotation != null && (classAnnotation.value() != null || classAnnotation.value("path") != null)) {
                    path = classAnnotation;
                }
            }
        } else if (target.kind().equals(METHOD)) {
            for (DotName possiblePath : paths) {
                AnnotationInstance methodAnnotation = target.asMethod().annotation(possiblePath);
                if (methodAnnotation != null && (methodAnnotation.value() != null || methodAnnotation.value("path") != null)) {
                    path = methodAnnotation;
                }
            }
            // Also support @RequestMapping
            AnnotationInstance methodAnnotation = target.asMethod().annotation(SpringConstants.REQUEST_MAPPING);
            if (methodAnnotation != null && (methodAnnotation.value() != null || methodAnnotation.value("path") != null)) {
                path = methodAnnotation;
            }
        }

        if (path != null) {
            String pathValue = requestMappingValuesToPath(path);
            if (pathValue.startsWith("/")) {
                pathValue = pathValue.substring(1);
            }

            if (pathValue.endsWith("/")) {
                pathValue = pathValue.substring(0, pathValue.length() - 1);
            }

            return pathValue;
        }

        return "";
    }

    /**
     * Creates a String path from the RequestMapping value
     *
     * @param requestMappingAnnotation
     * @return
     */
    static String requestMappingValuesToPath(AnnotationInstance requestMappingAnnotation) {
        StringBuilder sb = new StringBuilder();
        AnnotationValue value = getRequestMappingPathAnnotation(requestMappingAnnotation);
        if (value != null) {
            String[] parts = value.asStringArray();
            for (String part : parts) {
                sb.append(part);
            }
        }
        return sb.toString();
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
        if (ParameterConstant.DOTNAME_PARAMETER.equals(annotationName)) {
            return true;
        }
        return ParameterConstant.DOTNAME_PARAMETERS.equals(annotationName);
    }
}
