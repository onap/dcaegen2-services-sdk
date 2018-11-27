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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/27/18
 */
class DMaaPConsumerReactiveHttpClientTest {

    private static final String JSON_MESSAGE = "{ \"responseFromDmaap\": \"Success\"}";
    private DMaaPConsumerReactiveHttpClient dmaapConsumerReactiveHttpClient;
    private DmaapConsumerConfiguration consumerConfigurationMock = mock(DmaapConsumerConfiguration.class);
    private Mono<String> expectedResult = Mono.empty();
    private WebClient webClient;
    private RequestHeadersUriSpec requestHeadersSpec;
    private ResponseSpec responseSpec;


    @BeforeEach
    void setUp() {
        when(consumerConfigurationMock.dmaapHostName()).thenReturn("54.45.33.2");
        when(consumerConfigurationMock.dmaapProtocol()).thenReturn("https");
        when(consumerConfigurationMock.dmaapPortNumber()).thenReturn(1234);
        when(consumerConfigurationMock.dmaapUserName()).thenReturn("PRH");
        when(consumerConfigurationMock.dmaapUserPassword()).thenReturn("PRH");
        when(consumerConfigurationMock.dmaapContentType()).thenReturn("application/json");
        when(consumerConfigurationMock.dmaapTopicName()).thenReturn("unauthenticated.SEC_OTHER_OUTPUT");
        when(consumerConfigurationMock.consumerGroup()).thenReturn("OpenDCAE-c12");
        when(consumerConfigurationMock.consumerId()).thenReturn("c12");

        webClient = spy(WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, consumerConfigurationMock.dmaapContentType())
            .filter(basicAuthentication(consumerConfigurationMock.dmaapUserName(),
                consumerConfigurationMock.dmaapUserPassword()))
            .build());
        dmaapConsumerReactiveHttpClient = new DMaaPConsumerReactiveHttpClient(consumerConfigurationMock, webClient);
        requestHeadersSpec = mock(RequestHeadersUriSpec.class);
        responseSpec = mock(ResponseSpec.class);
    }


    @Test
    void getHttpResponse_Success() {
        //given
        expectedResult = Mono.just(JSON_MESSAGE);

        //when
        mockDependantObjects();
        doReturn(expectedResult).when(responseSpec).bodyToMono(String.class);
        Mono<String> response = dmaapConsumerReactiveHttpClient.getDMaaPConsumerResponse();

        //then
        StepVerifier.create(response).expectSubscription()
            .expectNextMatches(results -> {
                Assertions.assertEquals(results, expectedResult.block());
                return true;
            }).verifyComplete();
    }

    @Test
    void getAppropriateUri_whenPassingCorrectedPathForPnf() {
        Assertions.assertEquals(dmaapConsumerReactiveHttpClient.getUri(),
            URI.create("https://54.45.33.2:1234/unauthenticated.SEC_OTHER_OUTPUT/OpenDCAE-c12/c12"));
    }

    private void mockDependantObjects() {
        when(webClient.get()).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.uri((URI) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
    }

}