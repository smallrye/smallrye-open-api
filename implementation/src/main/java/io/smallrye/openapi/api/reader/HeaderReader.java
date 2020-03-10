package io.smallrye.openapi.api.reader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.headers.Header;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.models.headers.HeaderImpl;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;

/**
 * Reading the Header annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#headerObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class HeaderReader {
    private static final Logger LOG = Logger.getLogger(HeaderReader.class);

    private HeaderReader() {
    }

    /**
     * Reads a map of Header annotations.
     * 
     * @param context the scanning context
     * @param annotationValue map of {@literal @}Header annotations
     * @return Map of Header models
     */
    public static Map<String, Header> readHeaders(final AnnotationScannerContext context,
            final AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @Header annotations.");
        Map<String, Header> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);
            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }
            if (name != null) {
                map.put(name, readHeader(context, nested));
            }
        }
        return map;
    }

    /**
     * Reads a Header annotation into a model.
     * 
     * @param annotationInstance the {@literal @}Header annotations
     * @return Header model
     */
    private static Header readHeader(final AnnotationScannerContext context, AnnotationInstance annotationInstance) {
        if (annotationInstance == null) {
            return null;
        }
        LOG.debug("Processing a single @Header annotation.");
        Header header = new HeaderImpl();
        header.setDescription(JandexUtil.stringValue(annotationInstance, OpenApiConstants.PROP_DESCRIPTION));
        header.setSchema(SchemaFactory.readSchema(context.getIndex(), annotationInstance.value(OpenApiConstants.PROP_SCHEMA)));
        header.setRequired(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_REQUIRED));
        header.setDeprecated(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_DEPRECATED));
        header.setAllowEmptyValue(JandexUtil.booleanValue(annotationInstance, OpenApiConstants.PROP_ALLOW_EMPTY_VALUE));
        header.setRef(JandexUtil.refValue(annotationInstance, JandexUtil.RefType.Header));
        return header;
    }

}
