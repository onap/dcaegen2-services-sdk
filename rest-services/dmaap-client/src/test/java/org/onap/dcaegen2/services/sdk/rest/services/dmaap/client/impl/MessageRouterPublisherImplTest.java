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
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vavr.collection.List;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpHeaders;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
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
    private static final String TOPIC_URL = "https://dmaap-mr/TOPIC";
    private static final int MAX_BATCH_SIZE = 3;
    private final RxHttpClient httpClient = mock(RxHttpClient.class);
    private final MessageRouterPublisher cut = new MessageRouterPublisherImpl(httpClient, MAX_BATCH_SIZE, Duration.ofMinutes(1));
    private final ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    private final MessageRouterPublishRequest plainPublishRequest = createPublishRequest(TOPIC_URL, ContentType.TEXT_PLAIN);
    private final MessageRouterPublishRequest jsonPublishRequest = createPublishRequest(TOPIC_URL);
    private final HttpResponse successHttpResponse = createHttpResponse("OK", 200);

    @Test
    void puttingElementsShouldYieldNonChunkedHttpRequest() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final Flux<JsonObject> singleJsonMessageBatch = jsonBatch(threeJsonMessages);
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(jsonPublishRequest, singleJsonMessageBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.method()).isEqualTo(HttpMethod.POST);
        assertThat(httpRequest.url()).isEqualTo(TOPIC_URL);
        assertThat(httpRequest.body()).isNotNull();
        assertThat(httpRequest.body().length()).isGreaterThan(0);
    }

    @Test
    void onPut_givenJsonMessages_whenTheirAmountIsNotAboveMaxBatchSize_shouldSendSingleHttpRequest(){
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);

        final Flux<JsonObject> jsonMessagesMaxBatch = jsonBatch(threeJsonMessages);
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(jsonPublishRequest, jsonMessagesMaxBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        final JsonArray elementsInRequest = extractNonEmptyJsonRequestBody(httpRequest);

        assertThat(elementsInRequest.size()).describedAs("Http request batch size")
                .isEqualTo(MAX_BATCH_SIZE);
        assertListsContainSameElements(elementsInRequest, parsedThreeMessages);
    }



    @Test
    void onPut_givenJsonMessagesWithPlainContentType_whenTheirAmountIsNotAboveMaxBatchSize_shouldSendSingleHttpRequest(){
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);

        final Flux<JsonElement> plainMessagesMaxBatch = plainBatch(threeJsonMessages);
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, plainMessagesMaxBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        final List<JsonObject> elementsInRequest = extractNonEmptyPlainRequestBody(httpRequest)
                .map(JsonElement::getAsJsonObject);


        assertThat(elementsInRequest.size()).describedAs("Http request batch size")
                .isEqualTo(MAX_BATCH_SIZE);
        assertListsContainSameElements(elementsInRequest, parsedThreeMessages);
    }

    @Test
    void onPut_givenPlainMessages_whenTheirAmountIsNotAboveMaxBatchSize_shouldSendSingleHttpRequest(){
        // given
        final List<String> threePlainMessages = List.of("I", "like", "cookies");
        final List<JsonPrimitive> parsedThreeMessages = getAsJsonPrimitives(threePlainMessages);

        final Flux<JsonElement> plainMessagesMaxBatch = plainBatch(threePlainMessages);
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, plainMessagesMaxBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        final List<JsonPrimitive> elementsInRequest = extractNonEmptyPlainRequestBody(httpRequest)
                .map(JsonElement::getAsJsonPrimitive);

        assertThat(elementsInRequest.size()).describedAs("Http request batch size")
                .isEqualTo(MAX_BATCH_SIZE);
        assertListsContainSameElements(elementsInRequest, parsedThreeMessages);
    }

    @Test
    void puttingElementsWithoutContentTypeSetShouldUseApplicationJson(){
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final Flux<JsonObject> singleJsonMessageBatch = jsonBatch(threeJsonMessages);
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(jsonPublishRequest, singleJsonMessageBatch);
        responses.then().block();

        // then
        verify(httpClient).call(httpRequestArgumentCaptor.capture());
        final HttpRequest httpRequest = httpRequestArgumentCaptor.getValue();
        assertThat(httpRequest.headers().getOrElse(HttpHeaders.CONTENT_TYPE, ""))
                .isEqualTo(HttpHeaderValues.APPLICATION_JSON.toString());
    }

    @Test
    void onPut_givenJsonMessages_whenTheirAmountIsNotAboveMaxBatchSize_shouldReturnSingleResponse() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);

        final Flux<JsonObject> jsonMessagesMaxBatch = jsonBatch(threeJsonMessages);
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(jsonPublishRequest, jsonMessagesMaxBatch);

        // then
        verifySingleResponse(parsedThreeMessages, responses);
    }

    @Test
    void onPut_givenJsonMessagesWithPlainContentType_whenTheirAmountIsNotAboveMaxBatchSize_shouldReturnSingleResponse() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);

        final Flux<JsonElement> plainMessagesMaxBatch = plainBatch(threeJsonMessages);
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, plainMessagesMaxBatch);

        // then
        verifySingleResponse(parsedThreeMessages, responses);
    }

    @Test
    void onPut_givenPlainMessages_whenTheirAmountIsNotAboveMaxBatchSize_shouldReturnSingleResponse() {
        // given
        final List<String> threePlainMessages = List.of("I", "like", "cookies");
        final List<JsonPrimitive> parsedThreeMessages = getAsJsonPrimitives(threePlainMessages);

        final Flux<JsonElement> plainMessagesMaxBatch = plainBatch(threePlainMessages);
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, plainMessagesMaxBatch);

        // then
        verifySingleResponse(parsedThreeMessages, responses);
    }

    @Test
    void onPut_givenJsonMessages_whenTheirAmountIsAboveMaxBatchSize_shouldYieldMultipleHttpRequests() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<String> twoJsonMessages = getAsMRJsonMessages(List.of("and", "pierogi"));

        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);
        final List<JsonObject> parsedTwoMessages = getAsJsonObjects(twoJsonMessages);

        final Flux<JsonObject> doubleJsonMessageBatch = jsonBatch(
                threeJsonMessages.appendAll(twoJsonMessages));
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(jsonPublishRequest, doubleJsonMessageBatch);
        // then
        responses.then().block();

        verify(httpClient, times(2)).call(httpRequestArgumentCaptor.capture());
        final List<HttpRequest> httpRequests = List.ofAll(httpRequestArgumentCaptor.getAllValues());
        assertThat(httpRequests.size()).describedAs("number of requests").isEqualTo(2);

        final JsonArray firstRequest = extractNonEmptyJsonRequestBody(httpRequests.get(0));
        assertThat(firstRequest.size()).describedAs("Http request first batch size")
                .isEqualTo(MAX_BATCH_SIZE);
        assertListsContainSameElements(firstRequest, parsedThreeMessages);

        final JsonArray secondRequest = extractNonEmptyJsonRequestBody(httpRequests.get(1));
        assertThat(secondRequest.size()).describedAs("Http request second batch size")
                .isEqualTo(MAX_BATCH_SIZE-1);
        assertListsContainSameElements(secondRequest, parsedTwoMessages);
    }

    @Test
    void onPut_givenJsonMessagesWithPlainContentType_whenTheirAmountIsAboveMaxBatchSize_shouldYieldMultipleHttpRequests() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<String> twoJsonMessages = getAsMRJsonMessages(List.of("and", "pierogi"));

        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);
        final List<JsonObject> parsedTwoMessages = getAsJsonObjects(twoJsonMessages);

        final Flux<JsonElement> doublePlainMessageBatch = plainBatch(
                threeJsonMessages.appendAll(twoJsonMessages));
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, doublePlainMessageBatch);
        // then
        responses.then().block();

        verify(httpClient, times(2)).call(httpRequestArgumentCaptor.capture());
        final List<HttpRequest> httpRequests = List.ofAll(httpRequestArgumentCaptor.getAllValues());
        assertThat(httpRequests.size()).describedAs("number of requests").isEqualTo(2);

        final List<JsonObject> firstRequest = extractNonEmptyPlainRequestBody(httpRequests.get(0))
                .map(JsonElement::getAsJsonObject);
        assertThat(firstRequest.size()).describedAs("Http request first batch size")
                .isEqualTo(MAX_BATCH_SIZE);
        assertListsContainSameElements(firstRequest, parsedThreeMessages);

        final List<JsonObject> secondRequest = extractNonEmptyPlainRequestBody(httpRequests.get(1))
                .map(JsonElement::getAsJsonObject);
        assertThat(secondRequest.size()).describedAs("Http request second batch size")
                .isEqualTo(MAX_BATCH_SIZE-1);
        assertListsContainSameElements(secondRequest, parsedTwoMessages);
    }

    @Test
    void onPut_givenPlainMessages_whenTheirAmountIsAboveMaxBatchSize_shouldYieldMultipleHttpRequests() {
        // given
        final List<String> threePlainMessages = List.of("I", "like", "cookies");
        final List<String> twoPlainMessages = List.of("and", "pierogi");

        final List<JsonPrimitive> parsedThreePlainMessages = getAsJsonPrimitives(threePlainMessages);
        final List<JsonPrimitive> parsedTwoPlainMessages = getAsJsonPrimitives(twoPlainMessages);

        final Flux<JsonElement> doublePlainMessageBatch = plainBatch(
                threePlainMessages.appendAll(twoPlainMessages));
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, doublePlainMessageBatch);
        // then
        responses.then().block();

        verify(httpClient, times(2)).call(httpRequestArgumentCaptor.capture());
        final List<HttpRequest> httpRequests = List.ofAll(httpRequestArgumentCaptor.getAllValues());
        assertThat(httpRequests.size()).describedAs("number of requests").isEqualTo(2);

        final List<JsonPrimitive> firstRequest = extractNonEmptyPlainRequestBody(httpRequests.get(0))
                .map(JsonElement::getAsJsonPrimitive);
        assertThat(firstRequest.size()).describedAs("Http request first batch size")
                .isEqualTo(MAX_BATCH_SIZE);
        assertListsContainSameElements(firstRequest, parsedThreePlainMessages);

        final List<JsonPrimitive> secondRequest = extractNonEmptyPlainRequestBody(httpRequests.get(1))
                .map(JsonElement::getAsJsonPrimitive);
        assertThat(secondRequest.size()).describedAs("Http request second batch size")
                .isEqualTo(MAX_BATCH_SIZE-1);
        assertListsContainSameElements(secondRequest, parsedTwoPlainMessages);
    }

    @Test
    void onPut_givenJsonMessages_whenTheirAmountIsAboveMaxBatchSize_shouldReturnMoreResponses() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<String> twoJsonMessages = getAsMRJsonMessages(List.of("and", "pierogi"));

        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);
        final List<JsonObject> parsedTwoMessages = getAsJsonObjects(twoJsonMessages);

        final Flux<JsonObject> doubleJsonMessageBatch = jsonBatch(threeJsonMessages.appendAll(twoJsonMessages));
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(jsonPublishRequest, doubleJsonMessageBatch);

        // then
        verifyDoubleResponse(parsedThreeMessages, parsedTwoMessages, responses);
    }

    @Test
    void onPut_givenJsonMessagesWithPlainContentType_whenTheirAmountIsAboveMaxBatchSize_shouldReturnMoreResponses() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<String> twoJsonMessages = getAsMRJsonMessages(List.of("and", "pierogi"));

        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);
        final List<JsonObject> parsedTwoMessages = getAsJsonObjects(twoJsonMessages);

        final Flux<JsonElement> doubleJsonMessageBatch = plainBatch(threeJsonMessages.appendAll(twoJsonMessages));
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, doubleJsonMessageBatch);

        // then
        verifyDoubleResponse(parsedThreeMessages, parsedTwoMessages, responses);
    }

    @Test
    void onPut_givenPlainMessages_whenTheirAmountIsAboveMaxBatchSize_shouldReturnMoreResponses() {
        // given
        final List<String> threePlainMessages = List.of("I", "like", "cookies");
        final List<String> twoPlainMessages = List.of("and", "pierogi");

        final List<JsonPrimitive> parsedThreeMessages = getAsJsonPrimitives(threePlainMessages);
        final List<JsonPrimitive> parsedTwoMessages = getAsJsonPrimitives(twoPlainMessages);

        final Flux<JsonElement> doublePlainMessageBatch = plainBatch(
                threePlainMessages.appendAll(twoPlainMessages));
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, doublePlainMessageBatch);

        // then
        verifyDoubleResponse(parsedThreeMessages, parsedTwoMessages, responses);
    }

    private static List<String> getAsMRJsonMessages(List<String> plainTextMessages){
        return plainTextMessages
                .map(message -> String.format("{\"message\":\"%s\"}", message));
    }

    private static HttpResponse createHttpResponse(String statusReason, int statusCode){
        return ImmutableHttpResponse.builder()
                .statusCode(statusCode)
                .url(TOPIC_URL)
                .statusReason(statusReason)
                .rawBody("[]".getBytes())
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

    private JsonArray extractNonEmptyJsonRequestBody(HttpRequest httpRequest){
        return new Gson().fromJson(collectNonEmptyRequestBody(httpRequest), JsonArray.class);
    }

    private List<JsonElement> extractNonEmptyPlainRequestBody(HttpRequest httpRequest){
        return getAsJsonElements(
                List.of(
                        collectNonEmptyRequestBody(httpRequest)
                                .split("\n")
                )
        );
    }

    private void assertListsContainSameElements(List<? extends  JsonElement> actualMessages,
            List<? extends JsonElement> expectedMessages){
        for (int i = 0; i < actualMessages.size(); i++) {
            assertThat(actualMessages.get(i))
                    .describedAs(String.format("Http request element at position %d", i))
                    .isEqualTo(expectedMessages.get(i));
        }
    }

    private void assertListsContainSameElements(JsonArray actualMessages,
            List<? extends JsonElement> expectedMessages){
        assertThat(actualMessages.size()).describedAs("Http request batch size")
                .isEqualTo(expectedMessages.size());

        for (int i = 0; i < actualMessages.size(); i++) {
            assertThat(actualMessages.get(i))
                    .describedAs(String.format("Http request element at position %d", i))
                    .isEqualTo(expectedMessages.get(i));
        }
    }

    private void verifySingleResponse(List<? extends JsonElement> threeMessages,
            Flux<MessageRouterPublishResponse> responses) {
        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            threeMessages.get(0),
                            threeMessages.get(1),
                            threeMessages.get(2));
                })
                .expectComplete()
                .verify(TIMEOUT);
    }

    private void verifyDoubleResponse(List<? extends JsonElement> threeMessages,
            List<? extends JsonElement> twoMessages, Flux<MessageRouterPublishResponse> responses) {

        StepVerifier.create(responses)
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            threeMessages.get(0),
                            threeMessages.get(1),
                            threeMessages.get(2));
                })
                .consumeNextWith(response -> {
                    assertThat(response.successful()).describedAs("successful").isTrue();
                    assertThat(response.items()).containsExactly(
                            twoMessages.get(0),
                            twoMessages.get(1));
                })
                .expectComplete()
                .verify(TIMEOUT);
    }
}