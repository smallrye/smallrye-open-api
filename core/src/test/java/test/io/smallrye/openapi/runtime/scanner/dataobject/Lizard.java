package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class Lizard extends AbstractAnimal implements Reptile {

    @Schema(deprecated = true)
    static String scaleColor;
    boolean lovesRocks;

    @Override
    public String getScaleColor() {
        return "green";
    }

    public void setScaleColor(String scaleColor) {
        // Bad idea, but doing it anyway ;-)
        Lizard.scaleColor = scaleColor;
    }

    public void setAge(String age) {
        super.setAge(Integer.parseInt(age));
    }

}
