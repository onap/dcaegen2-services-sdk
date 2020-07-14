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
        JsonNode actualSchemaReference = SchemaReferenceJsonGenerator.getSchemaReferenceJson(validUrl);

        //then
        assertEquals(expectedSchemaReference, actualSchemaReference);
    }

    @Test
    public void shouldThrowErrorWhenUrlIsInInvalidFormat() {
        //given
        String invalidFormatUrl = "\"someDummyValue\n\t";

        //when
        //then
        assertThrows(IOException.class, () -> SchemaReferenceJsonGenerator.getSchemaReferenceJson(invalidFormatUrl));
    }
}