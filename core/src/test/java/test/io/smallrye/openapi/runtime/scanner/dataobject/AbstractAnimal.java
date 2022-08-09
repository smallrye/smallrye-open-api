package test.io.smallrye.openapi.runtime.scanner.dataobject;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/* Test models and resources below. */
@com.fasterxml.jackson.annotation.JsonPropertyOrder(value = { "age", "type" })
public abstract class AbstractAnimal implements Animal {

    @Schema
    private String type;
    protected Integer age;
    private boolean extinct;

    @Schema(name = "pet_type", required = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Schema
    public Boolean isExtinct() {
        return extinct;
    }

    public void setExtinct(boolean extinct) {
        this.extinct = extinct;
    }

}
