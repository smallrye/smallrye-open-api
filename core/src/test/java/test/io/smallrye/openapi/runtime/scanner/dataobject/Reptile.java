package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public interface Reptile {

    @Schema(name = "scaleColor", description = "The color of a reptile's scales")
    public String getScaleColor();

    @Schema(name = "scaleColor", description = "This is how the color is set, but the description comes from getScaleColor")
    public void setScaleColor(String color);

}
