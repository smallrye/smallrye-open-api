package io.smallrye.openapi.runtime.scanner;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.json.JSONException;
import org.junit.Test;

import io.smallrye.openapi.api.models.OpenAPIImpl;
import io.smallrye.openapi.api.models.media.SchemaImpl;
import io.smallrye.openapi.runtime.util.ModelUtil;

/**
 *
 * @author Michael Edgar {@literal <michael@xlate.io>}
 *
 */
public class SchemaRegistryTests extends IndexScannerTestBase {

    SchemaRegistry registry;

    @Test
    public void testParameterizedNameCollisionsUseSequence() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Container.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Nestable.class");
        Index index = indexer.complete();

        OpenAPIImpl oai = new OpenAPIImpl();
        SchemaRegistry registry = SchemaRegistry.newInstance(emptyConfig(), oai, index);

        DotName cName = componentize(Container.class.getName());
        ClassInfo cInfo = index.getClassByName(cName);

        FieldInfo n1 = cInfo.field("n1");
        FieldInfo n2 = cInfo.field("n2");
        FieldInfo n3 = cInfo.field("n3");

        Schema s1 = registry.register(n1.type(), new SchemaImpl());
        Schema s2 = registry.register(n2.type(), new SchemaImpl());
        Schema s3 = registry.register(n3.type(), new SchemaImpl());

        assertEquals("#/components/schemas/NestableStringNestable", s1.getRef());
        assertEquals("#/components/schemas/NestableStringNestable1", s2.getRef());
        assertEquals("#/components/schemas/NestableStringNestable2", s3.getRef());
    }

    @Test
    public void testWildcardLowerBoundName() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Container.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Nestable.class");
        Index index = indexer.complete();

        OpenAPIImpl oai = new OpenAPIImpl();
        SchemaRegistry registry = SchemaRegistry.newInstance(emptyConfig(), oai, index);

        DotName cName = componentize(Container.class.getName());
        ClassInfo cInfo = index.getClassByName(cName);

        FieldInfo n4 = cInfo.field("n4");
        Schema s4 = registry.register(n4.type(), new SchemaImpl());
        assertEquals("#/components/schemas/NestableStringSuperInteger", s4.getRef());
    }

    @Test
    public void testWildcardUpperBoundName() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Container.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Nestable.class");
        Index index = indexer.complete();

        OpenAPIImpl oai = new OpenAPIImpl();
        SchemaRegistry registry = SchemaRegistry.newInstance(emptyConfig(), oai, index);

        DotName cName = componentize(Container.class.getName());
        ClassInfo cInfo = index.getClassByName(cName);

        FieldInfo n5 = cInfo.field("n5");
        Schema s5 = registry.register(n5.type(), new SchemaImpl());
        assertEquals("#/components/schemas/NestableExtendsCharSequenceExtendsNumber", s5.getRef());
    }

    @Test
    public void testWildcardWithGivenName() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Container.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Nestable.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$NamedNestable.class");
        Index index = indexer.complete();

        OpenAPIImpl oai = new OpenAPIImpl();
        SchemaRegistry registry = SchemaRegistry.newInstance(emptyConfig(), oai, index);

        DotName cName = componentize(Container.class.getName());
        ClassInfo cInfo = index.getClassByName(cName);

        FieldInfo n6 = cInfo.field("n6");
        Schema s6 = registry.register(n6.type(), new SchemaImpl());
        assertEquals("#/components/schemas/n6", s6.getRef());
    }

    @Test
    public void testNestedGenericWildcard() throws IOException, JSONException {
        Indexer indexer = new Indexer();
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Container.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$Nestable.class");
        index(indexer, "io/smallrye/openapi/runtime/scanner/SchemaRegistryTests$NamedNestable.class");
        Index index = indexer.complete();

        OpenAPIImpl oai = new OpenAPIImpl();
        SchemaRegistry registry = SchemaRegistry.newInstance(nestingSupportConfig(), oai, index);

        DotName cName = componentize(Container.class.getName());
        ClassInfo cInfo = index.getClassByName(cName);

        Type n6Type = cInfo.field("n6").type();
        OpenApiDataObjectScanner scanner = new OpenApiDataObjectScanner(index, n6Type);

        Schema result = scanner.process();
        registry.register(n6Type, result);
        printToConsole(oai);

        String field3SchemaName = ModelUtil.nameFromRef(result.getProperties().get("field3").getRef());
        String field2SchemaName = oai.getComponents()
                .getSchemas()
                .get(field3SchemaName)
                .getProperties()
                .get("field2")
                .getRef();

        assertEquals("#/components/schemas/NestableExtendsNestable", field2SchemaName);
    }

    public static class Container {
        Nestable<String, Nestable<String, String>> n1;
        Nestable<String, Nestable<String, Object>> n2;
        Nestable<String, Nestable<String, Nestable<String, Object>>> n3;
        Nestable<String, ? super Integer> n4;
        Nestable<? extends CharSequence, ? extends Number> n5;
        NamedNestable<? extends CharSequence, ? extends Number> n6;
    }

    public static class Nestable<T1, T2> {
        T1 field1;
        T2 field2;
    }

    @org.eclipse.microprofile.openapi.annotations.media.Schema(name = "n6")
    public static class NamedNestable<T1, T2> {
        T1 field1;
        T2 field2;
        Nestable<String, ? extends Nestable<T1, T2>> field3;
    }
}
