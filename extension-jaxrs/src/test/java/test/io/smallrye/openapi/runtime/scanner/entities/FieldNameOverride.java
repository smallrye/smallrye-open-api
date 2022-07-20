package test.io.smallrye.openapi.runtime.scanner.entities;

import jakarta.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
public class FieldNameOverride {

    @JsonbProperty("dasherized-name")
    private String dasherizedName;

    @JsonbProperty("dasherized-name-required")
    @Schema(required = true)
    private String dasherizedNameRequired;

    @JsonbProperty("snake_case_name")
    private String snakeCaseName;

    @JsonbProperty("camelCaseNameCustom")
    private String camelCaseName;

    @JsonbProperty
    private String camelCaseNameDefault1;

    @SuppressWarnings("unused")
    private String camelCaseNameDefault2;

}
