package io.smallrye.openapi.runtime.scanner;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base for Spring tests. Index the test classes
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SpringDataObjectScannerTestBase extends IndexScannerTestBase {

    protected static Index index;

    @BeforeAll
    public static void createIndex() {
        Indexer indexer = new Indexer();

        // Test samples
        indexDirectory(indexer, "test/io/smallrye/openapi/runtime/scanner/entities/");

        index = indexer.complete();
    }

}
