package io.smallrye.openapi.tck;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * RESTEasy Servlet initializer requires a REST Application to start. Some TCKs don't have an Application class, so we
 * add one to make sure that RESTEasy is able to deploy the application.
 */
@ApplicationPath("/")
public class OpenApiApplication extends Application {
}
