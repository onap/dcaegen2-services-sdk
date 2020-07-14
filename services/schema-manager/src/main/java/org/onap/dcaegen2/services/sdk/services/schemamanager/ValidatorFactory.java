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

package org.onap.dcaegen2.services.sdk.services.schemamanager;

import org.onap.dcaegen2.services.sdk.services.schemamanager.service.SchemaReferenceMapper;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.UrlMapper;

public class ValidatorFactory {

    private String mappingFilePath;
    private String schemaRefPath;
    private String stndDefinedDataPath;
    private String schemasPath;

    public ValidatorFactory setMappingFilePath(String mappingFilePath) {
        this.mappingFilePath = mappingFilePath;
        return this;
    }

    public ValidatorFactory setSchemaRefPath(String schemaRefPath) {
        this.schemaRefPath = schemaRefPath;
        return this;
    }

    public ValidatorFactory setStndDefinedDataPath(String stndDefinedDataPath) {
        this.stndDefinedDataPath = stndDefinedDataPath;
        return this;
    }

    public ValidatorFactory setSchemasPath(String schemasPath) {
        this.schemasPath = schemasPath;
        return this;
    }

    public Validator create() {
        UrlMapper urlMapper = UrlMapper.getInstance(mappingFilePath, schemasPath);
        SchemaReferenceMapper schemaReferenceMapper = new SchemaReferenceMapper(urlMapper, schemasPath);
        return new Validator(schemaReferenceMapper, schemaRefPath, stndDefinedDataPath);
    }
}
