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

package io.smallrye.openapi.tck.extra.jsonignoreproperties;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 */
@Path("/jsonignoreproperties")
@Consumes("application/json")
@SuppressWarnings("unused")
public class JsonIgnorePropertiesResource {

    @POST
    @Path("/ignoreProperties")
    public String getIgnoreProperties(@RequestBody IgnoreProps ignoreProps) {
        return "Type extension value.";
    }

    static class IgnoreProps {
        private DirectIgnore directIgnore;
        private InheritIgnore inheritIgnore;
        private ThirdLevelIgnore thirdLevelIgnore;
        private InheritIgnoreOverride inheritIgnoreOverride;
        private NestedOverride nestedOverride;
    }

    @JsonIgnoreProperties("ignoreMe")
    static class DirectIgnore {
        private String ignoreMe;
        private String dontIgnoreMe;
        private String ignoreMeNested;

        public String getIgnoreMe() {
            return ignoreMe;
        }

        public void setIgnoreMe(String ignoreMe) {
            this.ignoreMe = ignoreMe;
        }

        public String getDontIgnoreMe() {
            return dontIgnoreMe;
        }

        public void setDontIgnoreMe(String dontIgnoreMe) {
            this.dontIgnoreMe = dontIgnoreMe;
        }

        public String getIgnoreMeNested() {
            return ignoreMeNested;
        }

        public void setIgnoreMeNested(String ignoreMeNested) {
            this.ignoreMeNested = ignoreMeNested;
        }
    }

    static class InheritIgnore extends DirectIgnore {

        @Override
        public String getIgnoreMe() {
            return super.getIgnoreMe();
        }

        @Override
        public void setIgnoreMe(String ignoreMe) {
            super.setIgnoreMe(ignoreMe);
        }

        @Override
        public String getDontIgnoreMe() {
            return super.getDontIgnoreMe();
        }

        @Override
        public void setDontIgnoreMe(String dontIgnoreMe) {
            super.setDontIgnoreMe(dontIgnoreMe);
        }

        @Override
        public String getIgnoreMeNested() {
            return super.getIgnoreMeNested();
        }

        @Override
        public void setIgnoreMeNested(String ignoreMeNested) {
            super.setIgnoreMeNested(ignoreMeNested);
        }
    }

    static class ThirdLevelIgnore extends InheritIgnore {
        @Override
        public String getIgnoreMe() {
            return super.getIgnoreMe();
        }

        @Override
        public void setIgnoreMe(String ignoreMe) {
            super.setIgnoreMe(ignoreMe);
        }

        @Override
        public String getDontIgnoreMe() {
            return super.getDontIgnoreMe();
        }

        @Override
        public void setDontIgnoreMe(String dontIgnoreMe) {
            super.setDontIgnoreMe(dontIgnoreMe);
        }

        @Override
        public String getIgnoreMeNested() {
            return super.getIgnoreMeNested();
        }

        @Override
        public void setIgnoreMeNested(String ignoreMeNested) {
            super.setIgnoreMeNested(ignoreMeNested);
        }
    }

    @JsonIgnoreProperties("dontIgnoreMe")
    static class InheritIgnoreOverride extends DirectIgnore {
        @Override
        public String getIgnoreMe() {
            return super.getIgnoreMe();
        }

        @Override
        public void setIgnoreMe(String ignoreMe) {
            super.setIgnoreMe(ignoreMe);
        }

        @Override
        public String getDontIgnoreMe() {
            return super.getDontIgnoreMe();
        }

        @Override
        public void setDontIgnoreMe(String dontIgnoreMe) {
            super.setDontIgnoreMe(dontIgnoreMe);
        }

        @Override
        public String getIgnoreMeNested() {
            return super.getIgnoreMeNested();
        }

        @Override
        public void setIgnoreMeNested(String ignoreMeNested) {
            super.setIgnoreMeNested(ignoreMeNested);
        }
    }

    static class NestedOverride {
        @JsonIgnoreProperties({ "ignoreMeNested" })
        private DirectIgnore nested = new DirectIgnore();

        public DirectIgnore getNested() {
            return nested;
        }

        public void setNested(DirectIgnore nested) {
            this.nested = nested;
        }
    }
}
