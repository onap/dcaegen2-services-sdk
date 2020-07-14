package org.onap.dcaegen2.services.sdk.services.schemamanager;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.schemamanager.exception.IncorrectFileInternalReference;
import org.onap.dcaegen2.services.sdk.services.schemamanager.exception.NoLocalReferenceException;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.FileReader;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatorTest {

    private static final String TEST_RESOURCES = "src/main/test/resources/";
    public static final String MAPPING_FILE_PATH = TEST_RESOURCES + "externalRepo/schema-map.json";
    public static final String SCHEMAS_PATH = TEST_RESOURCES + "externalRepo";

    private static final String VALID_EVENT_PATH = TEST_RESOURCES + "externalRepo/validEvent.json";
    private static final String INVALID_EVENT_PATH = TEST_RESOURCES + "externalRepo/invalidEvent.json";
    private static final String VALID_NO_HASH_EVENT_PATH = TEST_RESOURCES + "externalRepo/validNoHashEvent.json";
    private static final String INCORRECT_INTERNAL_REF_EVENT_PATH = TEST_RESOURCES + "externalRepo/incorrectHashEvent.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator = new Validator.ValidatorBuilder()
            .mappingFilePath(MAPPING_FILE_PATH)
            .schemasPath(SCHEMAS_PATH)
            .build();

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnTrueWhenValidEventIsGiven()
            throws IOException {
        //given
        JsonNode validEventNode = objectMapper.readTree(FileReader.readFileAsString(VALID_EVENT_PATH));

        //when
        boolean validationResult = validator.validate(validEventNode);

        //then
        assertTrue(validationResult);
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnFalseWhenInvalidEventIsGiven()
            throws IOException {
        //given
        JsonNode invalidEventNode = objectMapper.readTree(FileReader.readFileAsString(INVALID_EVENT_PATH));

        //when
        boolean validationResult = validator.validate(invalidEventNode);

        //then
        assertFalse(validationResult);
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnTrueWhenValidSchemaReferenceHasNoHash()
            throws IOException {
        //given
        JsonNode validEventNode = objectMapper.readTree(FileReader.readFileAsString(VALID_NO_HASH_EVENT_PATH));

        //when
        boolean validationResult = validator.validate(validEventNode);

        //then
        assertTrue(validationResult);
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnExceptionWhenEventReferToNotExistentLocalSchema()
            throws IOException {
        //given
        String noLocalResourceMappingFilePath = TEST_RESOURCES + "externalRepo/schema-map-no-local-resource.json";
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(noLocalResourceMappingFilePath)
                .schemasPath(SCHEMAS_PATH)
                .build();
        JsonNode validEventNode = objectMapper.readTree(FileReader.readFileAsString(VALID_EVENT_PATH));

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> validator.validate(validEventNode));
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnExceptionWhenEventReferToEmptySchema()
            throws IOException {
        //given
        String noLocalResourceMappingFilePath = TEST_RESOURCES + "externalRepo/schema-map-empty-content.json";
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(noLocalResourceMappingFilePath)
                .schemasPath(SCHEMAS_PATH)
                .build();
        JsonNode validEventNode = objectMapper.readTree(FileReader.readFileAsString(VALID_EVENT_PATH));

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> validator.validate(validEventNode));
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnExceptionWhenEventReferToIncorrectYamlFormatSchema()
            throws IOException {
        //given
        String noLocalResourceMappingFilePath = TEST_RESOURCES + "externalRepo/schema-map-incorrect-yaml-format.json";
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(noLocalResourceMappingFilePath)
                .schemasPath(SCHEMAS_PATH)
                .build();
        JsonNode validEventNode = objectMapper.readTree(FileReader.readFileAsString(VALID_EVENT_PATH));

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> validator.validate(validEventNode));
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnExceptionWhenEventReferToIncorrectInternalFileReference()
            throws IOException {
        //given
        JsonNode validEventNode = objectMapper.readTree(FileReader.readFileAsString(INCORRECT_INTERNAL_REF_EVENT_PATH));

        //when
        //then
        assertThrows(IncorrectFileInternalReference.class, () -> validator.validate(validEventNode));
    }

}