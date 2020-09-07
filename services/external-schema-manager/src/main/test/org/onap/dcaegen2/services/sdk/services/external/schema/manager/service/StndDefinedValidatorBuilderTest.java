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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StndDefinedValidatorBuilderTest {

    private static final String TEST_RESOURCES = "src/main/test/resources/externalRepo/";

    @Test
    void shouldGenerateValidatorWithAllSchemaMappings() {
        //when
        StndDefinedEventValidator validator = getValidator("schema-map.json");

        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache).hasSize(6);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWithReferenceToNotExistingLocalResource() {
        //when
        StndDefinedEventValidator validator = getValidator("schema-map-no-local-resource.json");
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache).hasSize(5);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWithEmptyLocalFileContent() {
        //when
        StndDefinedEventValidator validator = getValidator("schema-map-empty-content.json");
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache).hasSize(4);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWithIncorrectYamlFormat() {
        //when
        StndDefinedEventValidator validator = getValidator("schema-map-incorrect-yaml-format.json");
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache).hasSize(3);
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWhenSchemaMappingFileHasNotJsonFormat() {
        //when
        StndDefinedEventValidator validator = getValidator("schema-map-no-json-format.json");
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isZero();
    }

    @Test
    void shouldGenerateValidatorWithoutSchemaMappingsWhenSchemaMappingFileHasWrongFieldName() {
        //when
        StndDefinedEventValidator validator = getValidator("schema-map-wrong-field-name.json");
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isZero();
    }

    private StndDefinedEventValidator getValidator(String mappingFilePath) {
        return new StndDefinedEventValidator.EventValidatorBuilder()
                .mappingFilePath(TEST_RESOURCES + mappingFilePath)
                .schemasPath(TEST_RESOURCES)
                .schemaRefPath("/event/stndDefinedFields/schemaReference")
                .stndDefinedDataPath("/event/stndDefinedFields/data")
                .build();
    }

    private Map<String, String> getMappingsCache(StndDefinedEventValidator validator) {
        return validator.getValidatorCache()
                .getSchemaReferenceMapper()
                .getUrlMapper()
                .getMappingsCache();
    }
}
