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

package org.onap.dcaegen2.services.sdk.services.externalschemamanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

final class FileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileReader.class);

    private FileReader() {}

    static String readFileAsString(String filename) {
        String fileContent = "";
        try {
            fileContent = getFileContent(filename);
        } catch (IOException e) {
            LOGGER.error("Error while reading file. Filename: {}", filename);
        }
        return fileContent;
    }

    private static String getFileContent(String filename) throws IOException {
        return new String(readBytes(filename));
    }

    static boolean doesFileExists(String filename) {
        return new File(filename).exists();
    }

    private static byte[] readBytes(String filename) throws IOException {
        return Files.readAllBytes(Paths.get(filename));
    }
}