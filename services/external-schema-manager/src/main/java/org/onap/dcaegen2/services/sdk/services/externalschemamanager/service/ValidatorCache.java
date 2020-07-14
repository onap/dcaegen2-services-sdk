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
import org.onap.dcaegen2.services.sdk.services.externalschemamanager.exception.IncorrectInternalFileReferenceException;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ValidatorCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorCache.class);
    private final SchemaReferenceMapper schemaReferenceMapper;
    private final UrlSplitter urlSplitter;
    private final Map<String, SchemaValidator> cache;

    ValidatorCache(SchemaReferenceMapper schemaReferenceMapper) {
        this.schemaReferenceMapper = schemaReferenceMapper;
        this.urlSplitter = new UrlSplitter();
        this.cache = new HashMap<>();
    }

    SchemaReferenceMapper getSchemaReferenceMapper() {
        return schemaReferenceMapper;
    }

    synchronized SchemaValidator getValidator(String publicSchemaReference) throws IOException {
        String localSchemaReference = schemaReferenceMapper.mapToLocalSchema(publicSchemaReference);
        SchemaValidator validator = cache.get(urlSplitter.getUrlPart(localSchemaReference));
        if (!isValidatorCached(validator)) {
            validator = createNewValidator(localSchemaReference);
        }
        return validator;
    }

    private boolean isValidatorCached(SchemaValidator validator) {
        return validator != null;
    }

    private SchemaValidator createNewValidator(String localSchemaReference) throws IOException {
        LOGGER.info("Creating new stndDefined schema validator");
        JsonNode schemaRefNode = SchemaReferenceJsonGenerator.getSchemaReferenceJson(localSchemaReference);
        SchemaValidator schemaValidator = handleValidatorCreation(schemaRefNode);
        cacheSchemaValidator(localSchemaReference, schemaValidator);
        return schemaValidator;
    }

    private SchemaValidator handleValidatorCreation(JsonNode schemaNode) {
        try {
            return new SchemaValidator("StndDefinedSchemaValidator", schemaNode);
        } catch (ResolutionException e) {
            throw new IncorrectInternalFileReferenceException("Schema reference refer to existing file, " +
                    "but internal reference (after #) is incorrect. " + e.getMessage());
        }
    }

    private void cacheSchemaValidator(String localSchemaReference, SchemaValidator schemaValidator) {
        LOGGER.info("Adding new stndDefined schema validator to cache");
        cache.put(urlSplitter.getUrlPart(localSchemaReference), schemaValidator);
    }
}
