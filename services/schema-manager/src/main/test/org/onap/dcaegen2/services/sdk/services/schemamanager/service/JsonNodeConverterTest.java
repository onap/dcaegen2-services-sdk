package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JsonNodeConverterTest {

    @Test
    public void shouldThrowErrorWhenNotValidJsonContent() {

        assertThrows(IOException.class, () -> {
            String dummyJsonContent = "{ \"someObject\":";
            JsonNodeConverter.fromString(dummyJsonContent);
        });
    }

    @Test
    public void shouldReturnJsonNodeWhenJsonContentIsValid() throws IOException {
        //given
        String validJsonContent = "{ \"someObject\": true }";

        //when
        JsonNode expected = new ObjectMapper().readTree(validJsonContent);

        //then
        assertEquals(expected, JsonNodeConverter.fromString(validJsonContent));
    }


}