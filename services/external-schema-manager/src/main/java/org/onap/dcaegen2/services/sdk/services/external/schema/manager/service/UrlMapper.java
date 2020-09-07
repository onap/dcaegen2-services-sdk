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

import org.onap.dcaegen2.services.sdk.services.external.schema.manager.exception.NoLocalReferenceException;

import java.util.Map;
/**
 * An UrlMapper contains mapping between public urls received in events and schemas stored locally in the system.
 * */
final class UrlMapper {

    private final Map<String, String> mappingsCache;

    /**
     * Constructor
     * @param mappings contain mapping public urls to local schema file
     */
    UrlMapper(Map<String, String> mappings) {
        this.mappingsCache = Map.copyOf(mappings);
    }

    /**
     * @return cached mapping of public urls to local schemas
     */
    Map<String, String> getMappingsCache() {
        return Map.copyOf(mappingsCache);
    }

    /**
     * Map public url received in the event to url of local schema file
     * @param publicUrl public url
     * @return Urt to local schema file
     * @throws NoLocalReferenceException when local schema file doesn't exist for selected public url
     */
    String mapToLocalUrl(String publicUrl) throws NoLocalReferenceException {
        String externalUrl = mappingsCache.get(publicUrl);
        if (externalUrl == null) {
            throw new NoLocalReferenceException("Couldn't find mapping for public url. PublicURL: " + publicUrl);
        }
        return externalUrl;
    }


}
