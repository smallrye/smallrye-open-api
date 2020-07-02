package io.smallrye.openapi.tck.extra.jsonignoreproperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class JsonIgnorePropertiesUpstreamBehaviourTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDirectAnnotation() throws JsonProcessingException {
        List<String> fieldNames = getFieldNamesAfterJackson(new JsonIgnorePropertiesResource.DirectIgnore());
        assertThat(fieldNames, hasItem("ignoreMeNested"));
        assertThat(fieldNames, hasItem("dontIgnoreMe"));
        assertThat(fieldNames, not(hasItem("ignoreMe")));
    }

    @Test
    public void testInheritedAnnotation() throws JsonProcessingException {
        List<String> fieldNames = getFieldNamesAfterJackson(new JsonIgnorePropertiesResource.InheritIgnore());
        assertThat(fieldNames, hasItem("ignoreMeNested"));
        assertThat(fieldNames, hasItem("dontIgnoreMe"));
        assertThat(fieldNames, not(hasItem("ignoreMe")));
    }

    @Test
    public void testInheritedAnnotationThirdLevel() throws JsonProcessingException {
        List<String> fieldNames = getFieldNamesAfterJackson(new JsonIgnorePropertiesResource.ThirdLevelIgnore());
        assertThat(fieldNames, hasItem("ignoreMeNested"));
        assertThat(fieldNames, hasItem("dontIgnoreMe"));
        assertThat(fieldNames, not(hasItem("ignoreMe")));
    }

    @Test
    public void testInheritedAnnotationOverride() throws JsonProcessingException {
        List<String> fieldNames = getFieldNamesAfterJackson(new JsonIgnorePropertiesResource.InheritIgnoreOverride());
        assertThat(fieldNames, hasItem("ignoreMeNested"));
        assertThat(fieldNames, not(hasItem("dontIgnoreMe")));
        assertThat(fieldNames, hasItem("ignoreMe"));
    }

    @Test
    public void testInheritedAnnotationNestedOverride() throws JsonProcessingException {
        List<String> fieldNames = getFieldNamesAfterJackson(new JsonIgnorePropertiesResource.NestedOverride(), "nested");
        assertThat(fieldNames, not(hasItem("ignoreMeNested")));
        assertThat(fieldNames, hasItem("dontIgnoreMe"));
        assertThat(fieldNames, not(hasItem("ignoreMe")));
    }

    private List<String> getFieldNamesAfterJackson(Object input) throws JsonProcessingException {
        final String value = mapper.writeValueAsString(input);
        final JsonNode jsonNode = mapper.readTree(value);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(jsonNode.fieldNames(), 0), false)
                .collect(Collectors.toList());
    }

    private List<String> getFieldNamesAfterJackson(Object input, String property) throws JsonProcessingException {
        final String value = mapper.writeValueAsString(input);
        final JsonNode jsonNode = mapper.readTree(value);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(jsonNode.get(property).fieldNames(), 0), false)
                .collect(Collectors.toList());
    }
}
