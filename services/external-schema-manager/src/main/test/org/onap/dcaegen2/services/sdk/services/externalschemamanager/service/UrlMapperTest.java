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

package org.onap.dcaegen2.services.sdk.services.externalschemamanager.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.externalschemamanager.exception.NoLocalReferenceException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlMapperTest {
    private static final String MAPPING_FILE_PATH = "src/main/test/resources/schema-map-to-tests.json";
    private static final String SCHEMAS_PATH = "src/main/test/resources";
    private static UrlMapper urlMapper;

    @BeforeAll
    public static void setUp() {
        urlMapper = new UrlMapperFactory().getInstance(MAPPING_FILE_PATH, SCHEMAS_PATH);
    }

    @Test
    public void shouldThrowExceptionWhenNoMappingExists() {
        //given
        String notMappedPublicUrl = "http://localhost:8080/notExisting";

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(notMappedPublicUrl));
    }

    @Test
    public void shouldThrowExceptionWhenLocalSchemaFileIsEmpty() {
        //given
        String publicUrlToEmptyLocal = "http://someExternalUrl/emptySchema";

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(publicUrlToEmptyLocal));
    }

    @Test
    public void shouldThrowExceptionWhenFileHasInvalidYamlStructure() {
        //given
        String publicUrlToInvalidYamlLocal = "http://someExternalUrl/invalidYamlFile";

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(publicUrlToInvalidYamlLocal));
    }

    @Test
    public void shouldThrowExceptionWhenLocalFileDoesNotExist() {
        //given
        String publicUrlToNotExistingLocalFile = "http://someExternalUrl/missingFile";

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(publicUrlToNotExistingLocalFile));
    }

    @Test
    public void shouldReturnLocalUrlWhenFileValidAndFound() {
        //given
        String publicUrl = "http://someExternalUrl/external";

        //when
        //then
        assertEquals("file_with_one_line.json", urlMapper.mapToLocalUrl(publicUrl));
    }

    @Test
    public void shouldNotThrowExceptionWhenMappingFileDoesNotExist() {
        String invalidMappingFilePath = "src/main/test/resources/missing-schema.json";

        assertDoesNotThrow(() -> new UrlMapperFactory().getInstance(invalidMappingFilePath, SCHEMAS_PATH));
    }
}