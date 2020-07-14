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

package org.onap.dcaegen2.services.sdk.services.schemamanager.service;

import java.security.InvalidParameterException;

public class SchemaReferenceMapper {

    private final UrlMapper urlMapper;
    private final UrlSplitter urlSplitter;
    private final String schemasPath;

    public SchemaReferenceMapper(UrlMapper urlMapper, String schemasPath) {
        this.urlMapper = urlMapper;
        this.schemasPath = schemasPath;
        this.urlSplitter = new UrlSplitter();
    }

    public String mapToLocalSchema(String publicSchemaReference) {
        //todo test with schemaReference without #
        String localUrl;
        try {
            String publicUrlPart = urlSplitter.getUrlPart(publicSchemaReference);
            localUrl = urlMapper.mapToLocalUrl(publicUrlPart);
        } catch (InvalidParameterException e) {
            return urlMapper.mapToLocalUrl(publicSchemaReference);
        }
        return getLocalSchemaReference(localUrl, urlSplitter.getFragmentPart(publicSchemaReference));
    }

    private String getLocalSchemaReference(String localUrl, String urlFragment) {
        return schemasPath + "/" + localUrl + "#/" + urlFragment;
    }
}
