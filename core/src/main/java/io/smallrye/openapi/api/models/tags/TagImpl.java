package io.smallrye.openapi.api.models.tags;

import org.eclipse.microprofile.openapi.OASFactory;

import io.smallrye.openapi.internal.models.tags.AbstractTag;

/**
 * @deprecated use {@link OASFactory#createTag()} instead.
 */
@Deprecated(since = "4.0", forRemoval = true)
public class TagImpl extends AbstractTag { // NOSONAR
}
