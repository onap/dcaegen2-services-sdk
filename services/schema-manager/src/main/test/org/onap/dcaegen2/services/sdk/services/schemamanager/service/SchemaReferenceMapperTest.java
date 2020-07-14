package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaReferenceMapperTest {

    @Test
    public void shouldNotThrowErrorWhenReferenceWithoutHash() {
        //given
        String publicUrlWithoutHash = "http://someExternalUrl/external";
        String mappingFilePath = "src/main/test/resources/schema-map-to-tests.json";
        String schemasPath = "src/main/test/resources";
        UrlMapper urlMapper = UrlMapper.getInstance(mappingFilePath, schemasPath);
        SchemaReferenceMapper schemaReferenceMapper = new SchemaReferenceMapper(urlMapper, schemasPath);

        //when
        String expected = "file_with_one_line.json";
        String localReferenceWithoutHash = schemaReferenceMapper.mapToLocalSchema(publicUrlWithoutHash);

        //then
        assertEquals(expected, localReferenceWithoutHash);


    }

    @Test
    public void shouldReturnLocalPathWithHasWhenPublicReferenceContainsHash() {
        //given
        String publicUrlWithHash = "http://someExternalUrl/external#someString";
        String mappingFilePath = "src/main/test/resources/schema-map-to-tests.json";
        String schemasPath = "src/main/test/resources";
        UrlMapper urlMapper = UrlMapper.getInstance(mappingFilePath, schemasPath);
        SchemaReferenceMapper schemaReferenceMapper = new SchemaReferenceMapper(urlMapper, schemasPath);

        //when
        String expected = schemasPath + "/file_with_one_line.json#/someString";
        String localReferenceWithoutHash = schemaReferenceMapper.mapToLocalSchema(publicUrlWithHash);

        //then
        assertEquals(expected, localReferenceWithoutHash);
    }
}