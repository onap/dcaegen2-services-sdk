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

    private StndDefinedValidator(String schemaRefPath, String stndDefinedDataPath, ValidatorCache validatorCache) {
        this.schemaRefPath = schemaRefPath;
        this.stndDefinedDataPath = stndDefinedDataPath;
        this.validatorCache = validatorCache;
    }

    ValidatorCache getValidatorCache() {
        return validatorCache;
    }

    public boolean validate(JsonNode event) throws IOException {
        boolean validationResult = false;
        try {
            JsonNode stndDefinedData = JsonFragmentRetriever.getFragment(event, this.stndDefinedDataPath);
            SchemaValidator schemaValidator = validatorCache.resolveValidator(event, schemaRefPath);
            schemaValidator.validate(stndDefinedData);
            LOGGER.info("Validation of stndDefinedDomain has been successful");
            validationResult = true;
        } catch (ValidationException ex) {
            LOGGER.error(String.valueOf(ex.results()));
        }
        return validationResult;
    }

    public static final class ValidatorBuilder {

        public static final String DEFAULT_MAPPING_FILE_PATH = "etc/externalRepo/schema-map.json";
        public static final String DEFAULT_SCHEMA_REF_PATH = "/event/stndDefinedFields/schemaReference";
        public static final String DEFAULT_STND_DEFINED_DATA_PATH = "/event/stndDefinedFields/data";
        public static final String DEFAULT_SCHEMAS_PATH = "etc/externalRepo";

        private String mappingFilePath = DEFAULT_MAPPING_FILE_PATH;
        private String schemaRefPath = DEFAULT_SCHEMA_REF_PATH;
        private String stndDefinedDataPath = DEFAULT_STND_DEFINED_DATA_PATH;
        private String schemasPath = DEFAULT_SCHEMAS_PATH;

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
            UrlMapper urlMapper = new UrlMapperFactory().getInstance(mappingFilePath, schemasPath);
            SchemaReferenceMapper schemaReferenceMapper = new SchemaReferenceMapper(urlMapper, schemasPath);
            ValidatorCache validatorCache = new ValidatorCache(schemaReferenceMapper);
            return new StndDefinedValidator(schemaRefPath, stndDefinedDataPath, validatorCache);
        }
    }
}
