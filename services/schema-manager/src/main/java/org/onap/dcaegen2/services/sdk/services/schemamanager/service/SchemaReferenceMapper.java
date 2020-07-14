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

public class SchemaReferenceMapper {

    private final UrlMapper urlMapper;

    public SchemaReferenceMapper(UrlMapper urlMapper) {
        this.urlMapper = urlMapper;
    }

    public String mapToLocalSchema(String publicSchemaReference) {
        String[] urlFragments = publicSchemaReference.split("#");
        String localUrl = urlMapper.getLocalUrl(urlFragments[0]);
        if (urlFragments.length < 2) {
            return localUrl;
        }
        return getLocalSchemaReference(localUrl, urlFragments[1]);
    }

    private String getLocalSchemaReference(String localUrl, String urlFragment) {
        return localUrl + "#/" + urlFragment;
    }
}
