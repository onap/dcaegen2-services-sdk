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

package org.onap.dcaegen2.services.sdk.services.external.schema.manager.model;

import org.onap.dcaegen2.services.sdk.services.external.schema.manager.service.SchemaReferenceResolver;

/**
 * A SchemaReference model contains information about schema URL which will be used to validate json content.
 */
public class SchemaReference {

    public static final String URL_SEPARATOR = "#";

    private final String url;
    private final String internalReference;

    /**
     * Constructor
     * @param schemaReferenceResolver to resolve schema reference
     */
    public SchemaReference(SchemaReferenceResolver schemaReferenceResolver) {
        this(schemaReferenceResolver.resolveUrl(), schemaReferenceResolver.resolveInternalReference());
    }

    public SchemaReference(String url, String internalReference) {
        this.url = url;
        this.internalReference = internalReference;
    }

    /**
     * @return url to schema reference
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return internal schema reference path
     */
    public String getInternalReference() {
        return internalReference;
    }

    /**
     * Creates full schema reference as <path to yaml file>#<reference to part of yaml file with specific schema>
     *
     * @return <path to yaml file>#<reference to part of yaml file with specific schema>
     */
    public String getFullSchemaReference() {
        return url + URL_SEPARATOR + internalReference;
    }
}
