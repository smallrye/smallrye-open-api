package io.smallrye.openapi.api.reader;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;
import io.smallrye.openapi.runtime.util.JandexUtil;
import io.smallrye.openapi.runtime.util.SchemaFactory;

/**
 * Reading the Schema annotation
 * 
 * @see https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schemaObject
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Eric Wittmann (eric.wittmann@gmail.com)
 */
public class SchemaReader {
    private static final Logger LOG = Logger.getLogger(SchemaReader.class);

    private SchemaReader() {
    }

    /**
     * Reads a map of Schema annotations.
     * 
     * @param context the scanner context
     * @param annotationValue map of {@literal @}Schema annotations
     * @return Map of Schema models
     */
    public static Map<String, Schema> readSchemas(final AnnotationScannerContext context, AnnotationValue annotationValue) {
        if (annotationValue == null) {
            return null;
        }
        LOG.debug("Processing a map of @Schema annotations.");
        Map<String, Schema> map = new LinkedHashMap<>();
        AnnotationInstance[] nestedArray = annotationValue.asNestedArray();
        for (AnnotationInstance nested : nestedArray) {
            String name = JandexUtil.stringValue(nested, OpenApiConstants.PROP_NAME);

            if (name == null && JandexUtil.isRef(nested)) {
                name = JandexUtil.nameFromRef(nested);
            }

            /*
             * The name is REQUIRED when the schema is defined within
             * {@link org.eclipse.microprofile.openapi.annotations.Components}.
             */
            if (name != null) {
                map.put(name, SchemaFactory.readSchema(context.getIndex(), nested));
            } /*-
              //For consideration - be more lenient and attempt to use the name from the implementation's @Schema?
              else {
                if (JandexUtil.isSimpleClassSchema(nested)) {
                    Schema schema = SchemaFactory.readClassSchema(index, nested.value(OpenApiConstants.PROP_IMPLEMENTATION), false);
              
                    if (schema instanceof SchemaImpl) {
                        name = ((SchemaImpl) schema).getName();
              
                        if (name != null) {
                            map.put(name, schema);
                        }
                    }
                }
              }*/
        }
        return map;
    }

}
