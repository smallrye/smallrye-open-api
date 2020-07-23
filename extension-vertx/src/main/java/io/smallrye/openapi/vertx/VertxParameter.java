package io.smallrye.openapi.vertx;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.jboss.jandex.DotName;

/**
 * Meta information for the Vert.x Parameter annotations relating them
 * to the In and Style attributes of Parameters.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public enum VertxParameter {
    PATH_PARAM(VertxConstants.PARAM, Parameter.In.PATH, null, Parameter.Style.SIMPLE),
    QUERY_PARAM(VertxConstants.PARAM, Parameter.In.QUERY, null, Parameter.Style.FORM),
    HEADER_PARAM(VertxConstants.HEADER_PARAM, Parameter.In.HEADER, null, Parameter.Style.SIMPLE);

    private final DotName name;
    final Parameter.In location;
    final Parameter.Style style;
    final Parameter.Style defaultStyle;
    final String mediaType;

    private VertxParameter(DotName name, Parameter.In location, Parameter.Style style, Parameter.Style defaultStyle,
            String mediaType) {
        this.name = name;
        this.location = location;
        this.style = style;
        this.defaultStyle = defaultStyle;
        this.mediaType = mediaType;
    }

    private VertxParameter(DotName name, Parameter.In location, Parameter.Style style, Parameter.Style defaultStyle) {
        this(name, location, style, defaultStyle, null);
    }

    public static boolean isParameter(DotName annotationName) {
        for (VertxParameter value : values()) {
            if (value.name.equals(annotationName)) {
                return true;
            }
        }
        return false;
    }
}
