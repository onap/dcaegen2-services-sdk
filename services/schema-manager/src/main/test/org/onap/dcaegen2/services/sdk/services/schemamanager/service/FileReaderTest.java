package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileReaderTest {

    public static final String TEST_RESOURCES = "src/main/test/resources/";

    @Test
    public void shouldReturnEmptyStringWhenFileNotFound() {
        //given
        String expectedContent = "";

        //when
        String actualContent = FileReader.readFileAsString("dummyFileName");

        //then
        assertEquals(expectedContent, actualContent);
    }

    @Test
    public void shouldReturnFileContentWhenFileExists() {
        //given
        String expectedContent = "{\n" +
                "   \"someObject\":\"dummyValue\"\n" +
                "}";

        //when
        String actualContent = FileReader.readFileAsString(TEST_RESOURCES + "file_with_one_line.json");

        //then
        assertEquals(expectedContent, actualContent);
    }

    @Test
    public void shouldReturnFalseWhenFileDoesNotExist() {
        //when
        boolean doesFileExists = FileReader.doesFileExists("dummyFileName");

        //then
        assertFalse(doesFileExists);
    }

    @Test
    public void shouldReturnTrueWhenFileExists() {
        //when
        boolean doesFileExists = FileReader.doesFileExists(TEST_RESOURCES + "file_with_one_line.json");

        //then
        assertTrue(doesFileExists);
    }
}