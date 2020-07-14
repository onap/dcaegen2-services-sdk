package org.onap.dcaegen2.services.sdk.services.schemamanager;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.FileReader;
import org.openapi4j.core.exception.ResolutionException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatorTest {

    private static final String TEST_RESOURCES = "src/main/test/resources/";
    public static final String MAPPING_FILE_PATH = TEST_RESOURCES + "externalRepo/schema-map.json";
    public static final String SCHEMAS_PATH = TEST_RESOURCES + "externalRepo";

    private static final String VALID_EVENT_PATH = TEST_RESOURCES + "externalRepo/validEvent.json";
    private static final String INVALID_EVENT_PATH = TEST_RESOURCES + "externalRepo/invalidEvent.json";
    private static final String VALID_NO_HASH_EVENT_PATH = TEST_RESOURCES + "externalRepo/validNoHashEvent.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator = new Validator.ValidatorBuilder()
            .mappingFilePath(MAPPING_FILE_PATH)
            .schemasPath(SCHEMAS_PATH)
            .build();

    //todo test event with schemaReference without #
    //todo test event with schemaReference to incorrect file
    //todo test event with schemaReference to correct file but incorrect internal reference
    //todo test event with schemaReference to correct yaml but incorrect OpenApi schema

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnTrueWhenValidEventIsGiven()
            throws ResolutionException, IOException {
        //given
        JsonNode validEventNode = objectMapper.readTree(FileReader.readFileAsString(VALID_EVENT_PATH));

        //when
        boolean validationResult = validator.validate(validEventNode);

        //then
        assertTrue(validationResult);
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnFalseWhenInvalidEventIsGiven()
            throws ResolutionException, IOException {
        //given
        JsonNode invalidEventNode = objectMapper.readTree(FileReader.readFileAsString(INVALID_EVENT_PATH));

        //when
        boolean validationResult = validator.validate(invalidEventNode);

        //then
        assertFalse(validationResult);
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnTrueWhenValidSchemaReferenceHasNoHash()
            throws ResolutionException, IOException {
        //given
        JsonNode validEventNode = objectMapper.readTree(FileReader.readFileAsString(VALID_NO_HASH_EVENT_PATH));

        //when
        boolean validationResult = validator.validate(validEventNode);

        //then
        assertTrue(validationResult);
    }
}