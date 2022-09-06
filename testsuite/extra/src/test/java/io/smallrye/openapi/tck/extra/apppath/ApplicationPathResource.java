/*
 * Copyright 2020 Red Hat, Inc.
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

package io.smallrye.openapi.tck.extra.apppath;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;

/**
 * @author eric.wittmann@gmail.com
 */
@javax.ws.rs.Path("/apppaths")
@Consumes("application/json")
public class ApplicationPathResource {

    @GET
    public List<Path> getAllPaths() {
        List<Path> rval = new ArrayList<>();
        Path path = new Path();
        path.setName("Path One");
        path.setSize(12l);
        Path path2 = new Path();
        path2.setName("Path Two");
        path2.setSize(19l);
        rval.add(path);
        rval.add(path2);
        return rval;
    }

    /**
     * @param path request body
     */
    @POST
    public void createPath(Path path) {
    }

    /**
     * @param path path parameter
     */
    @GET
    @javax.ws.rs.Path("{path}")
    public Path getPath(@PathParam("path") String path) {
        Path Path = new Path();
        Path.setName("Foo Path");
        Path.setSize(17l);
        return Path;
    }

    /**
     * @param path path parameter
     * @param body request body
     */
    @PUT
    @javax.ws.rs.Path("{path}")
    public void updatePath(@PathParam("path") String path, Path body) {
        // Update the Path
    }

}
