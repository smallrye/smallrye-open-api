package io.smallrye.openapi.runtime.scanner;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import io.smallrye.openapi.api.constants.OpenApiConstants;
import io.smallrye.openapi.api.util.ClassLoaderUtil;
import io.smallrye.openapi.runtime.scanner.spi.AnnotationScannerContext;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class JaxRsDataObjectScannerTestBase extends IndexScannerTestBase {

    protected AnnotationScannerContext context;
    protected static Index index;

    @BeforeAll
    public static void createIndex() {
        Indexer indexer = new Indexer();

        // Stand-in stuff
        index(indexer, "io/smallrye/openapi/runtime/scanner/CollectionStandin.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/IterableStandin.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/MapStandin.class");

        // Test samples
        indexDirectory(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/");
        indexDirectory(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/jakarta/");

        // Microprofile TCK classes
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Airline.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Booking.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/CreditCard.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Flight.class");
        index(indexer, "org/eclipse/microprofile/openapi/apps/airlines/model/Airline.class");

        // Test containers
        //indexDirectory(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/");

        index = indexer.complete();
    }

    @BeforeEach
    public void createContext() {
        context = new AnnotationScannerContext(index, ClassLoaderUtil.getDefaultClassLoader(),
                dynamicConfig(OpenApiConstants.SMALLRYE_SORTED_PROPERTIES_ENABLE, Boolean.TRUE));
    }

    @AfterEach
    public void tearDown() {
        super.removeSchemaRegistry();
    }

    public FieldInfo getFieldFromKlazz(String containerName, String fieldName) {
        ClassInfo container = index.getClassByName(DotName.createSimple(containerName));
        return container.field(fieldName);
    }
}
