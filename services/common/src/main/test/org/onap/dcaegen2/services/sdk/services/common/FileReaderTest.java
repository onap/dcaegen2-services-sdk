/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2020-2021 Nokia. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.services.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileReaderTest {

    public static final String TEST_RESOURCES = "src/main/test/resources/";
    public static final String DUMMY_FILE_PATH = "dummyFileName";

    @Test
    void shouldReturnEmptyStringWhenFileNotFound() {
        //given
        String expectedContent = "";

        //when
        String actualContent = new FileReader(DUMMY_FILE_PATH).getContent();

        //then
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void shouldReturnFileContentWhenFileExists() {
        //given
        String expectedContent = "{\n" +
                "  \"someObject\": \"dummyValue\"\n" +
                "}";
        String filePath = TEST_RESOURCES + "file_with_one_line.json";

        //when
        String actualContent = new FileReader(filePath).getContent();

        //then
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void shouldReturnFalseWhenFileDoesNotExist() {
        //when
        boolean doesFileExists = new FileReader(DUMMY_FILE_PATH).doesFileExists();

        //then
        assertFalse(doesFileExists);
    }

    @Test
    void shouldReturnTrueWhenFileExists() {
        //when
        boolean doesFileExists = new FileReader(TEST_RESOURCES + "file_with_one_line.json").doesFileExists();

        //then
        assertTrue(doesFileExists);
    }
}
