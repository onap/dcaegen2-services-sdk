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

import org.onap.dcaegen2.services.sdk.services.externalschemamanager.model.SchemaReference;

import java.io.File;

class SchemaReferenceMapper {

    private final UrlMapper urlMapper;
    private final String schemasPath;

    SchemaReferenceMapper(UrlMapper urlMapper, String schemasPath) {
        this.urlMapper = urlMapper;
        this.schemasPath = schemasPath;
    }

    UrlMapper getUrlMapper() {
        return urlMapper;
    }

    SchemaReference mapToLocalSchema(SchemaReference schemaReference) {
        String publicUrl = schemaReference.getUrl();
        String localUrl = urlMapper.mapToLocalUrl(publicUrl);
        return createLocalSchemaReference(localUrl, schemaReference.getInternalReference());
    }

    private SchemaReference createLocalSchemaReference(String localUrl, String internalReference) {
        String relativeLocalUrl = getRelativeLocalUrl(localUrl);
        return new SchemaReference(relativeLocalUrl, internalReference);
    }

    private String getRelativeLocalUrl(String localUrl) {
        return schemasPath + File.separator + localUrl;
    }
}
