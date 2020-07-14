package org.onap.dcaegen2.services.sdk.services.schemamanager;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidatorBuilderTest {

    private static final String TEST_RESOURCES = "src/main/test/resources/";

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