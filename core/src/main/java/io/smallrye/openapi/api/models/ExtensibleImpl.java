package io.smallrye.openapi.api.models;

import org.eclipse.microprofile.openapi.models.Extensible;

/**
 * Implementation of the {@link Extensible} OpenAPI model interface. Base class for many of the
 * OpenAPI models.
 *
 * @author eric.wittmann@gmail.com
 */
@Deprecated(since = "4.0", forRemoval = true)
public abstract class ExtensibleImpl<T extends Extensible<T>> implements Extensible<T>, ModelImpl { // NOSONAR
}
