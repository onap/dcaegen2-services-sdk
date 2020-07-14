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

package org.onap.dcaegen2.services.sdk.services.external.schema.manager.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UrlMapperFactory {

    private static final Logger logger = LoggerFactory.getLogger(UrlMapperFactory.class);

    UrlMapper getUrlMapper(String mappingFilePath, String schemasPath) {
        Map<String, String> mappings = new HashMap<>();
        try {
            mappings = readMappingFile(mappingFilePath, schemasPath);
        } catch (IOException ex) {
            logger.warn("Unable to read mapping file. Mapping file path: {}", mappingFilePath);
        } catch (NullPointerException ex) {
            mappings = Collections.emptyMap();
            logger.warn("Schema mapping file has incorrect format. Mapping file path: {}", mappingFilePath);
        }

        return new UrlMapper(mappings);
    }

    private Map<String, String> readMappingFile(String mappingFilePath, String schemasPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        FileReader fileReader = new FileReader(mappingFilePath);
        Map<String, String> mappings = new HashMap<>();

        for (JsonNode mapping : objectMapper.readTree(fileReader.readFile())) {
            String localURL = mapping.get("localURL").asText();
            if (isMappingValid(schemasPath, localURL)) {
                mappings.put(mapping.get("publicURL").asText(), localURL);
            } else {
                logger.warn("Mapping for publicURL ({}) will not be added to validator.", mapping.get("publicURL"));
            }
        }
        return mappings;
    }

    private boolean isMappingValid(String schemasPath, String localURL) throws IOException {
        String schemaRelativePath = schemasPath + File.separator + localURL;
        return doesLocalFileExist(schemaRelativePath) && isFileValidSchema(schemaRelativePath);
    }

    private boolean isFileValidSchema(String schemaRelativePath) throws IOException {
        String schemaContent = new FileReader(schemaRelativePath).readFile();
        return isNotEmpty(schemaContent, schemaRelativePath) && isYaml(schemaContent, schemaRelativePath);
    }

    private boolean isNotEmpty(String schemaContent, String schemaRelativePath) {
        if (schemaContent.isEmpty()) {
            logger.warn("Schema file is empty. Schema path: {}", schemaRelativePath);
            return false;
        }
        return true;
    }

    private boolean isYaml(String schemaContent, String schemaRelativePath) throws IOException {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
        try {
            yamlMapper.readTree(schemaContent);
        } catch (JsonParseException e) {
            logger.warn("Schema has incorrect YAML structure. Schema path: {}", schemaRelativePath);
            return false;
        }
        return true;
    }

    private boolean doesLocalFileExist(String schemaRelativePath) {
        FileReader fileReader = new FileReader(schemaRelativePath);
        if (!fileReader.doesFileExists()) {
            logger.warn("Local schema resource missing. Schema file with path {} has not been found.", schemaRelativePath);
            return false;
        }
        return true;
    }
}
