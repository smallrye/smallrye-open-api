package io.smallrye.openapi.runtime.io.tag;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.jandex.DotName;

/**
 * Constants related to Server
 * 
 * @see <a href="https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#tagObject">tagObject</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class TagConstant {
    static final DotName DOTNAME_TAG = DotName.createSimple(Tag.class.getName());
    public static final DotName DOTNAME_TAGS = DotName.createSimple(Tags.class.getName());

    public static final String PROP_NAME = "name";
    public static final String PROP_DESCRIPTION = "description";

    private TagConstant() {
    }
}
