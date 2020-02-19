/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CloudConfigurationClientTest {
    private static final String CONFIGURATION_MOCK = "{\"test\":1}";
    private static final JsonObject CONFIGURATION_JSON_MOCK = new Gson()
        .fromJson(CONFIGURATION_MOCK, JsonObject.class);

    private final CloudConfigurationProvider provider = mock(CloudConfigurationProvider.class);
    private final CbsClientConfiguration configuration = mock(CbsClientConfiguration.class);

    private CloudConfigurationClient client;

    @BeforeEach
    void setUp() {
        client = new CloudConfigurationClient(provider);
        when(provider.callForServiceConfigurationReactive(any(CbsClientConfiguration.class)))
            .thenReturn(Mono.just(CONFIGURATION_JSON_MOCK));
    }

    @Test
    void callForServiceConfigurationReactive() {
        StepVerifier.create(client.callForServiceConfigurationReactive("hostName", 4444, "cbsName1", "appName1"))
            .expectSubscription()
            .expectNext(CONFIGURATION_JSON_MOCK).verifyComplete();
    }

    @Test
    void testCallForServiceConfigurationReactive() {
        StepVerifier.create(client.callForServiceConfigurationReactive(configuration))
            .expectSubscription()
            .expectNext(CONFIGURATION_JSON_MOCK).verifyComplete();
    }

    @Test
    void callForServiceConfiguration() {
        JsonObject json = client.callForServiceConfiguration("hostName", 4444, "cbsName1", "appName1");
        assertEquals(CONFIGURATION_JSON_MOCK, json);
    }

    @Test
    void testCallForServiceConfiguration() {
        JsonObject json = client.callForServiceConfiguration(configuration);
        assertEquals(CONFIGURATION_JSON_MOCK, json);
    }
}