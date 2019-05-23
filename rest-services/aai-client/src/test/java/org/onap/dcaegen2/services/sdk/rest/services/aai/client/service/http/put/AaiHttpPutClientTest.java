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
package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.put;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.AaiClientConfigurations.secureConfiguration;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.AbstractHttpClientTest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AaiHttpPutClientTest extends AbstractHttpClientTest {

    @Test
    void getAaiResponse_shouldCallPutMethod_withGivenAaiHeaders() {
        Map<String, String> headers = HashMap.of("sample-key", "sample-value");
        AaiHttpPutClient cut = new AaiHttpPutClient(secureConfiguration(headers.toJavaMap()), bodyBuilder, "",
                httpClient);

        given(bodyBuilder.createJsonBody(eq(aaiModel)))
                .willReturn("test-body");

        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.just(response));

        StepVerifier
                .create(cut.getAaiResponse(aaiModel))
                .expectNext(response)
                .verifyComplete();

        verify(httpClient)
                .call(argThat(httpRequest -> httpRequest.customHeaders().equals(headers)));
    }

    @Test
    void getAaiResponse_shouldCallPutMethod_withProperUri() {
        String uri = "test-uri";
        AaiHttpPutClient cut = new AaiHttpPutClient(secureConfiguration(), bodyBuilder, uri, httpClient);

        given(bodyBuilder.createJsonBody(eq(aaiModel)))
                .willReturn("test-body");

        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.just(response));

        StepVerifier
                .create(cut.getAaiResponse(aaiModel))
                .expectNext(response)
                .verifyComplete();

        verify(httpClient)
                .call(argThat(httpRequest -> httpRequest.url().equals(uri)));

    }
}
