/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
 * Copyright (C) 2023 Deutsche Telekom AG. All rights reserved.
 * =========================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================
 */

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonSyntaxException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.vavr.collection.HashMultimap;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReasonPresenter;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */

@Disabled
class MessageRouterSubscriberImplTest {

    private static final String ERROR_MESSAGE = "Something went wrong";
    private final RxHttpClient httpClient = mock(RxHttpClient.class);
    private final ClientErrorReasonPresenter clientErrorReasonPresenter = mock(ClientErrorReasonPresenter.class);
    private final MessageRouterSubscriberConfig clientConfig = MessageRouterSubscriberConfig.createDefault();
    private final MessageRouterSubscriber
            cut;
    private final ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    private final MessageRouterSource sourceDefinition = ImmutableMessageRouterSource.builder()
            .name("sample topic")
            .topicUrl("https://dmaap-mr/TOPIC")
            .build();
    private final MessageRouterSubscribeRequest mrRequest = ImmutableMessageRouterSubscribeRequest.builder()
            .consumerGroup("SAMPLE-GROUP")
            .sourceDefinition(sourceDefinition)
            .build();
    private final HttpResponse httpResponse = ImmutableHttpResponse.builder()
            .statusCode(200)
            .statusReason("OK")
            .url(sourceDefinition.topicUrl())
            .rawBody("[]".getBytes())
            .headers(HashMultimap.withSeq().empty())
            .build();
    private final HttpResponse retryableHttpResponse = ImmutableHttpResponse.builder()
            .statusCode(500)
            .statusReason("Something braked")
            .url(sourceDefinition.topicUrl())
            .rawBody("[]".getBytes())
            .headers(HashMultimap.withSeq().empty())
            .build();
    private final HttpResponse httpResponseWithWrongStatusCode = ImmutableHttpResponse.builder()
            .statusCode(301)
            .statusReason("Something braked")
            .url(sourceDefinition.topicUrl())
            .rawBody("[]".getBytes())
            .headers(HashMultimap.withSeq().empty())
            .build();
    private final HttpResponse httpResponseWithIncorrectJson = ImmutableHttpResponse.builder()
            .statusCode(200)
            .statusReason("OK")
            .url(sourceDefinition.topicUrl())
            .rawBody("{}".getBytes())
            .headers(HashMultimap.withSeq().empty())
            .build();
    private MessageRouterSubscriberImplTest() throws Exception{
        cut = new MessageRouterSubscriberImpl(httpClient, clientConfig.gsonInstance(),clientErrorReasonPresenter);
    }
    
    @Test
    void getWithProperRequest_shouldReturnCorrectResponse() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponse));

        // when
        final Mono<MessageRouterSubscribeResponse> responses = cut
                .get(mrRequest);
        final MessageRouterSubscribeResponse response = responses.block();

        // then
        assertThat(response.successful()).isTrue();
        assertThat(response.failReason()).isNull();
        assertThat(response.hasElements()).isFalse();


        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.method()).isEqualTo(HttpMethod.GET);
        assertThat(httpRequest.url()).isEqualTo(String.format("%s/%s/%s", sourceDefinition.topicUrl(),
                mrRequest.consumerGroup(), mrRequest.consumerId()));
        assertThat(httpRequest.body()).isNull();
    }

    @Test
    void getWithProperRequestButNotSuccessfulHttpRequest_shouldReturnMonoWithFailReason() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponseWithWrongStatusCode));

        // when
        final Mono<MessageRouterSubscribeResponse> responses = cut
                .get(mrRequest);
        final MessageRouterSubscribeResponse response = responses.block();

        // then
        assertThat(response.failed()).isTrue();
        assertThat(response.failReason()).
                isEqualTo(String.format("%d %s%n%s", httpResponseWithWrongStatusCode.statusCode(),
                        httpResponseWithWrongStatusCode.statusReason(),
                        httpResponseWithWrongStatusCode.bodyAsString()));
    }

    @Test
    void getWithImproperRawBody_shouldThrowNPE() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponseWithIncorrectJson));

        // when
        // then
        assertThatExceptionOfType(JsonSyntaxException.class).isThrownBy(() -> cut.get(mrRequest).block());
    }

    @Test
    void getWithProperRequest_shouldReturnTimeoutError() {

        // given
        given(clientErrorReasonPresenter.present(any()))
                .willReturn(ERROR_MESSAGE);
        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.error(ReadTimeoutException.INSTANCE));

        // when
        final Mono<MessageRouterSubscribeResponse> responses = cut
                .get(mrRequest);
        final MessageRouterSubscribeResponse response = responses.block();

        // then
        assertThat(response.failed()).isTrue();
        assertThat(response.failReason()).isEqualTo(ERROR_MESSAGE);
        assertThat(response.hasElements()).isFalse();

        verify(clientErrorReasonPresenter, times(1)).present(any());
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.method()).isEqualTo(HttpMethod.GET);
        assertThat(httpRequest.url()).isEqualTo(String.format("%s/%s/%s", sourceDefinition.topicUrl(),
                mrRequest.consumerGroup(), mrRequest.consumerId()));
        assertThat(httpRequest.body()).isNull();
    }

    @Test
    void getWithProperRequest_shouldReturnConnectionException() {
        given(clientErrorReasonPresenter.present(any()))
                .willReturn(ERROR_MESSAGE);
        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.error(new ConnectException()));

        // when
        final Mono<MessageRouterSubscribeResponse> responses = cut
                .get(mrRequest);
        final MessageRouterSubscribeResponse response = responses.block();

        assertThat(response.failed()).isTrue();
        assertThat(response.failReason()).isEqualTo(ERROR_MESSAGE);
        assertThat(response.hasElements()).isFalse();

        verify(clientErrorReasonPresenter, times(1)).present(any());
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.method()).isEqualTo(HttpMethod.GET);
        assertThat(httpRequest.url()).isEqualTo(String.format("%s/%s/%s", sourceDefinition.topicUrl(),
                mrRequest.consumerGroup(), mrRequest.consumerId()));
        assertThat(httpRequest.body()).isNull();
    }

    @Test
    void getWithProperRequest_shouldReturnCertainFailedResponse() {
        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.just(retryableHttpResponse));

        // when
        final Mono<MessageRouterSubscribeResponse> responses = cut
                .get(mrRequest);
        final MessageRouterSubscribeResponse response = responses.block();

        assertThat(response.failed()).isTrue();
        assertThat(response.failReason()).startsWith("500 Something braked");
        assertThat(response.hasElements()).isFalse();

        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.method()).isEqualTo(HttpMethod.GET);
        assertThat(httpRequest.url()).isEqualTo(String.format("%s/%s/%s", sourceDefinition.topicUrl(),
                mrRequest.consumerGroup(), mrRequest.consumerId()));
        assertThat(httpRequest.body()).isNull();
    }
}
