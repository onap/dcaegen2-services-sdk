package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.schemamanager.exception.NoLocalReferenceException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlMapperTest {
    private static final String MAPPING_FILE_PATH = "src/main/test/resources/schema-map-to-tests.json";
    private static final String SCHEMAS_PATH = "src/main/test/resources";
    private static UrlMapper urlMapper;

    @BeforeAll
    public static void setUp() {
        urlMapper = UrlMapper.getInstance(MAPPING_FILE_PATH, SCHEMAS_PATH);
    }

    @Test
    public void shouldThrowExceptionWhenNoMappingExists() {
        //given
        String notMappedPublicUrl = "http://localhost:8080/notExisting";

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(notMappedPublicUrl));
    }

    @Test
    public void shouldThrowExceptionWhenLocalSchemaFileIsEmpty() {
        //given
        String publicUrlToEmptyLocal = "http://someExternalUrl/emptySchema";

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(publicUrlToEmptyLocal));
    }

    @Test
    public void shouldThrowExceptionWhenFileHasInvalidYamlStructure() {
        //given
        String publicUrlToInvalidYamlLocal = "http://someExternalUrl/invalidYamlFile";

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(publicUrlToInvalidYamlLocal));
    }

    @Test
    public void shouldThrowExceptionWhenLocalFileDoesNotExist() {
        //given
        String publicUrlToNotExistingLocalFile = "http://someExternalUrl/missingFile";

        //when
        //then
        assertThrows(NoLocalReferenceException.class, () -> urlMapper.mapToLocalUrl(publicUrlToNotExistingLocalFile));
    }

    @Test
    public void shouldReturnLocalUrlWhenFileValidAndFound() {
        //given
        String publicUrl = "http://someExternalUrl/external";

        //when
        //then
        assertEquals("file_with_one_line.json", urlMapper.mapToLocalUrl(publicUrl));
    }

    @Test
    public void shouldNotThrowExceptionWhenMappingFileDoesNotExist() {
        String invalidMappingFilePath = "src/main/test/resources/missing-schema.json";

        assertDoesNotThrow(() -> UrlMapper.getInstance(invalidMappingFilePath, SCHEMAS_PATH));
    }
}