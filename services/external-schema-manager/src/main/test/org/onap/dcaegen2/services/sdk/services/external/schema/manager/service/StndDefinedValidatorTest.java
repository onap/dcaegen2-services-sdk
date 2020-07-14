/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.sdk.services.external.schema.manager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.external.schema.manager.exception.IncorrectInternalFileReferenceException;
import org.onap.dcaegen2.services.sdk.services.external.schema.manager.exception.NoLocalReferenceException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StndDefinedValidatorTest {

    private static final String TEST_RESOURCES = "src/main/test/resources/";
    private static final String MAPPING_FILE_PATH = TEST_RESOURCES + "externalRepo/schema-map.json";
    private static final String SCHEMAS_PATH = TEST_RESOURCES + "externalRepo";

    private static final String VALID_EVENT_PATH = TEST_RESOURCES + "externalRepo/validEvent.json";
    private static final String INVALID_EVENT_PATH = TEST_RESOURCES + "externalRepo/invalidEvent.json";
    private static final String VALID_NO_HASH_EVENT_PATH = TEST_RESOURCES + "externalRepo/validNoHashEvent.json";
    private static final String INCORRECT_INTERNAL_REF_EVENT_PATH = TEST_RESOURCES + "externalRepo/incorrectHashEvent.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StndDefinedValidator validator = new StndDefinedValidator.ValidatorBuilder()
            .mappingFilePath(MAPPING_FILE_PATH)
            .schemasPath(SCHEMAS_PATH)
            .build();

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnTrueWhenValidEventIsGiven()
            throws IOException {
        //given
        FileReader fileReader = new FileReader(VALID_EVENT_PATH);
        JsonNode validEventNode = objectMapper.readTree(fileReader.readFile());

        //when
        boolean validationResult = validator.validate(validEventNode);

        //then
        assertTrue(validationResult);
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnFalseWhenInvalidEventIsGiven()
            throws IOException {
        //given
        FileReader fileReader = new FileReader(INVALID_EVENT_PATH);
        JsonNode invalidEventNode = objectMapper.readTree(fileReader.readFile());

        //when
        boolean validationResult = validator.validate(invalidEventNode);

        //then
        assertFalse(validationResult);
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnTrueWhenValidSchemaReferenceHasNoHash()
            throws IOException {
        //given
        FileReader fileReader = new FileReader(VALID_NO_HASH_EVENT_PATH);
        JsonNode validEventNode = objectMapper.readTree(fileReader.readFile());

        //when
        boolean validationResult = validator.validate(validEventNode);

        //then
        assertTrue(validationResult);
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnExceptionWhenEventReferToNotExistingLocalSchema()
            throws IOException {
        //given
        String noLocalResourceMappingFilePath = TEST_RESOURCES + "externalRepo/schema-map-no-local-resource.json";
        StndDefinedValidator validator = getValidator(noLocalResourceMappingFilePath);
        FileReader fileReader = new FileReader(VALID_EVENT_PATH);
        JsonNode validEventNode = objectMapper.readTree(fileReader.readFile());

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> validator.validate(validEventNode));
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnExceptionWhenEventReferToEmptySchema()
            throws IOException {
        //given
        String noLocalResourceMappingFilePath = TEST_RESOURCES + "externalRepo/schema-map-empty-content.json";
        StndDefinedValidator validator = getValidator(noLocalResourceMappingFilePath);
        FileReader fileReader = new FileReader(VALID_EVENT_PATH);
        JsonNode validEventNode = objectMapper.readTree(fileReader.readFile());

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> validator.validate(validEventNode));
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnExceptionWhenEventReferToIncorrectYamlFormatSchema()
            throws IOException {
        //given
        String noLocalResourceMappingFilePath = TEST_RESOURCES + "externalRepo/schema-map-incorrect-yaml-format.json";
        StndDefinedValidator validator = getValidator(noLocalResourceMappingFilePath);
        JsonNode validEventNode = objectMapper.readTree(new FileReader(VALID_EVENT_PATH).readFile());

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> validator.validate(validEventNode));
    }

    @Test
    void shouldValidateStndDefinedFieldsInEventAndReturnExceptionWhenEventReferToIncorrectInternalFileReference()
            throws IOException {
        //given
        JsonNode validEventNode = objectMapper.readTree(new FileReader(INCORRECT_INTERNAL_REF_EVENT_PATH).readFile());

        //when
        //then
        assertThrows(IncorrectInternalFileReferenceException.class, () -> validator.validate(validEventNode));
    }


    private StndDefinedValidator getValidator(String noLocalResourceMappingFilePath) {
        return new StndDefinedValidator.ValidatorBuilder()
                .mappingFilePath(noLocalResourceMappingFilePath)
                .schemasPath(SCHEMAS_PATH)
                .build();
    }
}