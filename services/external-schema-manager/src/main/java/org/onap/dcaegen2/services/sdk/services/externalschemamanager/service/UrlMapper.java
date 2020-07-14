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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.onap.dcaegen2.services.sdk.services.externalschemamanager.exception.NoLocalReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UrlMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger("UrlMapper");
    private final Map<String, String> mappingsCache;

    private UrlMapper(Map<String, String> mappings) {
        this.mappingsCache = Map.copyOf(mappings);
    }

    public Map<String, String> getMappingsCache() {
        return mappingsCache;
    }

    public String mapToLocalUrl(String publicUrl) {
        String externalUrl = mappingsCache.get(publicUrl);
        if (externalUrl == null) {
            throw new NoLocalReferenceException("Couldn't find mapping for public url. PublicURL: " + publicUrl);
        }
        return externalUrl;
    }

    public static UrlMapper getInstance(String mappingFilePath, String schemasPath) {
        Map<String, String> mappings = new HashMap<>();
        try {
            mappings = readMappingFile(mappingFilePath, schemasPath);
        } catch (IOException ex) {
            LOGGER.warn("Unable to read mapping file. Mapping file path: {}", mappingFilePath);
        } catch (NullPointerException ex) {
            mappings = Collections.emptyMap();
            LOGGER.warn("Schema mapping file has incorrect format. Mapping file path: {}", mappingFilePath);
        }

        return new UrlMapper(mappings);
    }

    private static Map<String, String> readMappingFile(String mappingFilePath, String schemasPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> mappings = new HashMap<>();
        for (JsonNode mapping : objectMapper.readTree(FileReader.readFileAsString(mappingFilePath))) {
            String localURL = mapping.get("localURL").asText();
            if (isMappingValid(schemasPath, localURL)) {
                mappings.put(mapping.get("publicURL").asText(), localURL);
            } else {
                LOGGER.warn("Mapping for publicURL ({}) will not be added to validator.", mapping.get("publicURL"));
            }
        }
        return mappings;
    }

    private static boolean isMappingValid(String schemasPath, String localURL) throws IOException {
        String schemaRelativePath = schemasPath + File.separator + localURL;
        return doesLocalFileExist(schemaRelativePath) && isFileValidSchema(schemaRelativePath);
    }

    private static boolean isFileValidSchema(String schemaRelativePath) throws IOException {
        String schemaContent = FileReader.readFileAsString(schemaRelativePath);
        return isNotEmpty(schemaContent, schemaRelativePath) && isYaml(schemaContent, schemaRelativePath);
    }

    private static boolean isNotEmpty(String schemaContent, String schemaRelativePath) {
        if (schemaContent.isEmpty()) {
            LOGGER.warn("Schema file is empty. Schema path: {}", schemaRelativePath);
            return false;
        }
        return true;
    }

    private static boolean isYaml(String schemaContent, String schemaRelativePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
        try {
            yamlMapper.readTree(schemaContent);
        } catch (JsonParseException e) {
            LOGGER.warn("Schema has incorrect YAML structure. Schema path: {}", schemaRelativePath);
            return false;
        }
        return true;
    }

    private static boolean doesLocalFileExist(String schemaRelativePath) {
        if (!FileReader.doesFileExists(schemaRelativePath)) {
            LOGGER.warn("Local schema resource missing. Schema file with path {} has not been found.", schemaRelativePath);
            return false;
        }
        return true;
    }
}