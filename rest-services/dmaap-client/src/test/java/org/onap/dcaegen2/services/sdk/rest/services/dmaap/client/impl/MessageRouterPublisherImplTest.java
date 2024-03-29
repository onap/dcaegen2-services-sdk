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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.timeout.ReadTimeoutException;
import io.vavr.collection.HashMultimap;
import io.vavr.collection.List;

import org.junit.jupiter.api.Disabled;
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
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReasonPresenter;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.createPublishRequest;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.getAsJsonElements;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.getAsJsonObjects;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.getAsJsonPrimitives;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.jsonBatch;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.plainBatch;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since April 2019
 */
@Disabled
class MessageRouterPublisherImplTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String TOPIC_URL = "https://dmaap-mr/TOPIC";
    private static final int MAX_BATCH_SIZE = 3;
    private static final String ERROR_MESSAGE = "Something went wrong";
    private final RxHttpClient httpClient = mock(RxHttpClient.class);
    private final ClientErrorReasonPresenter clientErrorReasonPresenter = mock(ClientErrorReasonPresenter.class);
    private final MessageRouterPublisher cut;
    private final ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    private final MessageRouterPublishRequest plainPublishRequest = createPublishRequest(TOPIC_URL, ContentType.TEXT_PLAIN);
    private final MessageRouterPublishRequest jsonPublishRequest = createPublishRequest(TOPIC_URL);
    private final HttpResponse successHttpResponse = createHttpResponse("OK", 200);
    private final HttpResponse retryableHttpResponse = createHttpResponse("ERROR", 500);
    
    private MessageRouterPublisherImplTest()  throws Exception{
        cut = new MessageRouterPublisherImpl(
                httpClient, MAX_BATCH_SIZE, Duration.ofMinutes(1), clientErrorReasonPresenter);
    }
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
        assertThat(httpRequest.body().length()).isPositive();
    }

    @Test
    void onPut_givenJsonMessages_whenTheirAmountIsNotAboveMaxBatchSize_shouldSendSingleHttpRequest() {
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
    void onPut_givenJsonMessagesWithPlainContentType_whenTheirAmountIsNotAboveMaxBatchSize_shouldSendSingleHttpRequest() {
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
    void onPut_givenPlainMessages_whenTheirAmountIsNotAboveMaxBatchSize_shouldSendSingleHttpRequest() {
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
    void puttingElementsWithoutContentTypeSetShouldUseApplicationJson() {
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
                .isEqualTo(MAX_BATCH_SIZE - 1);
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
                .isEqualTo(MAX_BATCH_SIZE - 1);
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
                .isEqualTo(MAX_BATCH_SIZE - 1);
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

    @Test
    void onPut_whenReadTimeoutExceptionOccurs_shouldReturnOneTimeoutError() {
        // given
        final List<String> plainMessage = List.of("I", "like", "cookies");

        final Flux<JsonElement> plainMessagesMaxBatch = plainBatch(plainMessage);
        given(clientErrorReasonPresenter.present(any()))
                .willReturn(ERROR_MESSAGE);
        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.error(ReadTimeoutException.INSTANCE));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, plainMessagesMaxBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(this::assertFailedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void onPut_whenConnectionExceptionOccurs_shouldReturnOneConnectionException() {
        // given
        final List<String> plainMessage = List.of("I", "like", "cookies");

        final Flux<JsonElement> plainMessagesMaxBatch = plainBatch(plainMessage);
        given(clientErrorReasonPresenter.present(any()))
                .willReturn(ERROR_MESSAGE);
        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.error(new ConnectException()));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, plainMessagesMaxBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(this::assertFailedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void onPut_whenRetryableExceptionOccurs_shouldReturnCertainFailedResponse() {
        // given
        final List<String> plainMessage = List.of("I", "like", "cookies");

        final Flux<JsonElement> plainMessagesMaxBatch = plainBatch(plainMessage);
        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.just(retryableHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(plainPublishRequest, plainMessagesMaxBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(this::assertRetryableFailedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void onPut_whenReadTimeoutExceptionOccursForSecondBatch_shouldReturnOneCorrectResponseAndThenOneTimeoutError() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<String> twoJsonMessages = getAsMRJsonMessages(List.of("and", "pierogi"));

        final List<JsonObject> parsedThreeMessages = getAsJsonObjects(threeJsonMessages);

        final Flux<JsonObject> doubleJsonMessageBatch = jsonBatch(threeJsonMessages.appendAll(twoJsonMessages));
        given(clientErrorReasonPresenter.present(any()))
                .willReturn(ERROR_MESSAGE);
        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.just(successHttpResponse))
                .willReturn(Mono.error(ReadTimeoutException.INSTANCE));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(jsonPublishRequest, doubleJsonMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(response -> verifySuccessfulResponses(parsedThreeMessages, response))
                .consumeNextWith(this::assertFailedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void onPut_whenReadTimeoutExceptionOccursForFirstBatch_shouldReturnOneTimeoutErrorAndThenOneCorrectResponse() {
        // given
        final List<String> threeJsonMessages = getAsMRJsonMessages(List.of("I", "like", "cookies"));
        final List<String> twoJsonMessages = getAsMRJsonMessages(List.of("and", "pierogi"));

        final List<JsonObject> parsedTwoMessages = getAsJsonObjects(twoJsonMessages);

        final Flux<JsonObject> doubleJsonMessageBatch = jsonBatch(threeJsonMessages.appendAll(twoJsonMessages));
        given(clientErrorReasonPresenter.present(any()))
                .willReturn(ERROR_MESSAGE);
        given(httpClient.call(any(HttpRequest.class)))
                .willReturn(Mono.error(ReadTimeoutException.INSTANCE))
                .willReturn(Mono.just(successHttpResponse));

        // when
        final Flux<MessageRouterPublishResponse> responses = cut
                .put(jsonPublishRequest, doubleJsonMessageBatch);

        // then
        StepVerifier.create(responses)
                .consumeNextWith(this::assertFailedResponse)
                .consumeNextWith(response -> verifySuccessfulResponses(parsedTwoMessages, response))
                .expectComplete()
                .verify(TIMEOUT);
    }

    private static List<String> getAsMRJsonMessages(List<String> plainTextMessages) {
        return plainTextMessages
                .map(message -> String.format("{\"message\":\"%s\"}", message));
    }

    private static HttpResponse createHttpResponse(String statusReason, int statusCode) {
        return ImmutableHttpResponse.builder()
                .statusCode(statusCode)
                .url(TOPIC_URL)
                .statusReason(statusReason)
                .rawBody("[]".getBytes())
                .headers(HashMultimap.withSeq().empty())
                .build();
    }

    private String collectNonEmptyRequestBody(HttpRequest httpRequest) {
        final String body = Flux.from(httpRequest.body().contents())
                .collect(ByteBufAllocator.DEFAULT::compositeBuffer,
                        (byteBufs, buffer) -> byteBufs.addComponent(true, buffer))
                .map(byteBufs -> byteBufs.toString(StandardCharsets.UTF_8))
                .block();
        assertThat(body).describedAs("request body").isNotBlank();

        return body;
    }

    private JsonArray extractNonEmptyJsonRequestBody(HttpRequest httpRequest) {
        return new Gson().fromJson(collectNonEmptyRequestBody(httpRequest), JsonArray.class);
    }

    private List<JsonElement> extractNonEmptyPlainRequestBody(HttpRequest httpRequest) {
        return getAsJsonElements(
                List.of(
                        collectNonEmptyRequestBody(httpRequest)
                                .split("\n")
                )
        );
    }

    private void assertListsContainSameElements(List<? extends JsonElement> actualMessages,
                                                List<? extends JsonElement> expectedMessages) {
        for (int i = 0; i < actualMessages.size(); i++) {
            assertThat(actualMessages.get(i))
                    .describedAs(String.format("Http request element at position %d", i))
                    .isEqualTo(expectedMessages.get(i));
        }
    }

    private void assertListsContainSameElements(JsonArray actualMessages,
                                                List<? extends JsonElement> expectedMessages) {
        assertThat(actualMessages.size()).describedAs("Http request batch size")
                .isEqualTo(expectedMessages.size());

        for (int i = 0; i < actualMessages.size(); i++) {
            assertThat(actualMessages.get(i))
                    .describedAs(String.format("Http request element at position %d", i))
                    .isEqualTo(expectedMessages.get(i));
        }
    }

    private void assertFailedResponse(MessageRouterPublishResponse response) {
        assertThat(response.failed()).isTrue();
        assertThat(response.items()).isEmpty();
        assertThat(response.failReason()).isEqualTo(ERROR_MESSAGE);
    }

    private void assertRetryableFailedResponse(MessageRouterPublishResponse response) {
        assertThat(response.failed()).isTrue();
        assertThat(response.items()).isEmpty();
        assertThat(response.failReason()).startsWith("500 ERROR");
    }

    private void verifySingleResponse(List<? extends JsonElement> threeMessages,
                                      Flux<MessageRouterPublishResponse> responses) {
        StepVerifier.create(responses)
                .consumeNextWith(response -> verifySuccessfulResponses(threeMessages, response))
                .expectComplete()
                .verify(TIMEOUT);
    }

    private void verifyDoubleResponse(List<? extends JsonElement> threeMessages,
                                      List<? extends JsonElement> twoMessages, Flux<MessageRouterPublishResponse> responses) {
        StepVerifier.create(responses)
                .consumeNextWith(response -> verifySuccessfulResponses(threeMessages, response))
                .consumeNextWith(response -> verifySuccessfulResponses(twoMessages, response))
                .expectComplete()
                .verify(TIMEOUT);
    }

    private void verifySuccessfulResponses(List<? extends JsonElement> threeMessages, MessageRouterPublishResponse response) {
        assertThat(response.successful()).describedAs("successful").isTrue();
        JsonElement[] jsonElements = threeMessages.toJavaStream().toArray(JsonElement[]::new);
        assertThat(response.items()).containsExactly(jsonElements);
    }
}
