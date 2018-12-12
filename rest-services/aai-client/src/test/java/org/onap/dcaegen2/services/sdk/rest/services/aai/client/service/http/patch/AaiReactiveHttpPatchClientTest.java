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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.patch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;

import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;


class AaiReactiveHttpPatchClientTest {
    private static final Integer SUCCESS_RESPONSE = 200;
    private static AaiClientConfiguration aaiConfigurationMock = mock(AaiClientConfiguration.class);


    private AaiReactiveHttpPatchClient httpClient;
    private WebClient webClient;
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.ResponseSpec responseSpec;

    private Map<String, String> aaiHeaders;
    private ClientResponse clientResponse;
    private Mono<ClientResponse> clientResponseMono;

    private AaiModel aaiModel = mock(AaiModel.class);
    private JsonBodyBuilder<AaiModel> jsonBodyBuilder = mock(JsonBodyBuilder.class);

    @BeforeEach
    void setUp() {
        setupHeaders();
        clientResponse = mock(ClientResponse.class);
        clientResponseMono = Mono.just(clientResponse);

        when(aaiConfigurationMock.aaiHost()).thenReturn("54.45.33.2");
        when(aaiConfigurationMock.aaiProtocol()).thenReturn("https");
        when(aaiConfigurationMock.aaiPort()).thenReturn(1234);
        when(aaiConfigurationMock.aaiUserName()).thenReturn("PRH");
        when(aaiConfigurationMock.aaiUserPassword()).thenReturn("PRH");
        when(aaiConfigurationMock.aaiBasePath()).thenReturn("/aai/v11");
        when(aaiConfigurationMock.aaiPnfPath()).thenReturn("/network/pnfs/pnf");
        when(aaiConfigurationMock.aaiHeaders()).thenReturn(aaiHeaders);

        when(aaiModel.getCorrelationId()).thenReturn("NOKnhfsadhff");

        when(jsonBodyBuilder.createJsonBody(aaiModel)).thenReturn(
                "{\"correlationId\":\"NOKnhfsadhff\"," +
                "\"ipaddress-v4\":\"256.22.33.155\", " +
                "\"ipaddress-v6\":\"200J:0db8:85a3:0000:0000:8a2e:0370:7334\"}");

        httpClient = new AaiReactiveHttpPatchClient(aaiConfigurationMock, jsonBodyBuilder);

        webClient = spy(WebClient.builder()
                .defaultHeaders(httpHeaders -> httpHeaders.setAll(aaiHeaders))
                .filter(basicAuthentication(aaiConfigurationMock.aaiUserName(), aaiConfigurationMock.aaiUserPassword()))
                .build());

        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);
    }

    @Test
    void getAaiProducerResponse_shouldReturn200() {
        //given
        Mono<Integer> expectedResult = Mono.just(SUCCESS_RESPONSE);

        //when
        mockWebClientDependantObject();
        doReturn(expectedResult).when(responseSpec).bodyToMono(Integer.class);
        httpClient.createAaiWebClient(webClient);

        //then
        StepVerifier.create(httpClient.getAaiProducerResponse(aaiModel)).expectSubscription()
                .expectNextMatches(results -> {
                    Assertions.assertEquals(results, clientResponse);
                    return true;
                }).verifyComplete();
    }


    @Test
    void getAppropriateUri_whenPassingCorrectedPathForPnf() {
        Assertions.assertEquals(httpClient.getUri("NOKnhfsadhff"),
                URI.create("https://54.45.33.2:1234/aai/v11/network/pnfs/pnf/NOKnhfsadhff"));
    }


    private void setupHeaders() {
        aaiHeaders = new HashMap<>();
        aaiHeaders.put("X-FromAppId", "PRH");
        aaiHeaders.put("X-TransactionId", "vv-temp");
        aaiHeaders.put("Accept", "application/json");
        aaiHeaders.put("Real-Time", "true");
        aaiHeaders.put("Content-Type", "application/merge-patch+json");
    }

    private void mockWebClientDependantObject() {
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(webClient.patch()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri((URI) any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(), (Class<Object>) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchange()).thenReturn(clientResponseMono);
    }
}