package io.smallrye.openapi.spring;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.DotName;

import io.smallrye.openapi.runtime.scanner.spi.FrameworkParameter;

/**
 * Meta information for the Spring Parameter annotations relating them
 * to the In and Style attributes of Parameters.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum SpringParameter {
    PATH_PARAM(SpringConstants.PATH_PARAM, Parameter.In.PATH, null, Parameter.Style.SIMPLE),
    MATRIX_PARAM(SpringConstants.MATRIX_PARAM, Parameter.In.PATH, Parameter.Style.MATRIX, Parameter.Style.MATRIX),
    QUERY_PARAM(SpringConstants.QUERY_PARAM, Parameter.In.QUERY, null, Parameter.Style.FORM),
    HEADER_PARAM(SpringConstants.HEADER_PARAM, Parameter.In.HEADER, null, Parameter.Style.SIMPLE),
    COOKIE_PARAM(SpringConstants.COOKIE_PARAM, Parameter.In.COOKIE, null, Parameter.Style.FORM),
    // SpringDoc annotation to indicate a bean with parameters (like Jakarta @BeanParam)
    PARAMETER_OBJECT(SpringConstants.PARAMETER_OBJECT, null, null, null);

    //BEAN_PARAM(SpringConstants.BEAN_PARAM, null, null, null),
    //FORM_PARAM(SpringConstants.FORM_PARAM, null, Parameter.Style.FORM, Parameter.Style.FORM),

    final FrameworkParameter parameter;

    private SpringParameter(DotName name, Parameter.In location, Parameter.Style style, Parameter.Style defaultStyle,
            String mediaType) {
        this.parameter = new FrameworkParameter(name, location, style, defaultStyle, mediaType);
    }

    private SpringParameter(DotName name, Parameter.In location, Parameter.Style style, Parameter.Style defaultStyle) {
        this(name, location, style, defaultStyle, null);
    }

    static FrameworkParameter forName(DotName annotationName) {
        for (SpringParameter value : values()) {
            for (DotName name : value.parameter.getNames()) {
                if (name.equals(annotationName)) {
                    return value.parameter;
                }
            }
        }
        return null;
    }

    public static boolean isParameter(DotName annotationName) {
        return forName(annotationName) != null;
    }
}
