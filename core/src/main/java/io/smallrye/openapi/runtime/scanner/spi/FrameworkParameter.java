package io.smallrye.openapi.runtime.scanner.spi;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.In;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.Style;
import org.jboss.jandex.DotName;

public class FrameworkParameter {

    public final DotName name;
    public final Parameter.In location;
    public final Parameter.Style style;
    public final Parameter.Style defaultStyle;
    public final String mediaType;

    public FrameworkParameter(DotName name, In location, Style style, Style defaultStyle, String mediaType) {
        super();
        this.name = name;
        this.location = location;
        this.style = style;
        this.defaultStyle = defaultStyle;
        this.mediaType = mediaType;
    }

    public DotName getName() {
        return name;
    }

    public Parameter.In getLocation() {
        return location;
    }

    public Parameter.Style getStyle() {
        return style;
    }

    public Parameter.Style getDefaultStyle() {
        return defaultStyle;
    }

    public String getMediaType() {
        return mediaType;
    }

}
