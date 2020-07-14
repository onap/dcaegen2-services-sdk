package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSplitterTest {

    public static final String VALID_JSON_CONTENT = "{\n" +
            "   \"validObject\":{\n" +
            "      \"someEvent\":{\n" +
            "         \"someObject\":true\n" +
            "      }\n" +
            "   }\n" +
            "}";

    @Test
    public void shouldReturnJsonFragmentAtValidPath() throws IOException {
        //given
        JsonNode jsonContent = new ObjectMapper().readTree(VALID_JSON_CONTENT);
        JsonNode expectedJsonNode = new ObjectMapper().readTree("true");
        String validPath = "/validObject/someEvent/someObject";

        //when
        JsonNode actualJsonNode = JsonSplitter.getFragment(jsonContent, validPath);

        //then
        assertEquals(expectedJsonNode, actualJsonNode);


    }

    @Test
    public void shouldThrowErrorWhenPathDoesNotExistInJsonContent() throws IOException {
        //given
        JsonNode jsonContent = new ObjectMapper().readTree(VALID_JSON_CONTENT);
        String dummyPath = "dummyPath";

        //when
        //then
        assertThrows(IllegalArgumentException.class, () -> JsonSplitter.getFragment(jsonContent, dummyPath));
    }
}