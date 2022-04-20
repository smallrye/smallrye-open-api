/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.openapi.tck.extra.extensions;

import jakarta.ws.rs.*;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.extensions.Extensions;

/**
 * @author eric.wittmann@gmail.com
 */
@Path("/extensions")
@Consumes("application/json")
@Extension(name = "x-type-extension", value = "Type extension value.")
public class ExtensionResource {

    static class Bean {
        @DefaultValue("BEAN1")
        String dontCare;
    }

    @GET
    @Path("/typeExtension")
    public String getValueWithTypeExtension() {
        return "Type extension value.";
    }

    @GET
    @Path("/opExtension")
    @Extension(name = "x-operation-extension", value = "Operation extension value.")
    public String getValueWithOperationExtension() {
        return "Operation extension value.";
    }

    @GET
    @Path("/opWrapperExtension")
    @Extensions({
            @Extension(name = "x-operation-extension-1", value = "Operation extension wrapper value (1)."),
            @Extension(name = "x-operation-extension-2", value = "Operation extension wrapper value (2).")
    })
    public String getValueWithOperationExtensionWrapper() {
        return "Operation wrapper extension value.";
    }

    @SuppressWarnings("unused")
    @GET
    @Path("pathParamExtension/{param}")
    public String getParamWithExtension(
            @BeanParam Bean bean,
            @PathParam("param") @Extension(name = "x-parameter-extension", value = "Parameter extension value.") String param) {
        return "Path param extension value.";
    }
}
