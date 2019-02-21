/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.get;


import org.junit.jupiter.api.BeforeEach;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import reactor.netty.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AaiHttpGetClientTest {

    private static final String SUCCESS_RESPONSE = "{\"correlationId\":\"NOKnhfsadhff\"," +
            "\"ipaddress-v4\":\"256.22.33.155\", " +
            "\"ipaddress-v6\":\"200J:0db8:85a3:0000:0000:8a2e:0370:7334\"}";

    private AaiHttpGetClient aaiReactiveHttpGetClient;
    private HttpClient httpClient;

    private AaiClientConfiguration aaiConfigurationMock;
    private AaiModel aaiModel;
    private Map<String,String> aaiHeaders;


    @BeforeEach
    void setUp() {
        setupHeaders();
        aaiModel = mock(AaiModel.class);
        aaiConfigurationMock = mock(AaiClientConfiguration.class);

        when(aaiConfigurationMock.aaiHost()).thenReturn("54.45.33.2");
        when(aaiConfigurationMock.aaiProtocol()).thenReturn("https");
        when(aaiConfigurationMock.aaiPort()).thenReturn(1234);
        when(aaiConfigurationMock.aaiUserName()).thenReturn("PRH");
        when(aaiConfigurationMock.aaiUserPassword()).thenReturn("PRH");
        when(aaiConfigurationMock.aaiBasePath()).thenReturn("/aai/v11");
        when(aaiConfigurationMock.aaiPnfPath()).thenReturn("/network/pnfs/pnf");
        when(aaiConfigurationMock.aaiHeaders()).thenReturn(aaiHeaders);

        when(aaiModel.getCorrelationId()).thenReturn("NOKnhfsadhff");


    }


    private void setupHeaders() {
        aaiHeaders = new HashMap<>();
        aaiHeaders.put("X-FromAppId", "PRH");
        aaiHeaders.put("X-TransactionId", "vv-temp");
        aaiHeaders.put("Accept", "application/json");
        aaiHeaders.put("Real-Time", "true");
        aaiHeaders.put("Content-Type", "application/json");
    }

    private void mockHttpClientObject() {
        //when(HttpClient.create().)
    }

}