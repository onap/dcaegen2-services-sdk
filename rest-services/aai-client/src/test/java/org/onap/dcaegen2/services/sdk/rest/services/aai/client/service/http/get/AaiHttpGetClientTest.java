/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.AaiClientConfigurations;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AbstractHttpClientTest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AaiHttpGetClientTest extends AbstractHttpClientTest {

    @BeforeEach
    void setup() {
        this.httpClient = mock(CloudHttpClient.class);
        this.response = mock(HttpResponse.class);
    }

    @Test
    void getAaiResponse_shouldCallGetMethod_withGivenAaiHeaders() {
        AaiModel model = () -> "test-id";
        Map<String, String> headers = new HashMap<>();
        AaiHttpGetClient cut = new AaiHttpGetClient(AaiClientConfigurations.secureConfiguration(headers), httpClient);

        when(httpClient.get(
                anyString(),
                any(RequestDiagnosticContext.class),
                eq(headers)
        )).thenReturn(Mono.just(response));

        StepVerifier
                .create(cut.getAaiResponse(model))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void getAaiResponse_shouldCallGetMethod_withProperUri() {
        AaiModel model = () -> "test-id";
        AaiClientConfiguration configuration = AaiClientConfigurations.secureConfiguration();
        String expectedUri = constructAaiUri(configuration, model.getCorrelationId());
        AaiHttpGetClient cut = new AaiHttpGetClient(configuration, httpClient);

        when(httpClient.get(
                eq(expectedUri),
                any(RequestDiagnosticContext.class),
                anyMap()
        )).thenReturn(Mono.just(response));

        StepVerifier
                .create(cut.getAaiResponse(model))
                .expectNext(response)
                .verifyComplete();
    }
}
