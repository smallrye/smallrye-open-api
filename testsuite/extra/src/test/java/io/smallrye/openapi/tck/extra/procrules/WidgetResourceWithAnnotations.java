package io.smallrye.openapi.tck.extra.procrules;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;

import io.smallrye.openapi.tck.extra.jaxrs.WidgetResource;

@ExternalDocumentation(url = "http://widget-external-docs.org", description = "Widget resource external documentation")
public class WidgetResourceWithAnnotations extends WidgetResource {
}
