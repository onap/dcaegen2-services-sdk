package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JsonSplitterTest {

    @Test
    public void shouldReturnJsonFragmentAtValidPath() throws IOException {
        //given
        String validJsonContent = "{\n" +
                "   \"validObject\":{\n" +
                "      \"someEvent\":{\n" +
                "         \"someObject\":true\n" +
                "      }\n" +
                "   }\n" +
                "}";
        JsonNode jsonContent = new ObjectMapper().readTree(validJsonContent);

        //when
        JsonNode expectedJsonFragment = new ObjectMapper().readTree("true");
        String validPath = "/validObject/someEvent/someObject";

        //then
        assertEquals(expectedJsonFragment, JsonSplitter.getFragment(jsonContent, validPath));


    }

    @Test
    public void shouldThrowErrorWhenPathDoesNotExistInContent() throws IOException {
        //given
        String validJsonContent = "{\n" +
                "   \"validObject\":{\n" +
                "      \"someEvent\":{\n" +
                "         \"someObject\":true\n" +
                "      }\n" +
                "   }\n" +
                "}";
        JsonNode jsonContent = new ObjectMapper().readTree(validJsonContent);

        //when
        String dummyPath = "dummyPath";

        //then
        assertThrows(IllegalArgumentException.class, () -> JsonSplitter.getFragment(jsonContent, dummyPath));
    }

}