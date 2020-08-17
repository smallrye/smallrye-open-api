package io.smallrye.openapi.jaxrs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.microprofile.openapi.models.media.Encoding;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.In;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.Style;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.smallrye.openapi.api.models.media.EncodingImpl;
import io.smallrye.openapi.runtime.io.parameter.ParameterConstant;
import io.smallrye.openapi.runtime.scanner.AnnotationScannerExtension;
import io.smallrye.openapi.runtime.scanner.ResourceParameters;
import io.smallrye.openapi.runtime.scanner.spi.AbstractParameterProcessor;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.scanner.spi.FrameworkParameter;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.TypeUtil;

/**
 *
 * Note, {@link javax.ws.rs.PathParam PathParam} targets of
 * {@link javax.ws.rs.core.PathSegment PathSegment} are not currently supported.
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 *
 */
public class ParameterProcessor extends AbstractParameterProcessor {

    /**
     * Pattern to describe a path template parameter with a regular expression pattern restriction.
     * 
     * Sonar validation is disabled on this expression because there is no danger of denial of
     * service attacks input derived from the developer of the host application.
     * 
     * See JAX-RS {@link javax.ws.rs.Path Path} JavaDoc for explanation.
     */
    static final Pattern TEMPLATE_PARAM_PATTERN = Pattern
            .compile("\\{[ \\t]*(\\w[\\w\\.-]*)[ \\t]*:[ \\t]*((?:[^{}]|\\{[^{}]+\\})+)\\}"); //NOSONAR

    private ParameterProcessor(AnnotationScannerContext scannerContext,
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
     * @param resourceMethod the JAX-RS resource method, annotated with one of the
     *        JAX-RS HTTP annotations
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

        ParameterProcessor processor = new ParameterProcessor(context, reader, extensions);
        return processor.process(resourceClass, resourceMethod, reader);
    }

    @Override
    protected void processPathParameters(ClassInfo resourceClass, MethodInfo resourceMethod, ResourceParameters parameters) {
        ClassInfo resourceMethodClass = resourceMethod.declaringClass();

        /*
         * Phase I - Read class fields, constructors, "setter" methods not annotated with JAX-RS
         * HTTP method. Check both the class declaring the method as well as the resource
         * class, if different.
         */
        readParametersInherited(resourceMethodClass, null, false);

        if (!resourceClass.equals(resourceMethodClass)) {
            /*
             * The resource class may be a subclass/implementor of the resource method class. Scanning
             * the resource class after the method's class allows for parameter details to be overridden
             * by annotations in the subclass.
             */
            readParameters(resourceClass, null, true);
        }

        parameters.setPathItemParameters(getParameters(resourceMethod));
    }

    @Override
    protected Pattern getTemplateParameterPattern() {
        return TEMPLATE_PARAM_PATTERN;
    }

    @Override
    protected FrameworkParameter getMatrixParameter() {
        return JaxRsParameter.MATRIX_PARAM.parameter;
    }

    @Override
    protected String getDefaultFormMediaType() {
        return javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
    }

    @Override
    protected void addEncoding(Map<String, Encoding> encodings, String paramName, AnnotationTarget paramTarget) {
        if (paramTarget == null) {
            return;
        }

        AnnotationInstance type = TypeUtil.getAnnotation(paramTarget, RestEasyConstants.PART_TYPE);

        if (type != null) {
            Encoding encoding = new EncodingImpl();
            encoding.setContentType(type.value().asString());
            encodings.put(paramName, encoding);
        }
    }

    /**
     * Check if the given parameter name is present as a path segment in the resourcePath.
     * 
     * @param paramName name of parameter
     * @param paramStyle style of parameter, e.g. simple or matrix
     * @param resourcePath resource path/URL
     * @return true if the paramName is in the resourcePath, false otherwise.
     */
    static boolean parameterInPath(String paramName, Style paramStyle, String resourcePath) {
        if (paramName == null || resourcePath == null) {
            return true;
        }

        final String regex;

        if (Style.MATRIX.equals(paramStyle)) {
            regex = String.format("(?:\\{[ \\t]*|^|/?)\\Q%s\\E(?:[ \\t]*(?:}|:)|/?|$)", paramName);
        } else {
            regex = String.format("\\{[ \\t]*\\Q%s\\E[ \\t]*(?:}|:)", paramName);
        }

        return Pattern.compile(regex).matcher(resourcePath).find();
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
            FrameworkParameter frameworkParam = JaxRsParameter.forName(name);

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
                } else if (frameworkParam.location == In.PATH && targetType != null
                        && JaxRsConstants.PATH_SEGMENT.equals(targetType.name())) {
                    String pathSegment = JandexUtil.value(annotation, ParameterConstant.PROP_VALUE);

                    if (!matrixParams.containsKey(pathSegment)) {
                        matrixParams.put(pathSegment, new HashMap<>());
                    }
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
                        readParametersInherited(beanParam, annotation, overriddenParametersOnly);
                    }
                }
            }
        }
    }

    @Override
    protected DotName getDefaultAnnotationName() {
        return JaxRsConstants.DEFAULT_VALUE;
    }

    @Override
    protected String getDefaultAnnotationProperty() {
        return ParameterConstant.PROP_VALUE;
    }

    @Override
    protected String pathOf(AnnotationTarget target) {
        AnnotationInstance path = null;

        switch (target.kind()) {
            case CLASS:
                path = target.asClass().classAnnotation(JaxRsConstants.PATH);
                break;
            case METHOD:
                path = target.asMethod().annotation(JaxRsConstants.PATH);
                break;
            default:
                break;
        }

        if (path != null) {
            String pathValue = path.value().asString();

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

    @Override
    protected boolean isSubResourceLocator(MethodInfo method) {
        return method.returnType().kind() == Type.Kind.CLASS &&
                method.hasAnnotation(JaxRsConstants.PATH) &&
                method.annotations().stream()
                        .map(AnnotationInstance::name)
                        .noneMatch(JaxRsConstants.HTTP_METHODS::contains);
    }

    @Override
    protected boolean isResourceMethod(MethodInfo method) {
        return method.annotations()
                .stream()
                .map(AnnotationInstance::name)
                .anyMatch(JaxRsConstants.HTTP_METHODS::contains);
    }

    @Override
    protected boolean isParameter(DotName annotationName) {
        if (JaxRsParameter.isParameter(annotationName)) {
            return true;
        }
        if (ParameterConstant.DOTNAME_PARAMETER.equals(annotationName)) {
            return true;
        }
        return ParameterConstant.DOTNAME_PARAMETERS.equals(annotationName);
    }
}
