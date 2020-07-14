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


public class SchemaReferenceResolver {

    private static final String ROOT_REFERENCE = "/";
    private static final int URL_INDEX = 0;
    private static final int INTERNAL_REFERENCE_INDEX = 1;
    private static final int REFERENCE_FRAGMENTS_CONTAINING_INTERNAL_REF_SIZE = 2;

    private final String schemaReference;

    public SchemaReferenceResolver(String schemaReference) {
        this.schemaReference = schemaReference;
    }

    /**
     * Extracts the schema reference fragment, provided in the event, corresponding to the url path (before #).
     *
     * @return url path
     */
    public String resolveUrl() {
        String[] referenceFragments = schemaReference.split(SchemaReference.URL_SEPARATOR);
        return referenceFragments[URL_INDEX];
    }

    /**
     * Returns internal reference (after #) if it is present in schema reference and add "/" on the beginning if it's
     * not present, otherwise returns "/".
     *
     * @return resolved internal reference
     */
    public String resolveInternalReference() {
        String reference;
        String[] referenceFragments = schemaReference.split(SchemaReference.URL_SEPARATOR);
        if (internalReferenceExists(referenceFragments)) {
            String internalReference = referenceFragments[INTERNAL_REFERENCE_INDEX];
            reference = prepareExistingInternalReference(internalReference);
        } else {
            reference = ROOT_REFERENCE;
        }

        return reference;
    }

    private boolean internalReferenceExists(String[] referenceFragments) {
        return referenceFragments.length == REFERENCE_FRAGMENTS_CONTAINING_INTERNAL_REF_SIZE;
    }

    private String prepareExistingInternalReference(String internalReference) {
        return (!isAbsolute(internalReference)) ? ROOT_REFERENCE + internalReference : internalReference;
    }

    private boolean isAbsolute(String internalReference) {
        return internalReference.startsWith(ROOT_REFERENCE);
    }


}
