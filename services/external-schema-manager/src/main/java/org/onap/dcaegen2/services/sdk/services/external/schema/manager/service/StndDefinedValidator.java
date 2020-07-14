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

import com.fasterxml.jackson.databind.JsonNode;
import org.onap.dcaegen2.services.sdk.services.external.schema.manager.exception.IncorrectInternalFileReferenceException;
import org.onap.dcaegen2.services.sdk.services.external.schema.manager.exception.NoLocalReferenceException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class StndDefinedValidator {
    private static final Logger logger = LoggerFactory.getLogger(StndDefinedValidator.class);
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

    /**
     * Validates incoming event
     *
     * @param event as JsonNode
     * @return validation result
     * @throws IncorrectInternalFileReferenceException when reference to part of openApi yaml file with schemas is incorrect.
     * @throws NoLocalReferenceException when mapping for public url is not present in schema mapping file.
     */
    public boolean validate(JsonNode event) throws IncorrectInternalFileReferenceException, NoLocalReferenceException {
        boolean validationResult = false;
        try {
            JsonNode stndDefinedData = JsonFragmentRetriever.getFragment(event, this.stndDefinedDataPath);
            SchemaValidator schemaValidator = validatorCache.resolveValidator(event, schemaRefPath);
            schemaValidator.validate(stndDefinedData);
            logger.info("Validation of stndDefinedDomain has been successful");
            validationResult = true;
        } catch (ValidationException ex) {
            logger.error(String.valueOf(ex.results()));
        } catch (IOException ex){
            logger.error("Schema reference has invalid characters", ex);
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

        /**
         * @param mappingFilePath relative path to the file with mappings of schemas from the context in which
         *                        the application is running.
         * @return builder reference
         * @implNote example mapping file:
         * [
         * {
         * "publicURL": "http://localhost:8080/external1",
         * "localURL": "rel-16.4/2020-07-10-3GPP_TS28532_FaultMNS.yaml"
         * }
         * ]
         * @implNote default mapping file path: "etc/externalRepo/schema-map.json"
         */
        public ValidatorBuilder mappingFilePath(String mappingFilePath) {
            this.mappingFilePath = mappingFilePath;
            return this;
        }

        /**
         * @param schemaRefPath schema reference path in json.
         * @return builder reference
         * @implNote default: "/event/stndDefinedFields/schemaReference"
         */
        public ValidatorBuilder schemaRefPath(String schemaRefPath) {
            this.schemaRefPath = schemaRefPath;
            return this;
        }

        /**
         * @param stndDefinedDataPath path to stndDefined data in json.
         * @return builder reference
         * @implNote default: "/event/stndDefinedFields/data"
         */
        public ValidatorBuilder stndDefinedDataPath(String stndDefinedDataPath) {
            this.stndDefinedDataPath = stndDefinedDataPath;
            return this;
        }

        /**
         * @param schemasPath relative path to schemas directory from the context in which the application is running.
         * @return builder reference
         * @implNote default: "etc/externalRepo"
         */
        public ValidatorBuilder schemasPath(String schemasPath) {
            this.schemasPath = new File(schemasPath).getAbsolutePath();
            return this;
        }

        /**
         * Builds stndDefined Validator. May log warnings when:
         * - schema mapping file does not exist
         * - schema mapping file has invalid format
         * - any of schema files does not exist
         * - any of schema files has invalid yaml format
         * - any of schema files is empty
         *
         * @return stndDefinedValidator with cached schemas
         */
        public StndDefinedValidator build() {
            UrlMapper urlMapper = new UrlMapperFactory().getUrlMapper(mappingFilePath, schemasPath);
            SchemaReferenceMapper schemaReferenceMapper = new SchemaReferenceMapper(urlMapper, schemasPath);
            ValidatorCache validatorCache = new ValidatorCache(schemaReferenceMapper);
            return new StndDefinedValidator(schemaRefPath, stndDefinedDataPath, validatorCache);
        }
    }
}
