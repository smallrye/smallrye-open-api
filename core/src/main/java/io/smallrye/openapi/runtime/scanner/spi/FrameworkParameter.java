package io.smallrye.openapi.runtime.scanner.spi;

import java.util.Arrays;
import java.util.List;

import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.In;
import org.eclipse.microprofile.openapi.models.parameters.Parameter.Style;
import org.jboss.jandex.DotName;

public class FrameworkParameter {

    public final List<DotName> names;
    public final Parameter.In location;
    public final Parameter.Style style;
    public final Parameter.Style defaultStyle;
    public final String mediaType;

    public FrameworkParameter(DotName name, In location, Style style, Style defaultStyle, String mediaType) {
        this(Arrays.asList(name), location, style, defaultStyle, mediaType);
    }

    public FrameworkParameter(List<DotName> names, In location, Style style, Style defaultStyle, String mediaType) {
        super();
        this.names = names;
        this.location = location;
        this.style = style;
        this.defaultStyle = defaultStyle;
        this.mediaType = mediaType;
    }

    public List<DotName> getNames() {
        return names;
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
