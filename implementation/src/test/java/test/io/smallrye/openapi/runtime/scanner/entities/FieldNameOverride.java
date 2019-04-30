/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.io.smallrye.openapi.runtime.scanner.entities;

import javax.json.bind.annotation.JsonbProperty;

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
