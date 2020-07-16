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

package org.onap.dcaegen2.services.sdk.services.schemamanager;

import org.onap.dcaegen2.services.sdk.services.schemamanager.service.SchemaReferenceMapper;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.UrlMapper;

public class ValidatorFactory {

    public Validator create(String mappingFilePath) {
        UrlMapper urlMapper = UrlMapper.getInstance(mappingFilePath);
        SchemaReferenceMapper schemaReferenceMapper = new SchemaReferenceMapper(urlMapper);
        return new Validator(schemaReferenceMapper);
    }
}
