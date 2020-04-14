package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.jboss.jandex.Index;
import org.junit.BeforeClass;

/**
 * Base for Spring tests. Index the test classes
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SpringDataObjectScannerTestBase extends IndexScannerTestBase {

    static {
        //if ( Boolean.getBoolean("trace")) {
        InputStream stream = SpringDataObjectScannerTestBase.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
    }

    protected static Index index;

    @BeforeClass
    public static void createIndex() {
        //     Indexer indexer = new Indexer();

        //     // Test samples
        //     indexDirectory(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/");

        //     index = indexer.complete();
    }

}
