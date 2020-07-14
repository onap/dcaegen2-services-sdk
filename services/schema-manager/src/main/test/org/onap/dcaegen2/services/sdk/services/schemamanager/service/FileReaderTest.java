package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.NoSuchFileException;

import static org.junit.jupiter.api.Assertions.*;

class FileReaderTest {



    @Test
    public void shouldReturnEmptyStringWhenFileNotFound() {
        //when
        String expected = "";
        String actualContent = FileReader.readFileAsString("dummyFileName");

        //then
        assertEquals(expected, actualContent);


    }

    @Test
    public void shouldReturnFileContentWhenFileExist() {
        //when
        String exampleJson = "{\n" +
                "   \"someObject\":\"dummyValue\"\n" +
                "}";
        String actualContent = FileReader.readFileAsString("src/main/test/resources/file_with_one_line.json");

        //then
        assertEquals(exampleJson, actualContent);

    }

    @Test
    public void shouldReturnFalseWhenFileDoesNotExist(){
        //when
        boolean fileExists = FileReader.doesFileExists("dummyFileName");

        //then
        assertFalse(fileExists);
    }

    @Test
    public void shouldReturnTrueWhenFileDoesExist(){
        //when
        boolean fileExists = FileReader.doesFileExists("src/main/test/resources/file_with_one_line.json");

        //then
        assertTrue(fileExists);
    }


}