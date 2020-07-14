package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonNodeConverterTest {

    @Test
    public void shouldThrowErrorWhenValidationNotValidJsonContent() {
        //given
        String invalidJsonFormat = "{ \"someObject\":";

        //when
        //then
        assertThrows(IOException.class, () -> JsonNodeConverter.fromString(invalidJsonFormat));
    }

    @Test
    public void shouldReturnCorrectJsonNodeWhenJsonContentIsValid() throws IOException {
        //given
        String jsonContent = "{ \"someObject\": true }";
        JsonNode expectedJsonNode = new ObjectMapper().readTree(jsonContent);

        //then
        assertEquals(expectedJsonNode, JsonNodeConverter.fromString(jsonContent));
    }
}