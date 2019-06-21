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

package io.smallrye.openapi.tck.extra.jaxrs;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author eric.wittmann@gmail.com
 */
@Path("/jaxrs/widgets")
@Consumes("application/json")
public class WidgetResource {

    @GET
    public List<Widget> getAllWidgets() {
        List<Widget> rval = new ArrayList<>();
        Widget w1 = new Widget();
        w1.setName("Widget One");
        w1.setDescription("The description of widget one.");
        Widget w2 = new Widget();
        w2.setName("Widget Two");
        w2.setDescription("The description of widget two.");
        rval.add(w1);
        rval.add(w2);
        return rval;
    }

    @POST
    public void createWidget(Widget widget) {
        // Add the widget to the DB
    }

    @GET
    @Path("{widgetId}")
    public Widget getWidget(@PathParam("widgetId") String widgetId) {
        Widget widget = new Widget();
        widget.setName("Foo Widget");
        widget.setDescription("The description of the widget.");
        return widget;
    }

    @PUT
    @Path("{widgetId}")
    public void updateWidget(@PathParam("widgetId") String widgetId, Widget widget) {
        // Update the widget
    }

    public static class Widget {
        private String name;
        private String description;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
