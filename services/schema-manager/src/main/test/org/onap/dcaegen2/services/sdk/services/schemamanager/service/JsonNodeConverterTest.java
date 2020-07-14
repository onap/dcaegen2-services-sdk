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