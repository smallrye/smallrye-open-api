package io.smallrye.openapi.runtime.scanner;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import test.io.smallrye.openapi.runtime.scanner.entities.SpecialCaseTestContainer;

/**
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
class SpecialCaseTests extends JaxRsDataObjectScannerTestBase {

    @ParameterizedTest
    @CsvSource({
            "SimpleTerminalType, listOfString, special.simple.expected.json",
            "DataObjectList, ccList, special.dataObjectList.expected.json",
            "WildcardWithSuperBound, listSuperFlight, special.wildcardWithSuperBound.expected.json",
            "WildcardWithExtendBound, listExtendsFoo, special.wildcardWithExtendBound.expected.json",
            "Wildcard, listOfAnything, special.wildcard.expected.json"
    })
    void testCollection(String label, String field, String expectedResource) throws IOException, JSONException {
        String name = SpecialCaseTestContainer.class.getName();
        Type pType = getFieldFromKlazz(name, field).type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(context, pType);

        Schema result = scanner.process();

        printToConsole(name, result);
        assertJsonEquals(name, expectedResource, result);
    }

}
