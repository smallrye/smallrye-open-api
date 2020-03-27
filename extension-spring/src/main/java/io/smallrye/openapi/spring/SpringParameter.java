package io.smallrye.openapi.spring;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.DotName;

/**
 * Meta information for the Spring Parameter annotations relating them
 * to the {@link In} and {@link Style} attributes of {@link Parameter}s.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum SpringParameter {
    PATH_PARAM(SpringConstants.PATH_PARAM, Parameter.In.PATH, null, Parameter.Style.SIMPLE),
    MATRIX_PARAM(SpringConstants.MATRIX_PARAM, Parameter.In.PATH, Parameter.Style.MATRIX, Parameter.Style.MATRIX),
    QUERY_PARAM(SpringConstants.QUERY_PARAM, Parameter.In.QUERY, null, Parameter.Style.FORM),
    HEADER_PARAM(SpringConstants.HEADER_PARAM, Parameter.In.HEADER, null, Parameter.Style.SIMPLE),
    COOKIE_PARAM(SpringConstants.COOKIE_PARAM, Parameter.In.COOKIE, null, Parameter.Style.FORM);

    //BEAN_PARAM(SpringConstants.BEAN_PARAM, null, null, null),
    //FORM_PARAM(SpringConstants.FORM_PARAM, null, Parameter.Style.FORM, Parameter.Style.FORM),

    private final DotName name;
    final Parameter.In location;
    final Parameter.Style style;
    final Parameter.Style defaultStyle;
    final String mediaType;

    private SpringParameter(DotName name, Parameter.In location, Parameter.Style style, Parameter.Style defaultStyle,
            String mediaType) {
        this.name = name;
        this.location = location;
        this.style = style;
        this.defaultStyle = defaultStyle;
        this.mediaType = mediaType;
    }

    private SpringParameter(DotName name, Parameter.In location, Parameter.Style style, Parameter.Style defaultStyle) {
        this(name, location, style, defaultStyle, null);
    }

    static SpringParameter forName(DotName annotationName) {
        for (SpringParameter value : values()) {
            if (value.name.equals(annotationName)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isParameter(DotName annotationName) {
        for (SpringParameter value : values()) {
            if (value.name.equals(annotationName)) {
                return true;
            }
        }
        return false;
    }
}
