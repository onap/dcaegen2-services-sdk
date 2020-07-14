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

package org.onap.dcaegen2.services.sdk.services.externalschemamanager;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidatorBuilderTest {

    private static final String TEST_RESOURCES = "src/main/test/resources/";

    @Test
    void shouldCheckIfBuilderBuildsFieldsCorrectly() {
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(TEST_RESOURCES + "externalRepo/schema-map.json")
                .schemasPath(TEST_RESOURCES + "externalRepo")
                .schemaRefPath("/event/stndDefinedFields/schemaReference")
                .stndDefinedDataPath("/event/stndDefinedFields/data")
                .build();
    }

    @Test
    void shouldGenerateValidatorWithAllSchemaMappings() {
        //when
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(TEST_RESOURCES + "externalRepo/schema-map.json")
                .schemasPath(TEST_RESOURCES + "externalRepo")
                .build();
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isEqualTo(6);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWithReferenceToNotExistingLocalResource() {
        //when
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(TEST_RESOURCES + "externalRepo/schema-map-no-local-resource.json")
                .schemasPath(TEST_RESOURCES + "externalRepo")
                .build();
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isEqualTo(5);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWithEmptyLocalFileContent() {
        //when
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(TEST_RESOURCES + "externalRepo/schema-map-empty-content.json")
                .schemasPath(TEST_RESOURCES + "externalRepo")
                .build();
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isEqualTo(4);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWithIncorrectYamlFormat() {
        //when
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(TEST_RESOURCES + "externalRepo/schema-map-incorrect-yaml-format.json")
                .schemasPath(TEST_RESOURCES + "externalRepo")
                .build();
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isEqualTo(3);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWhenSchemaMappingFileHasNotJsonFormat() {
        //when
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(TEST_RESOURCES + "externalRepo/schema-map-no-json-format.json")
                .schemasPath(TEST_RESOURCES + "externalRepo")
                .build();
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isEqualTo(0);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWhenSchemaMappingFileHasWrongFieldName() {
        //when
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath(TEST_RESOURCES + "externalRepo/schema-map-wrong-field-name.json")
                .schemasPath(TEST_RESOURCES + "externalRepo")
                .build();
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isEqualTo(0);
    }

    private Map<String, String> getMappingsCache(Validator validator) {
        return validator.getSchemaReferenceMapper().getUrlMapper().getMappingsCache();
    }
}