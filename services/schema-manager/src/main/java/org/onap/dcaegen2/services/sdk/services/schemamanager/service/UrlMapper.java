/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UrlMapper {

    private static final Logger LOG = LoggerFactory.getLogger("UrlMapper");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Map<String, String> urlCache;

    private UrlMapper(Map<String, String> mappings) {
        this.urlCache = Map.copyOf(mappings);
    }

    //todo handle exception when file is not present
    public static UrlMapper getInstance(String filepath) {
        Map<String, String> mappings = new HashMap<>();
        try {
            for (JsonNode mapping : OBJECT_MAPPER.readTree(FileReader.readFileAsString(filepath))) {
                mappings.put(mapping.get("publicURL").asText(), mapping.get("localURL").asText());
            }
        } catch (IOException ex) {
            LOG.error("Unable to read JMRI resources for JSON schema mapping", ex);
        }
        return new UrlMapper(mappings);
    }

    public String getLocalUrl(String externalUrl) {
        //todo when externalUrl is not present
        return urlCache.get(externalUrl);
    }
}