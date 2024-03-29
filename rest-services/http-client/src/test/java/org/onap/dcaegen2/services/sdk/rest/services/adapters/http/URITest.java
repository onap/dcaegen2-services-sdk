/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019-2022 NOKIA Intellectual Property. All rights reserved.
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
 *
 */

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class URITest {

    @Test
    void buildProperUri() {
        String expectedValue = "http://user@localhost:8080/path?query#fragment";
        URI uri = new URI.URIBuilder().scheme("http")
            .host("localhost")
            .port(8080)
            .path("/path")
            .fragment("fragment")
            .authority("authority")
            .userInfo("user")
            .query("query")
            .build();

        assertEquals(expectedValue, uri.toString());
    }

    @Test
    void buildProperUriWithoutUser() {
        String expectedValue = "http://localhost:8080/path?query#fragment";
        URI uri = new URI.URIBuilder().scheme("http")
            .host("localhost")
            .port(8080)
            .path("/path")
            .fragment("fragment")
            .authority("authority")
            .query("query")
            .build();

        assertEquals(expectedValue, uri.toString());
    }

    @Test
    void buildProperUriForMissingQuery() {
        String expectedValue = "http://localhost:8080/path#fragment";
        URI uri = new URI.URIBuilder().scheme("http")
            .host("localhost")
            .port(8080)
            .path("/path")
            .fragment("fragment")
            .authority("authority")
            .build();

        assertEquals(expectedValue, uri.toString());
    }

    @Test
    void buildProperUriForMissingFragment() {
        String expectedValue = "http://localhost:8080/path?query";
        URI uri = new URI.URIBuilder().scheme("http")
            .host("localhost")
            .port(8080)
            .path("/path")
            .authority("authority")
            .query("query")
            .build();

        assertEquals(expectedValue, uri.toString());
    }
}
