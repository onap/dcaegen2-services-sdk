package org.onap.dcaegen2.services.sdk.services.schemamanager;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorBuilderTest {

    @Test
    void shouldGenerateValidatorWithAllSchemas() {
        //when
        Validator validator = new Validator.ValidatorBuilder()
                .mappingFilePath("etc/externalRepo/schema-map.json")
                .build();
        Map<String, String> mappingsCache = getMappingsCache(validator);

        //then
        assertThat(mappingsCache.size()).isEqualTo(6);
    }

    private Map<String, String> getMappingsCache(Validator validator) {
        return validator.getSchemaReferenceMapper().getUrlMapper().getMappingsCache();
    }
}