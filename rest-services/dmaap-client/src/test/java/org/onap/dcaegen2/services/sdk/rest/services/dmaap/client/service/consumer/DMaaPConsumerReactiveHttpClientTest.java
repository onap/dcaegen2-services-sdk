/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.net.URI;
import java.util.Optional;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.DMaaPClientServiceUtils;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/27/18
 */
class DMaaPConsumerReactiveHttpClientTest {

    private static final String JSON_MESSAGE = "{ \"responseFromDmaap\": \"Success\"}";
    private DMaaPConsumerReactiveHttpClient dmaapConsumerReactiveHttpClient;
    private DmaapConsumerConfiguration consumerConfigurationMock = mock(DmaapConsumerConfiguration.class);
    private Mono<JsonElement> expectedResult;
    private CloudHttpClient httpClient = mock(CloudHttpClient.class);
    private URI exampleTestUri = URI
        .create("https://54.45.33.2:1234/unauthenticated.SEC_OTHER_OUTPUT/OpenDCAE-c12/c12");
    private RequestDiagnosticContext requestDiagnosticContext = mock(RequestDiagnosticContext.class);

    @BeforeEach
    void setUp() {
        when(consumerConfigurationMock.dmaapHostName()).thenReturn("54.45.33.2");
        when(consumerConfigurationMock.dmaapProtocol()).thenReturn("https");
        when(consumerConfigurationMock.dmaapPortNumber()).thenReturn(1234);
        when(consumerConfigurationMock.dmaapUserName()).thenReturn("PRH");
        when(consumerConfigurationMock.dmaapUserPassword()).thenReturn("PRH");
        when(consumerConfigurationMock.dmaapContentType()).thenReturn(ContentType.APPLICATION_JSON.getMimeType());
        when(consumerConfigurationMock.dmaapTopicName()).thenReturn("unauthenticated.SEC_OTHER_OUTPUT");
        when(consumerConfigurationMock.consumerGroup()).thenReturn("OpenDCAE-c12");
        when(consumerConfigurationMock.consumerId()).thenReturn("c12");
        dmaapConsumerReactiveHttpClient = new DMaaPConsumerReactiveHttpClient(consumerConfigurationMock, httpClient);
    }

    @Test
    void getHttpResponse_Success() {
        //given
        expectedResult = Mono.just(mock(JsonElement.class));
        when(httpClient.get(exampleTestUri.toString(), requestDiagnosticContext,
            DMaaPClientServiceUtils.getHeaders(ContentType.APPLICATION_JSON.getMimeType()), JsonElement.class))
            .thenReturn(expectedResult);
        //when
        Mono<JsonElement> response = dmaapConsumerReactiveHttpClient
            .getDMaaPConsumerResponse(Optional.of(requestDiagnosticContext));
        //then
        StepVerifier.create(response).expectSubscription()
            .expectNextMatches(results -> {
                Assertions.assertEquals(results, expectedResult.block());
                return true;
            }).verifyComplete();
    }

    @Test
    void getAppropriateUri_whenPassingCorrectedPathForPnf() {
        Assertions.assertEquals(dmaapConsumerReactiveHttpClient.getUri(), exampleTestUri);
    }


}