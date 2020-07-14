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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaReferenceResolverTest {

    @Test
    void shouldResolveUrlFromSchemaReference() {
        //given
        String expectedUrl = "http://localhost:8080/test";
        String schemaReference = expectedUrl + "#test/internal/ref";

        //when
        String actualUrl = new SchemaReferenceResolver(schemaReference).resolveUrl();

        //then
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldResolveUrlFromSchemaReferenceWhenInternalReferenceDoesNotExist() {
        //given
        String expectedUrl = "http://localhost:8080/test";

        //when
        String actualUrl = new SchemaReferenceResolver(expectedUrl).resolveUrl();

        //then
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void shouldResolveInternalReferenceFromSchemaReference() {
        //given
        String schemaReference = "http://localhost:8080/test#/internal/ref/test";
        String expectedInternalReference = "/internal/ref/test";

        //when
        String actualInternalReference = new SchemaReferenceResolver(schemaReference).resolveInternalReference();

        //then
        assertEquals(expectedInternalReference, actualInternalReference);
    }

    @Test
    void shouldResolveInternalReferenceFromSchemaReferenceWhenInternalReferenceDoesNotExist() {
        //given
        String schemaReference = "http://localhost:8080/test";
        String expectedInternalReference = "/";

        //when
        String actualInternalReference = new SchemaReferenceResolver(schemaReference).resolveInternalReference();

        //then
        assertEquals(expectedInternalReference, actualInternalReference);
    }

    @Test
    void shouldResolveInternalReferenceFromSchemaReferenceWhenInternalReferenceIsRoot() {
        //given
        String schemaReference = "http://localhost:8080/test#/";
        String expectedInternalReference = "/";

        //when
        String actualInternalReference = new SchemaReferenceResolver(schemaReference).resolveInternalReference();

        //then
        assertEquals(expectedInternalReference, actualInternalReference);
    }

    @Test
    void shouldResolveInternalReferenceFromSchemaReferenceWhenInternalReferenceIsEmpty() {
        //given
        String schemaReference = "http://localhost:8080/test#";
        String expectedInternalReference = "/";

        //when
        String actualInternalReference = new SchemaReferenceResolver(schemaReference).resolveInternalReference();

        //then
        assertEquals(expectedInternalReference, actualInternalReference);
    }
}