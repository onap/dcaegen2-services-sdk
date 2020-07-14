package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaReferenceMapperTest {

    public static final String SCHEMAS_PATH = "src/main/test/resources";

    @Test
    public void shouldReturnProperReferenceWhenSchemaReferenceHasNoHash() {
        //given
        SchemaReferenceMapper schemaReferenceMapper = getSchemaReferenceMapper();
        String publicUrlWithoutHash = "http://someExternalUrl/external";
        String expectedReference = SCHEMAS_PATH + "/file_with_one_line.json#/";

        //when
        String actualReference = schemaReferenceMapper.mapToLocalSchema(publicUrlWithoutHash);

        //then
        assertEquals(expectedReference, actualReference);
    }

    @Test
    public void shouldReturnProperReferenceWhenSchemaReferenceContainsHash() {
        //given
        SchemaReferenceMapper schemaReferenceMapper = getSchemaReferenceMapper();
        String publicUrlWithHash = "http://someExternalUrl/external#someString";
        String expectedReference = SCHEMAS_PATH + "/file_with_one_line.json#/someString";

        //when
        String actualReference = schemaReferenceMapper.mapToLocalSchema(publicUrlWithHash);

        //then
        assertEquals(expectedReference, actualReference);
    }

    private SchemaReferenceMapper getSchemaReferenceMapper() {
        String mappingFilePath = "src/main/test/resources/schema-map-to-tests.json";
        UrlMapper urlMapper = UrlMapper.getInstance(mappingFilePath, SCHEMAS_PATH);
        return new SchemaReferenceMapper(urlMapper, SCHEMAS_PATH);
    }
}