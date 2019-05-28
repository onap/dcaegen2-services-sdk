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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.Gson;
import io.netty.buffer.ByteBufAllocator;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpHeaders;
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
    private static final JsonParser parser = new JsonParser();
    private static final List<String> textPlainFirstMessageBatchItems = Arrays.asList("I", "like", "cookies");
    private static final List<String> textPlainSecondMessageBatchItems = Arrays.asList("and", "pierogi");
    private static final List<String> textPlainLongMessageBatchItems = Stream.concat(
            textPlainFirstMessageBatchItems.stream(),
            textPlainSecondMessageBatchItems.stream())
            .collect(Collectors.toList());
    private static final List<String> jsonFirstMessageBatchItems = Arrays
            .asList("{\"message\":\"I\"}", "{\"message\":\"like\"}", "{\"message\":\"cookies\"}");
    private static final List<String> jsonSecondMessageBatchItems = Arrays
            .asList("{\"message\":\"and\"}", "{\"message\":\"pierogi\"}");
    private static final List<String> jsonLongMessageBatchItems = Stream.concat(
            jsonFirstMessageBatchItems.stream(),
            jsonSecondMessageBatchItems.stream())
            .collect(Collectors.toList());
    private static final Flux<JsonPrimitive> textPlainShortMessageBatch = Flux
            .fromIterable(textPlainFirstMessageBatchItems).map(JsonPrimitive::new);
    private static final Flux<JsonPrimitive> textPlainLongMessageBatch = Flux
            .fromIterable(textPlainLongMessageBatchItems).map(JsonPrimitive::new);
    private static final Flux<JsonObject> jsonShortMessageBatch = Flux
            .fromIterable(jsonFirstMessageBatchItems)
            .map(parser::parse).map(JsonElement::getAsJsonObject);
    private static final Flux<JsonObject> jsonLongMessageBatch = Flux
            .fromIterable(jsonLongMessageBatchItems)
            .map(parser::parse).map(JsonElement::getAsJsonObject);
    private final RxHttpClient httpClient = mock(RxHttpClient.class);
    private final MessageRouterPublisher cut = new MessageRouterPublisherImpl(httpClient, 3, Duration.ofMinutes(1));

    private final ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    private final MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
            .name("the topic")
            .topicUrl("https://dmaap-mr/TOPIC")
            .build();
    private final MessageRouterPublishRequest mrRequestTextPlain = ImmutableMessageRouterPublishRequest
            .builder()
            .sinkDefinition(sinkDefinition)
            .contentType(ContentType.TEXT_PLAIN.toString())
            .build();
    private final MessageRouterPublishRequest mrRequestJson = ImmutableMessageRouterPublishRequest
            .builder()
            .sinkDefinition(sinkDefinition)
            .contentType(ContentType.APPLICATION_JSON.toString())
            .build();
    private final HttpResponse successHttpResponse = ImmutableHttpResponse.builder()
            .statusCode(200)
            .statusReason("OK")
            .url(sinkDefinition.topicUrl())
            .rawBody("[]".getBytes())
            .build();
    private final HttpResponse badRequestResponse = ImmutableHttpResponse.builder()
            .statusCode(400)
            .url(sinkDefinition.topicUrl())
            .statusReason("Object should start with {")
            .rawBody("[]".getBytes())
            .build();

    @Test
    void puttingElementsShouldYieldNonChunkedHttpRequest() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestTextPlain, textPlainShortMessageBatch);
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
    void puttingLowNumberOfElementsAsTextPlainShouldYieldSingleHttpRequest() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestTextPlain, textPlainShortMessageBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        final List<String> elementsInRequest = extractNonEmptyTextPlainRequestBody(httpRequest);
        assertThat(elementsInRequest.size()).isEqualTo(3);
        assertThat(elementsInRequest.get(0)).isEqualTo(textPlainFirstMessageBatchItems.get(0));
        assertThat(elementsInRequest.get(1)).isEqualTo(textPlainFirstMessageBatchItems.get(1));
        assertThat(elementsInRequest.get(2)).isEqualTo(textPlainFirstMessageBatchItems.get(2));
    }

    @Test
    void puttingLowNumberOfElementsAsJsonShouldYieldSingleHttpRequest() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestJson, jsonShortMessageBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        final JsonArray elementsInRequest = extractNonEmptyJsonRequestBody(httpRequest);
        assertThat(elementsInRequest.size()).isEqualTo(3);
        assertThat(elementsInRequest.get(0).toString()).isEqualTo(jsonFirstMessageBatchItems.get(0));
        assertThat(elementsInRequest.get(1).toString()).isEqualTo(jsonFirstMessageBatchItems.get(1));
        assertThat(elementsInRequest.get(2).toString()).isEqualTo(jsonFirstMessageBatchItems.get(2));
    }

    @Test
    void puttingLowNumberOfElementsAsTextPlainShouldReturnSingleResponse() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestTextPlain, textPlainShortMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new JsonPrimitive(textPlainFirstMessageBatchItems.get(0)),
                            new JsonPrimitive(textPlainFirstMessageBatchItems.get(1)),
                            new JsonPrimitive(textPlainFirstMessageBatchItems.get(2)));
                })
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void puttingLowNumberOfElementsAsJsonShouldReturnSingleResponse() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestJson, jsonShortMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new Gson().fromJson(jsonFirstMessageBatchItems.get(0), JsonObject.class),
                            new Gson().fromJson(jsonFirstMessageBatchItems.get(1), JsonObject.class),
                            new Gson().fromJson(jsonFirstMessageBatchItems.get(2), JsonObject.class));
                })
                .expectComplete()
                .verify(TIMEOUT);
    }


    @Test
    void puttingHighNumberOfElementsAsTextPlainShouldYieldMultipleHttpRequests() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestTextPlain, textPlainLongMessageBatch);

        // then
        responses.then().block();

        verify(httpClient, times(2)).call(httpRequestArgumentCaptor.capture());
        final List<HttpRequest> httpRequests = httpRequestArgumentCaptor.getAllValues();
        assertThat(httpRequests.size()).describedAs("number of requests").isEqualTo(2);

        final List<String> firstRequest = extractNonEmptyTextPlainRequestBody(httpRequests.get(0));
        assertThat(firstRequest.size()).isEqualTo(3);
        assertThat(firstRequest.get(0)).isEqualTo(textPlainFirstMessageBatchItems.get(0));
        assertThat(firstRequest.get(1)).isEqualTo(textPlainFirstMessageBatchItems.get(1));
        assertThat(firstRequest.get(2)).isEqualTo(textPlainFirstMessageBatchItems.get(2));

        final List<String> secondRequest = extractNonEmptyTextPlainRequestBody(httpRequests.get(1));
        assertThat(secondRequest.size()).isEqualTo(2);
        assertThat(secondRequest.get(0)).isEqualTo(textPlainSecondMessageBatchItems.get(0));
        assertThat(secondRequest.get(1)).isEqualTo(textPlainSecondMessageBatchItems.get(1));
    }

    @Test
    void puttingHighNumberOfElementsAsJsonShouldYieldMultipleHttpRequests() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestJson, jsonLongMessageBatch);
        // then
        responses.then().block();

        verify(httpClient, times(2)).call(httpRequestArgumentCaptor.capture());
        final List<HttpRequest> httpRequests = httpRequestArgumentCaptor.getAllValues();
        assertThat(httpRequests.size()).describedAs("number of requests").isEqualTo(2);

        final JsonArray firstRequest = extractNonEmptyJsonRequestBody(httpRequests.get(0));
        assertThat(firstRequest.size()).isEqualTo(3);
        assertThat(firstRequest.get(0).toString()).isEqualTo(jsonFirstMessageBatchItems.get(0));
        assertThat(firstRequest.get(1).toString()).isEqualTo(jsonFirstMessageBatchItems.get(1));
        assertThat(firstRequest.get(2).toString()).isEqualTo(jsonFirstMessageBatchItems.get(2));

        final JsonArray secondRequest = extractNonEmptyJsonRequestBody(httpRequests.get(1));
        assertThat(secondRequest.size()).isEqualTo(2);
        assertThat(secondRequest.get(0).toString()).isEqualTo(jsonSecondMessageBatchItems.get(0));
        assertThat(secondRequest.get(1).toString()).isEqualTo(jsonSecondMessageBatchItems.get(1));
    }

    @Test
    void puttingHighNumberOfElementsAsTextPlainShouldReturnMoreResponses() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestTextPlain, textPlainLongMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new JsonPrimitive(textPlainFirstMessageBatchItems.get(0)),
                            new JsonPrimitive(textPlainFirstMessageBatchItems.get(1)),
                            new JsonPrimitive(textPlainFirstMessageBatchItems.get(2)));
                })
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new JsonPrimitive(textPlainSecondMessageBatchItems.get(0)),
                            new JsonPrimitive(textPlainSecondMessageBatchItems.get(1)));
                })
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void puttingHighNumberOfElementsAsJsonShouldReturnMoreResponses() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestJson, jsonLongMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new Gson().fromJson(jsonFirstMessageBatchItems.get(0), JsonObject.class),
                            new Gson().fromJson(jsonFirstMessageBatchItems.get(1), JsonObject.class),
                            new Gson().fromJson(jsonFirstMessageBatchItems.get(2), JsonObject.class));
                })
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new Gson().fromJson(jsonSecondMessageBatchItems.get(0), JsonObject.class),
                            new Gson().fromJson(jsonSecondMessageBatchItems.get(1), JsonObject.class));
                })
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void puttingTextPlainElementsWithJsonContentTypeShouldAddQuotesToMessages(){
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(badRequestResponse));
        final List<String> textPlainFirstMssgsWithExtraQuotes = Arrays
                .asList("\"I\"", "\"like\"", "\"cookies\"");

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestJson, textPlainShortMessageBatch);

        // then
        responses.then().block();

        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.headers().get(HttpHeaders.CONTENT_TYPE))
                .containsExactly(ContentType.APPLICATION_JSON.toString());

        final JsonArray elementsInRequest = extractNonEmptyJsonRequestBody(httpRequest);
        assertThat(elementsInRequest.size()).isEqualTo(3);
        assertThat(elementsInRequest.get(0).toString())
                .isEqualTo(textPlainFirstMssgsWithExtraQuotes.get(0));
        assertThat(elementsInRequest.get(1).toString())
                .isEqualTo(textPlainFirstMssgsWithExtraQuotes.get(1));
        assertThat(elementsInRequest.get(2).toString())
                .isEqualTo(textPlainFirstMssgsWithExtraQuotes.get(2));
    }

    @Test
    void puttingJsonElementsWithTextPlainContentTypeShouldRemoveInnerQuotesFromMessages(){
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));
        final List<String> jsonFirstMssgsWithoutQuotes = Arrays
                .asList("{message:I}", "{message:like}", "{message:cookies}");

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestTextPlain, jsonShortMessageBatch);

        // then
        responses.then().block();

        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.headers().get(HttpHeaders.CONTENT_TYPE))
                .containsExactly(ContentType.TEXT_PLAIN.toString());

        final List<String> elementsInRequest = extractNonEmptyTextPlainRequestBody(httpRequest);
        assertThat(elementsInRequest.size()).isEqualTo(3);
        assertThat(elementsInRequest.get(0)).isEqualTo(jsonFirstMssgsWithoutQuotes.get(0));
        assertThat(elementsInRequest.get(1)).isEqualTo(jsonFirstMssgsWithoutQuotes.get(1));
        assertThat(elementsInRequest.get(2)).isEqualTo(jsonFirstMssgsWithoutQuotes.get(2));
    }



    private String collectNonEmptyRequestBody(HttpRequest httpRequest){
        final String body = Flux.from(httpRequest.body().contents())
                .collect(ByteBufAllocator.DEFAULT::compositeBuffer,
                        (byteBufs, buffer) -> byteBufs.addComponent(true, buffer))
                .map(byteBufs -> byteBufs.toString(StandardCharsets.UTF_8))
                .block();
        assertThat(body).describedAs("request body").isNotBlank();

        return body;
    }

    private List<String> extractNonEmptyTextPlainRequestBody(HttpRequest httpRequest) {
        return Arrays.asList(collectNonEmptyRequestBody(httpRequest).split("\n"));
    }

    private JsonArray extractNonEmptyJsonRequestBody(HttpRequest httpRequest){
        return new Gson().fromJson(collectNonEmptyRequestBody(httpRequest), JsonArray.class);
    }


}