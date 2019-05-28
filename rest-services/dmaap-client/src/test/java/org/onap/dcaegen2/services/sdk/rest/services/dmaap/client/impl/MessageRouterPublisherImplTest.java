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
import java.util.ArrayList;
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
    private static final List<String> threePlainTextMessages = Arrays.asList("I", "like", "cookies");
    private static final List<String> twoPlainTextMessages = Arrays.asList("and", "pierogi");
    private static final List<String> twoJsonMessages = getAsMRJsonMessages(twoPlainTextMessages);
    private static final List<String> threeJsonMessages = getAsMRJsonMessages(threePlainTextMessages);
    private static final Flux<JsonPrimitive> singlePlainMessageBatch = plainBatch(threePlainTextMessages);
    private static final Flux<JsonPrimitive> textPlainLongMessageBatch = plainBatch(concat(
            threePlainTextMessages, twoPlainTextMessages));
    private static final Flux<JsonObject> singleJsonMessageBatch = jsonBatch(threeJsonMessages);
    private static final Flux<JsonObject> doubleJsonMessageBatch = jsonBatch(concat(
            threeJsonMessages, twoJsonMessages));
    private static final MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
            .name("the topic")
            .topicUrl("https://dmaap-mr/TOPIC")
            .build();
    private final RxHttpClient httpClient = mock(RxHttpClient.class);
    private final MessageRouterPublisher cut = new MessageRouterPublisherImpl(httpClient, 3, Duration.ofMinutes(1));
    private final ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    private final MessageRouterPublishRequest mrRequestTextPlain = createMRRPublishRequest(ContentType.TEXT_PLAIN);
    private final MessageRouterPublishRequest mrRequestJson = createMRRPublishRequest(ContentType.APPLICATION_JSON);
    private final HttpResponse successHttpResponse = createHttpResponse("OK", 200);
    private final HttpResponse badRequestResponse = createHttpResponse("Object should start with {", 400);

    @Test
    void puttingElementsShouldYieldNonChunkedHttpRequest() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestTextPlain, singlePlainMessageBatch);
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
                .put(mrRequestTextPlain, singlePlainMessageBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        final List<String> elementsInRequest = extractNonEmptyTextPlainRequestBody(httpRequest);
        assertThat(elementsInRequest.size()).isEqualTo(3);
        assertThat(elementsInRequest.get(0)).isEqualTo(threePlainTextMessages.get(0));
        assertThat(elementsInRequest.get(1)).isEqualTo(threePlainTextMessages.get(1));
        assertThat(elementsInRequest.get(2)).isEqualTo(threePlainTextMessages.get(2));
    }

    @Test
    void puttingLowNumberOfElementsAsJsonShouldYieldSingleHttpRequest() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestJson, singleJsonMessageBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        final JsonArray elementsInRequest = extractNonEmptyJsonRequestBody(httpRequest);
        assertThat(elementsInRequest.size()).isEqualTo(3);
        assertThat(elementsInRequest.get(0).toString()).isEqualTo(threeJsonMessages.get(0));
        assertThat(elementsInRequest.get(1).toString()).isEqualTo(threeJsonMessages.get(1));
        assertThat(elementsInRequest.get(2).toString()).isEqualTo(threeJsonMessages.get(2));
    }

    @Test
    void puttingLowNumberOfElementsAsTextPlainShouldReturnSingleResponse() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestTextPlain, singlePlainMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new JsonPrimitive(threePlainTextMessages.get(0)),
                            new JsonPrimitive(threePlainTextMessages.get(1)),
                            new JsonPrimitive(threePlainTextMessages.get(2)));
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
                .put(mrRequestJson, singleJsonMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            getAsJsonObject(threeJsonMessages.get(0)),
                            getAsJsonObject(threeJsonMessages.get(1)),
                            getAsJsonObject(threeJsonMessages.get(2)));
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
        assertThat(firstRequest.get(0)).isEqualTo(threePlainTextMessages.get(0));
        assertThat(firstRequest.get(1)).isEqualTo(threePlainTextMessages.get(1));
        assertThat(firstRequest.get(2)).isEqualTo(threePlainTextMessages.get(2));

        final List<String> secondRequest = extractNonEmptyTextPlainRequestBody(httpRequests.get(1));
        assertThat(secondRequest.size()).isEqualTo(2);
        assertThat(secondRequest.get(0)).isEqualTo(twoPlainTextMessages.get(0));
        assertThat(secondRequest.get(1)).isEqualTo(twoPlainTextMessages.get(1));
    }

    @Test
    void puttingHighNumberOfElementsAsJsonShouldYieldMultipleHttpRequests() {
        // given
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(mrRequestJson, doubleJsonMessageBatch);
        // then
        responses.then().block();

        verify(httpClient, times(2)).call(httpRequestArgumentCaptor.capture());
        final List<HttpRequest> httpRequests = httpRequestArgumentCaptor.getAllValues();
        assertThat(httpRequests.size()).describedAs("number of requests").isEqualTo(2);

        final JsonArray firstRequest = extractNonEmptyJsonRequestBody(httpRequests.get(0));
        assertThat(firstRequest.size()).isEqualTo(3);
        assertThat(firstRequest.get(0).toString()).isEqualTo(threeJsonMessages.get(0));
        assertThat(firstRequest.get(1).toString()).isEqualTo(threeJsonMessages.get(1));
        assertThat(firstRequest.get(2).toString()).isEqualTo(threeJsonMessages.get(2));

        final JsonArray secondRequest = extractNonEmptyJsonRequestBody(httpRequests.get(1));
        assertThat(secondRequest.size()).isEqualTo(2);
        assertThat(secondRequest.get(0).toString()).isEqualTo(twoJsonMessages.get(0));
        assertThat(secondRequest.get(1).toString()).isEqualTo(twoJsonMessages.get(1));
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
                            new JsonPrimitive(threePlainTextMessages.get(0)),
                            new JsonPrimitive(threePlainTextMessages.get(1)),
                            new JsonPrimitive(threePlainTextMessages.get(2)));
                })
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            new JsonPrimitive(twoPlainTextMessages.get(0)),
                            new JsonPrimitive(twoPlainTextMessages.get(1)));
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
                .put(mrRequestJson, doubleJsonMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            getAsJsonObject(threeJsonMessages.get(0)),
                            getAsJsonObject(threeJsonMessages.get(1)),
                            getAsJsonObject(threeJsonMessages.get(2)));
                })
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            getAsJsonObject(twoJsonMessages.get(0)),
                            getAsJsonObject(twoJsonMessages.get(1)));
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
                .put(mrRequestJson, singlePlainMessageBatch);

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
                .put(mrRequestTextPlain, singleJsonMessageBatch);

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

    private static List<String> getAsMRJsonMessages(List<String> plainTextMessages){
        return plainTextMessages.stream()
                .map(message -> String.format("{\"message\":\"%s\"}", message))
                .collect(Collectors.toList());
    }

    private static Flux<JsonPrimitive> plainBatch(List<String> messages){
        return Flux.fromIterable(messages).map(JsonPrimitive::new);
    }

    private static Flux<JsonObject> jsonBatch(List<String> messages){
        return Flux.fromIterable(messages).map(parser::parse).map(JsonElement::getAsJsonObject);
    }

    private static List<String> concat(List<String> firstList, List<String> secondList){
        return Stream.concat(firstList.stream(), secondList.stream()).collect(Collectors.toList());
    }

    private static HttpResponse createHttpResponse(String statusReason, int statusCode){
        return ImmutableHttpResponse.builder()
                .statusCode(statusCode)
                .url(sinkDefinition.topicUrl())
                .statusReason(statusReason)
                .rawBody("[]".getBytes())
                .build();
    }

    private static MessageRouterPublishRequest createMRRPublishRequest(ContentType contentType){
        return ImmutableMessageRouterPublishRequest
                .builder()
                .sinkDefinition(sinkDefinition)
                .contentType(contentType.toString())
                .build();
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

    private JsonObject getAsJsonObject(String item){
        return new Gson().fromJson(item, JsonObject.class);
    }
}