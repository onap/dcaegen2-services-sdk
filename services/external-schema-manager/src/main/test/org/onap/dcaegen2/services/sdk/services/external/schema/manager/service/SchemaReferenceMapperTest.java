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

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.external.schema.manager.model.SchemaReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaReferenceMapperTest {

    public static final String SCHEMAS_PATH = "src/main/test/resources";

    @Test
    public void shouldReturnProperReferenceWhenSchemaReferenceHasNoHash() {
        //given
        SchemaReferenceMapper schemaReferenceMapper = getSchemaReferenceMapper();
        String publicUrlWithoutHash = "http://someExternalUrl/external";
        SchemaReferenceResolver schemaReferenceResolver = new SchemaReferenceResolver(publicUrlWithoutHash);
        SchemaReference schemaReference = new SchemaReference(schemaReferenceResolver);

        String expectedReference = SCHEMAS_PATH + "/file_with_one_line.json#/";

        //when
        String actualReference = schemaReferenceMapper.mapToLocalSchema(schemaReference).getFullSchemaReference();

        //then
        assertEquals(expectedReference, actualReference);
    }

    @Test
    public void shouldReturnProperReferenceWhenSchemaReferenceContainsHash() {
        //given
        SchemaReferenceMapper schemaReferenceMapper = getSchemaReferenceMapper();
        String publicUrlWithHash = "http://someExternalUrl/external#someString";
        SchemaReferenceResolver schemaReferenceResolver = new SchemaReferenceResolver(publicUrlWithHash);
        SchemaReference schemaReference = new SchemaReference(schemaReferenceResolver);
        String expectedReference = SCHEMAS_PATH + "/file_with_one_line.json#/someString";

        //when
        String actualReference = schemaReferenceMapper.mapToLocalSchema(schemaReference).getFullSchemaReference();

        //then
        assertEquals(expectedReference, actualReference);
    }

    private SchemaReferenceMapper getSchemaReferenceMapper() {
        String mappingFilePath = "src/main/test/resources/schema-map-to-tests.json";
        UrlMapper urlMapper = new UrlMapperFactory().getUrlMapper(mappingFilePath, SCHEMAS_PATH);
        return new SchemaReferenceMapper(urlMapper, SCHEMAS_PATH);
    }
}