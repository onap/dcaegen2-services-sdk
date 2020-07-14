package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchemaReferenceJsonGeneratorTest {

    @Test
    public void shouldReturnValidSchemaReferenceWhenUrlIsInValidFormat() throws IOException {
        //given
        String validUrl = "src/main/test/resources/file_with_one_line.json";
        String schemaReferenceContent = "{\"$ref\":\"" + validUrl + "\"}";
        JsonNode expectedSchemaReference = new ObjectMapper().readTree(schemaReferenceContent);

        //when
        JsonNode actualSchemaReference = SchemaReferenceJsonGenerator.getSchemaReference(validUrl);

        //then
        assertEquals(expectedSchemaReference, actualSchemaReference);
    }

    @Test
    public void shouldThrowErrorWhenUrlIsInInvalidFormat() {
        //given
        String invalidFormatUrl = "\"someDummyValue\n\t";

        //when
        //then
        assertThrows(IOException.class, () -> SchemaReferenceJsonGenerator.getSchemaReference(invalidFormatUrl));
    }
}