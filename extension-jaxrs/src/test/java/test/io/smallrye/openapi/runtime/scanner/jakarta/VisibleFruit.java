package test.io.smallrye.openapi.runtime.scanner.jakarta;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(hidden = false)
public class VisibleFruit {

    public String name;
    public String description;

    public VisibleFruit() {
    }

    public VisibleFruit(String name, String description) {
        this.name = name;
        this.description = description;
    }
}