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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlSplitterTest {

    private static UrlSplitter urlSplitter;

    @BeforeAll
    static void setUp() {
        urlSplitter = new UrlSplitter();
    }

    @Test
    public void shouldReturnFirstPartOfLocalUrl() {
        //given
        String url = "http://localhost:8080/test#secondPartOfUrl";
        String expectedPart = "http://localhost:8080/test";

        //when
        String actualPart = urlSplitter.getUrlPart(url);

        //then
        assertEquals(expectedPart, actualPart);
    }

    @Test
    public void shouldReturnSecondPartOfLocalUrl() {
        //given
        String url = "http://localhost:8080/test#secondPartOfUrl";
        String expectedPart = "secondPartOfUrl";

        //when
        String actualPart = urlSplitter.getFragmentPart(url);

        //then
        assertEquals(expectedPart, actualPart);
    }

    @Test
    public void shouldReturnEmptyStringWhenGettingSecondPartOfUrlAndAddressDoesNotContainHash() {
        //given
        String invalidUrlAddress = "urlWithoutHash";

        //when
        String result = UrlSplitterTest.urlSplitter.getFragmentPart(invalidUrlAddress);

        //then
        assertEquals("", result);
    }
}