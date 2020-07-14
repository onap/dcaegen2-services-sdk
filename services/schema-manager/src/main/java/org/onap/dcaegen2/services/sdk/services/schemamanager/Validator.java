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

import com.fasterxml.jackson.databind.JsonNode;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.JsonSplitter;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.SchemaReferenceJsonGenerator;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.SchemaReferenceMapper;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.UrlMapper;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.UrlSplitter;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Validator");
    private final String schemaRefPath;
    private final String stndDefinedDataPath;
    private final SchemaReferenceMapper schemaReferenceMapper;
    private final UrlSplitter urlSplitter;
    private final Map<String, SchemaValidator> validatorCache;

    public Validator(SchemaReferenceMapper schemaReferenceMapper, String schemaRefPath, String stndDefinedDataPath) {
        this.schemaReferenceMapper = schemaReferenceMapper;
        this.schemaRefPath = schemaRefPath;
        this.stndDefinedDataPath = stndDefinedDataPath;
        this.validatorCache = new HashMap<>();
        this.urlSplitter = new UrlSplitter();
    }

    public SchemaReferenceMapper getSchemaReferenceMapper() {
        return schemaReferenceMapper;
    }

    public boolean validate(JsonNode event) throws ResolutionException, IOException {
        String publicSchemaReference = JsonSplitter.getFragment(event, schemaRefPath).asText();
        JsonNode stndDefinedData = JsonSplitter.getFragment(event, this.stndDefinedDataPath);

        SchemaValidator schemaValidator = getValidatorFromCache(publicSchemaReference);

        try {
            schemaValidator.validate(stndDefinedData);
            LOGGER.info("Validation of stndDefinedDomain has been successful");
            return true;
        } catch (ValidationException ex) {
            LOGGER.error(String.valueOf(ex.results()));
            return false;
        }
    }

    private SchemaValidator getValidatorFromCache(String publicSchemaReference) throws ResolutionException, IOException {
        String localSchemaReference = schemaReferenceMapper.mapToLocalSchema(publicSchemaReference);
        return validatorCache.getOrDefault(
                urlSplitter.getUrlPart(localSchemaReference),
                createValidator(localSchemaReference));
    }

    private SchemaValidator createValidator(String localSchemaReference) throws ResolutionException, IOException {
        LOGGER.info("Creating new stndDefined schema validator");
        JsonNode schemaNode = SchemaReferenceJsonGenerator.getSchemaReference(localSchemaReference);
        SchemaValidator schemaValidator = new SchemaValidator("StndDefinedSchemaValidator", schemaNode);
        cacheSchemaValidator(localSchemaReference, schemaValidator);
        return schemaValidator;
    }

    private void cacheSchemaValidator(String localSchemaReference, SchemaValidator schemaValidator) {
        validatorCache.put(urlSplitter.getUrlPart(localSchemaReference), schemaValidator);
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

        public Validator build() {
            UrlMapper urlMapper = UrlMapper.getInstance(mappingFilePath, schemasPath);
            SchemaReferenceMapper schemaReferenceMapper = new SchemaReferenceMapper(urlMapper, schemasPath);
            return new Validator(schemaReferenceMapper, schemaRefPath, stndDefinedDataPath);
        }
    }
}
