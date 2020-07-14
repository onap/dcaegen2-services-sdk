package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.schemamanager.exception.NoLocalReferenceException;

import static org.junit.jupiter.api.Assertions.*;

class UrlMapperTest {
    private static final String MAPPING_FILE_PATH = "src/main/test/resources/schema-map-to-tests.json";
    private static final String SCHEMAS_PATH = "src/main/test/resources";
    private static UrlMapper urlMapper;

    @BeforeAll
    public static void setUp() {
        urlMapper = UrlMapper.getInstance(MAPPING_FILE_PATH, SCHEMAS_PATH);
    }

    @Test
    public void shouldThrowErrorWhenNoLocalReferenceExist() {
        //when
        String dummyPublicUrl = "dummyPublicUrl";

        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(dummyPublicUrl));

    }

    @Test
    public void shouldThrowErrorWhenSchemaFileIsEmpty() {

        //when
        String dummyPublicUrl = "http://someExternalUrl/emptySchema";

        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(dummyPublicUrl));


    }

    @Test
    public void shouldThrowErrorWhenFileHasInvalidYamlStructure() {

        //when
        String dummyPublicUrl = "http://someExternalUrl/invalidYamlFile";

        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(dummyPublicUrl));

    }


    @Test
    public void shouldThrowErrorWhenFileDoesNotExist() {
        //when
        String dummyPublicUrl = "http://someExternalUrl/missingFile";

        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(dummyPublicUrl));

    }

    @Test
    public void shouldReturnLocalUrlWhenFileValidAndFound() {

        //when
        String validPublicUrl = "http://someExternalUrl/external";

        //then
        assertEquals("file_with_one_line.json", urlMapper.mapToLocalUrl(validPublicUrl));
    }


    @Test
    public void shouldNotThrowErrorWhenMappingFileDoesNotExist() {
        String invalidMappingFilePath = "src/main/test/resources/missing-schema.json";

        assertDoesNotThrow(()->UrlMapper.getInstance(invalidMappingFilePath, SCHEMAS_PATH));
    }


}