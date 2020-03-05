/**
 * Copyright 2020 Red Hat, Inc, and individual contributors.
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
package io.smallrye.openapi.runtime.scanner;

/**
 * Help with making a valid path
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PathMaker {

    private PathMaker() {
    }

    /**
     * Make a path out of a number of path segments.
     * 
     * @param segments String paths
     * @return Path built from the segments
     */
    public static String makePath(String... segments) {
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (segment.startsWith("/")) {
                segment = segment.substring(1);
            }
            if (segment.endsWith("/")) {
                segment = segment.substring(0, segment.length() - 1);
            }
            if (segment.isEmpty()) {
                continue;
            }
            builder.append("/");
            builder.append(segment);
        }
        String rval = builder.toString();
        if (rval.isEmpty()) {
            return "/";
        }
        return rval;
    }
}
