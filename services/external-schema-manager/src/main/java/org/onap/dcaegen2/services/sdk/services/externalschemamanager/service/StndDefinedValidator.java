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

import com.fasterxml.jackson.databind.JsonNode;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class StndDefinedValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(StndDefinedValidator.class);
    private final String schemaRefPath;
    private final String stndDefinedDataPath;
    private final ValidatorCache validatorCache;

    public StndDefinedValidator(SchemaReferenceMapper schemaReferenceMapper, String schemaRefPath, String stndDefinedDataPath) {
        this.schemaRefPath = schemaRefPath;
        this.stndDefinedDataPath = stndDefinedDataPath;
        this.validatorCache = new ValidatorCache(schemaReferenceMapper);
    }

    ValidatorCache getValidatorCache() {
        return validatorCache;
    }

    public boolean validate(JsonNode event) throws IOException {
        String publicSchemaReference = JsonFragmentRetriever.getFragment(event, schemaRefPath).asText();
        JsonNode stndDefinedData = JsonFragmentRetriever.getFragment(event, this.stndDefinedDataPath);

        SchemaValidator schemaValidator = validatorCache.getValidator(publicSchemaReference);

        try {
            schemaValidator.validate(stndDefinedData);
            LOGGER.info("Validation of stndDefinedDomain has been successful");
            return true;
        } catch (ValidationException ex) {
            LOGGER.error(String.valueOf(ex.results()));
            return false;
        }
    }

    public static final class ValidatorBuilder {

        private String mappingFilePath = "etc/externalRepo/schema-map.json";
        private String schemaRefPath = "/event/stndDefinedFields/schemaReference";
        private String stndDefinedDataPath = "/event/stndDefinedFields/data";
        private String schemasPath = "etc/externalRepo";

        public ValidatorBuilder mappingFilePath(String mappingFilePath) {
            this.mappingFilePath = mappingFilePath;
            return this;
        }

        public ValidatorBuilder schemaRefPath(String schemaRefPath) {
            this.schemaRefPath = schemaRefPath;
            return this;
        }

        public ValidatorBuilder stndDefinedDataPath(String stndDefinedDataPath) {
            this.stndDefinedDataPath = stndDefinedDataPath;
            return this;
        }

        public ValidatorBuilder schemasPath(String schemasPath) {
            this.schemasPath = new File(schemasPath).getAbsolutePath();
            return this;
        }

        public StndDefinedValidator build() {
            UrlMapper urlMapper = UrlMapper.getInstance(mappingFilePath, schemasPath);
            SchemaReferenceMapper schemaReferenceMapper = new SchemaReferenceMapper(urlMapper, schemasPath);
            return new StndDefinedValidator(schemaReferenceMapper, schemaRefPath, stndDefinedDataPath);
        }
    }
}
