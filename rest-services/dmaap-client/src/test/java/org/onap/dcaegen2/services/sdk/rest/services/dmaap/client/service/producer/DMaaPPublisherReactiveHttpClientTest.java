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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;

import org.onap.dcaegen2.services.sdk.rest.services.model.DmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/4/18
 */

class DMaaPPublisherReactiveHttpClientTest {

    private DMaaPPublisherReactiveHttpClient dmaapPublisherReactiveHttpClient;
    private DmaapPublisherConfiguration dmaapPublisherConfigurationMock = mock(DmaapPublisherConfiguration.class);

    private RestTemplate restTemplate = mock(RestTemplate.class);

    private DmaapModel dmaapModel = mock(DmaapModel.class);
    private JsonBodyBuilder<DmaapModel> jsonBodyBuilder = mock(JsonBodyBuilder.class);


    @BeforeEach
    void setUp() {
        when(dmaapPublisherConfigurationMock.dmaapHostName()).thenReturn("54.45.33.2");
        when(dmaapPublisherConfigurationMock.dmaapProtocol()).thenReturn("https");
        when(dmaapPublisherConfigurationMock.dmaapPortNumber()).thenReturn(1234);
        when(dmaapPublisherConfigurationMock.dmaapUserName()).thenReturn("PRH");
        when(dmaapPublisherConfigurationMock.dmaapUserPassword()).thenReturn("PRH");
        when(dmaapPublisherConfigurationMock.dmaapContentType()).thenReturn("application/json");
        when(dmaapPublisherConfigurationMock.dmaapTopicName()).thenReturn("unauthenticated.PNF_READY");

        when(jsonBodyBuilder.createJsonBody(dmaapModel)).thenReturn(
                "{\"correlationId\":\"NOKnhfsadhff\"," +
                        "\"ipaddress-v4\":\"256.22.33.155\", " +
                        "\"ipaddress-v6\":\"200J:0db8:85a3:0000:0000:8a2e:0370:7334\"}");

        dmaapPublisherReactiveHttpClient =
                new DMaaPPublisherReactiveHttpClient(dmaapPublisherConfigurationMock, Mono.just(restTemplate),jsonBodyBuilder);
    }

    @Test
    void getHttpResponse_Success() {
        //given
        int responseSuccess = 200;
        ResponseEntity<String> mockedResponseEntity = mock(ResponseEntity.class);
        //when
        when(mockedResponseEntity.getStatusCode()).thenReturn(HttpStatus.valueOf(responseSuccess));
        doReturn(mockedResponseEntity).when(restTemplate)
                .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), (Class<Object>) any());

        //then
        StepVerifier.create(dmaapPublisherReactiveHttpClient.getDMaaPProducerResponse(dmaapModel))
                .expectSubscription().expectNext(mockedResponseEntity).verifyComplete();
    }

    @Test
    void getAppropriateUri_whenPassingCorrectedPathForPnf() {
        Assertions.assertEquals(dmaapPublisherReactiveHttpClient.getUri(),
                URI.create("https://54.45.33.2:1234/unauthenticated.PNF_READY"));
    }
}