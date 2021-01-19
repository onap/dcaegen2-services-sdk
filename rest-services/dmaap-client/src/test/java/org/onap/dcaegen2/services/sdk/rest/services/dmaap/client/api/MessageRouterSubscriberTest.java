/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.DmaapResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableDmaapTimeoutConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendError;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendResource;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendWithDelay;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */
class MessageRouterSubscriberTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String ERROR_MESSAGE = "Something went wrong";
    private static final String TIMEOUT_ERROR_MESSAGE = "408 Request Timeout";
    private static final String CONSUMER_GROUP = "group1";
    private static final String SUCCESS_CONSUMER_ID = "consumer200";
    private static final String DELAY_CONSUMER_ID = "delay200";
    private static final String FAILING_WITH_401_CONSUMER_ID = "consumer401";
    private static final String FAILING_WITH_403_CONSUMER_ID = "consumer403";
    private static final String FAILING_WITH_409_CONSUMER_ID = "consumer409";
    private static final String FAILING_WITH_429_CONSUMER_ID = "consumer429";
    private static final String FAILING_WITH_500_CONSUMER_ID = "consumer500";

    private static final String CONSUMER_PATH = String.format("/events/TOPIC/%s", CONSUMER_GROUP);

    private static final String SUCCESS_RESP_PATH = String
            .format("%s/%s", CONSUMER_PATH, SUCCESS_CONSUMER_ID);
    private static final String DELAY_RESP_PATH = String
            .format("%s/%s", CONSUMER_PATH, DELAY_CONSUMER_ID);
    private static final String FAILING_WITH_401_RESP_PATH = String
            .format("%s/%s", CONSUMER_PATH, FAILING_WITH_401_CONSUMER_ID);
    private static final String FAILING_WITH_403_RESP_PATH = String
            .format("%s/%s", CONSUMER_PATH, FAILING_WITH_403_CONSUMER_ID);
    private static final String FAILING_WITH_409_RESP_PATH = String
            .format("%s/%s", CONSUMER_PATH, FAILING_WITH_409_CONSUMER_ID);
    private static final String FAILING_WITH_429_RESP_PATH = String
            .format("%s/%s", CONSUMER_PATH, FAILING_WITH_429_CONSUMER_ID);
    private static final String FAILING_WITH_500_RESP_PATH = String
            .format("%s/%s", CONSUMER_PATH, FAILING_WITH_500_CONSUMER_ID);

    private static MessageRouterSubscribeRequest mrSuccessRequest;
    private static MessageRouterSubscribeRequest mrFailingRequest;
    private MessageRouterSubscriber sut = DmaapClientFactory
            .createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());
    private static MessageRouterSource sourceDefinition;


    @BeforeAll
    static void setUp() {
        DummyHttpServer server = DummyHttpServer.start(routes -> routes
                .get(SUCCESS_RESP_PATH, (req, resp) ->
                        sendResource(resp, "/sample-mr-subscribe-response.json"))
                .get(DELAY_RESP_PATH, (req, resp) -> sendWithDelay(resp, 200, TIMEOUT))
                .get(FAILING_WITH_401_RESP_PATH, (req, resp) -> sendError(resp, 401, ERROR_MESSAGE))
                .get(FAILING_WITH_403_RESP_PATH, (req, resp) -> sendError(resp, 403, ERROR_MESSAGE))
                .get(FAILING_WITH_409_RESP_PATH, (req, resp) -> sendError(resp, 409, ERROR_MESSAGE))
                .get(FAILING_WITH_429_RESP_PATH, (req, resp) -> sendError(resp, 429, ERROR_MESSAGE))
                .get(FAILING_WITH_500_RESP_PATH, (req, resp) -> sendError(resp, 500, ERROR_MESSAGE)));

        sourceDefinition = createMessageRouterSource(server);

        mrSuccessRequest = createSuccessRequest();

        mrFailingRequest = createFailingRequest(FAILING_WITH_500_CONSUMER_ID);
    }

    @Test
    void subscriber_shouldGetCorrectResponse() {
        Mono<MessageRouterSubscribeResponse> response = sut
                .get(mrSuccessRequest);

        List<String> expectedItems = List.of("I", "like", "pizza");

        MessageRouterSubscribeResponse expectedResponse = ImmutableMessageRouterSubscribeResponse
                .builder()
                .items(expectedItems.map(JsonPrimitive::new))
                .build();

        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @ParameterizedTest
    @CsvSource({
            FAILING_WITH_401_CONSUMER_ID + "," + "401 Unauthorized",
            FAILING_WITH_403_CONSUMER_ID + "," + "403 Forbidden",
            FAILING_WITH_409_CONSUMER_ID + "," + "409 Conflict",
            FAILING_WITH_429_CONSUMER_ID + "," + "429 Too Many Requests",
            FAILING_WITH_500_CONSUMER_ID + "," + "500 Internal Server Error"
    })
    void subscriber_shouldHandleError(String consumerId, String failReason) {
        MessageRouterSubscribeRequest request = createFailingRequest(consumerId);
        Mono<MessageRouterSubscribeResponse> response = sut.get(request);

        MessageRouterSubscribeResponse expectedResponse = createErrorResponse(failReason);

        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldParseCorrectResponse() {
        final Flux<String> result = sut
                .getElements(mrSuccessRequest)
                .map(JsonElement::getAsString);

        StepVerifier.create(result)
                .expectNext("I", "like", "pizza")
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldParseErrorResponse() {
        Flux<String> result = sut
                .getElements(mrFailingRequest)
                .map(JsonElement::getAsString);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldSubscribeCorrectly() {
        Flux<String> subscriptionForElements = sut
                .subscribeForElements(mrSuccessRequest, Duration.ofSeconds(1))
                .map(JsonElement::getAsString);

        StepVerifier.create(subscriptionForElements.take(2))
                .expectNext("I", "like")
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldParseErrorWhenSubscribed() {
        Flux<String> subscriptionForElements = sut
                .subscribeForElements(mrFailingRequest, Duration.ofSeconds(1))
                .map(JsonElement::getAsString);

        StepVerifier.create(subscriptionForElements.take(2))
                .expectError(IllegalStateException.class)
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldHandleClientTimeoutError() {
        Duration requestTimeout = Duration.ofMillis(1);
        MessageRouterSubscribeRequest request = createDelayRequest(DELAY_CONSUMER_ID, requestTimeout);
        Mono<MessageRouterSubscribeResponse> response = sut.get(request);

        StepVerifier.create(response)
                .consumeNextWith(this::assertTimeoutError)
                .expectComplete()
                .verify(TIMEOUT);
    }

    private static MessageRouterSource createMessageRouterSource(DummyHttpServer server) {
        return ImmutableMessageRouterSource.builder()
                .name("the topic")
                .topicUrl(String.format("http://%s:%d/events/TOPIC", server.host(), server.port()))
                .build();
    }

    private static MessageRouterSubscribeRequest createSuccessRequest() {
        return ImmutableMessageRouterSubscribeRequest.builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(CONSUMER_GROUP)
                .consumerId(SUCCESS_CONSUMER_ID)
                .build();
    }

    private static MessageRouterSubscribeRequest createDelayRequest(String consumerId, Duration timeout) {
        return ImmutableMessageRouterSubscribeRequest.builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(CONSUMER_GROUP)
                .consumerId(consumerId)
                .timeoutConfig(ImmutableDmaapTimeoutConfig.builder().timeout(timeout).build())
                .build();
    }

    private static MessageRouterSubscribeRequest createFailingRequest(String consumerId) {
        return ImmutableMessageRouterSubscribeRequest
                .builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(CONSUMER_GROUP)
                .consumerId(consumerId)
                .build();
    }

    private MessageRouterSubscribeResponse createErrorResponse(String failReason) {
        String failReasonFormat = failReason + "\n%s";
        return ImmutableMessageRouterSubscribeResponse
                .builder()
                .failReason(String.format(failReasonFormat, ERROR_MESSAGE))
                .build();
    }

    private void assertTimeoutError(DmaapResponse response) {
        assertThat(response.failed()).isTrue();
        assertThat(response.failReason()).startsWith(TIMEOUT_ERROR_MESSAGE);
    }
}

