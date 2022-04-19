package test.io.smallrye.openapi.runtime.scanner.javax;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(hidden = true)
public class HiddenFruit {

    public String name;
    public String description;

    public HiddenFruit() {
    }

    public HiddenFruit(String name, String description) {
        this.name = name;
        this.description = description;
    }
}