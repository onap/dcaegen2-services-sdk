/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.verification.VerificationMode;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since April 2019
 */
class MessageRouterPublisherImplTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private final RxHttpClient httpClient = mock(RxHttpClient.class);
    private final MessageRouterPublisher cut = new MessageRouterPublisherImpl(httpClient, 3, Duration.ofMinutes(1));

    private final ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    private final MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
            .name("the topic")
            .topicUrl("https://dmaap-mr/TOPIC")
            .build();
    private final MessageRouterPublishRequest mrRequest = ImmutableMessageRouterPublishRequest.builder()
            .sinkDefinition(sinkDefinition)
            .build();
    private final HttpResponse httpResponse = ImmutableHttpResponse.builder()
            .statusCode(200)
            .statusReason("OK")
            .url(sinkDefinition.topicUrl())
            .rawBody("{}".getBytes())
            .build();

    @Test
    void puttingElementsShouldYieldNonChunkedHttpRequest() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequest, Flux.just("I", "like", "cookies").map(JsonPrimitive::new));
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.method()).isEqualTo(HttpMethod.POST);
        assertThat(httpRequest.url()).isEqualTo(sinkDefinition.topicUrl());
        assertThat(httpRequest.body()).isNotNull();
        assertThat(httpRequest.body().length()).isGreaterThan(0);
    }

    @Test
    void puttingLowNumberOfElementsShouldYieldSingleHttpRequest() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequest, Flux.just("I", "like", "cookies").map(JsonPrimitive::new));
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        final JsonArray elementsInRequest = extractNonEmptyRequestBody(httpRequest);
        assertThat(elementsInRequest.size()).isEqualTo(3);
        assertThat(elementsInRequest.get(0).getAsString()).isEqualTo("I");
        assertThat(elementsInRequest.get(1).getAsString()).isEqualTo("like");
        assertThat(elementsInRequest.get(2).getAsString()).isEqualTo("cookies");
    }

    @Test
    void puttingLowNumberOfElementsShouldReturnSingleResponse() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequest, Flux.just("I", "like", "cookies").map(JsonPrimitive::new));

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new JsonPrimitive("I"),
                            new JsonPrimitive("like"),
                            new JsonPrimitive("cookies"));
                })
                .expectComplete()
                .verify(TIMEOUT);
    }


    @Test
    void puttingHighNumberOfElementsShouldYieldMultipleHttpRequests() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequest, Flux.just("I", "like", "cookies", "and", "pierogi").map(JsonPrimitive::new));

        // then
        responses.then().block();

        verify(httpClient, times(2)).call(httpRequestArgumentCaptor.capture());
        final List<HttpRequest> httpRequests = httpRequestArgumentCaptor.getAllValues();
        assertThat(httpRequests.size()).describedAs("number of requests").isEqualTo(2);

        final JsonArray firstRequest = extractNonEmptyRequestBody(httpRequests.get(0));
        assertThat(firstRequest.size()).isEqualTo(3);
        assertThat(firstRequest.get(0).getAsString()).isEqualTo("I");
        assertThat(firstRequest.get(1).getAsString()).isEqualTo("like");
        assertThat(firstRequest.get(2).getAsString()).isEqualTo("cookies");

        final JsonArray secondRequest = extractNonEmptyRequestBody(httpRequests.get(1));
        assertThat(secondRequest.size()).isEqualTo(2);
        assertThat(secondRequest.get(0).getAsString()).isEqualTo("and");
        assertThat(secondRequest.get(1).getAsString()).isEqualTo("pierogi");
    }

    @Test
    void puttingHighNumberOfElementsShouldReturnMoreResponses() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequest, Flux.just("I", "like", "cookies", "and", "pierogi").map(JsonPrimitive::new));

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new JsonPrimitive("I"),
                            new JsonPrimitive("like"),
                            new JsonPrimitive("cookies"));
                })
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new JsonPrimitive("and"),
                            new JsonPrimitive("pierogi"));
                })
                .expectComplete()
                .verify(TIMEOUT);
    }

    private JsonArray extractNonEmptyRequestBody(HttpRequest httpRequest) {
        final String body = Flux.from(httpRequest.body().contents())
                .collect(ByteBufAllocator.DEFAULT::compositeBuffer,
                        (byteBufs, buffer) -> byteBufs.addComponent(true, buffer))
                .map(byteBufs -> byteBufs.toString(StandardCharsets.UTF_8))
                .block();
        assertThat(body).describedAs("request body").isNotBlank();
        return new Gson().fromJson(body, JsonArray.class);
    }
}