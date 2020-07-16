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

import com.fasterxml.jackson.databind.JsonNode;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.JsonSplitter;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.SchemaReferenceJsonGenerator;
import org.onap.dcaegen2.services.sdk.services.schemamanager.service.SchemaReferenceMapper;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.schema.validator.v3.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Validator");
    private static final String SCHEMA_REF_PATH = "/event/stndDefinedFields/schemaReference";
    private static final String STND_DEFINED_DATA = "/event/stndDefinedFields/data";
    private final SchemaReferenceMapper schemaReferenceMapper;

    public Validator(SchemaReferenceMapper schemaReferenceMapper) {
        this.schemaReferenceMapper = schemaReferenceMapper;
    }

    public boolean validate(JsonNode event) throws ResolutionException, IOException {
        SchemaValidator schemaValidator = prepareValidator(event);
        JsonNode stndDefinedData = JsonSplitter.getFragment(event, STND_DEFINED_DATA);

        try {
            schemaValidator.validate(stndDefinedData);
            LOGGER.info("Validation of stndDefinedDomain has been successful");
            return true;
        } catch (ValidationException ex) {
            LOGGER.error(String.valueOf(ex.results()));
            return false;
        }
    }

    private SchemaValidator prepareValidator(JsonNode event) throws ResolutionException, IOException {
        String publicSchemaReference = JsonSplitter.getFragment(event, SCHEMA_REF_PATH).asText();
        String localSchemaReference = schemaReferenceMapper.mapToLocalSchema(publicSchemaReference);
        JsonNode schemaNode = SchemaReferenceJsonGenerator.getSchemaReference(localSchemaReference);
        return new SchemaValidator("EventValidator", schemaNode);
    }

}
