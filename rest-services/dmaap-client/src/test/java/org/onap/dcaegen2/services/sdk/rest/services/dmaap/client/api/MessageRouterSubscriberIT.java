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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api;

import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendError;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendResource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */
class MessageRouterSubscriberIT {
    private static final Duration TEN_SECONDS = Duration.ofSeconds(10);
    private static final String CONSUMER_GROUP = "group1";
    private static final String SUCCESS_CONSUMER_ID = "consumer8";
    private static final String FAILING_CONSUMER_ID = "consumer12";
    private static final String SUCCESS_RESP_PATH = String
            .format("/events/TOPIC/%s/%s", CONSUMER_GROUP, SUCCESS_CONSUMER_ID);
    private static final String FAILING_RESP_PATH = String
            .format("/events/TOPIC/%s/%s", CONSUMER_GROUP, FAILING_CONSUMER_ID);

    private static MessageRouterSubscribeRequest mrSuccessRequest;
    private static MessageRouterSubscribeRequest mrFailingRequest;
    private MessageRouterSubscriber sut = DmaapClientFactory
            .createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());


    @BeforeAll
    static void setUp() {
        DummyHttpServer server = DummyHttpServer.start(routes ->
                routes
                        .get(SUCCESS_RESP_PATH, (req, resp) ->
                                sendResource(resp, "/sample-mr-subscribe-response.json"))
                        .get(FAILING_RESP_PATH, (req, resp) ->
                                sendError(resp, 500, "Something went wrong"))
        );

        MessageRouterSource sourceDefinition = ImmutableMessageRouterSource.builder()
                .name("the topic")
                .topicUrl(String.format("http://%s:%d/events/TOPIC", server.host(), server.port()))
                .build();

        mrSuccessRequest = ImmutableMessageRouterSubscribeRequest.builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(CONSUMER_GROUP)
                .consumerId(SUCCESS_CONSUMER_ID)
                .build();

        mrFailingRequest = ImmutableMessageRouterSubscribeRequest
                .builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(CONSUMER_GROUP)
                .consumerId(FAILING_CONSUMER_ID)
                .build();
    }

    @Test
    void subscriber_shouldGetCorrectResponse(){
        Mono<MessageRouterSubscribeResponse> response = sut
                .get(mrSuccessRequest);

        JsonArray expectedItems = new JsonArray();
        expectedItems.add("I");
        expectedItems.add("like");
        expectedItems.add("pizza");

        ImmutableMessageRouterSubscribeResponse expectedResponse = ImmutableMessageRouterSubscribeResponse
                .builder()
                .items(expectedItems)
                .build();

        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TEN_SECONDS);
    }

    @Test
    void subscriber_shouldGetErrorResponse(){
        Mono<MessageRouterSubscribeResponse> response = sut
                .get(mrFailingRequest);

        ImmutableMessageRouterSubscribeResponse expectedResponse = ImmutableMessageRouterSubscribeResponse
                .builder()
                .failReason("500 Internal Server Error\n" + "Something went wrong")
                .build();

        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TEN_SECONDS);
    }

    @Test
    void subscriber_shouldParseCorrectResponse() {
        final Flux<String> result = sut
                .getElements(mrSuccessRequest)
                .map(JsonElement::getAsString);

        StepVerifier.create(result)
                .expectNext("I", "like", "pizza")
                .expectComplete()
                .verify(TEN_SECONDS);
    }

    @Test
    void subscriber_shouldParseErrorResponse(){
        Flux<String> result = sut
                .getElements(mrFailingRequest)
                .map(JsonElement::getAsString);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify(TEN_SECONDS);
    }

    @Test
    void subscriber_shouldSubscribeCorrectly(){
        Flux<String> subscriptionForElements = sut
                .subscribeForElements(mrSuccessRequest, Duration.ofSeconds(1))
                .map(JsonElement:: getAsString);

        StepVerifier.create(subscriptionForElements.take(2))
                .expectNext("I", "like")
                .expectComplete()
                .verify(TEN_SECONDS);
    }

    @Test
    void subscriber_shouldParseErrorWhenSubscribed(){
        Flux<String> subscriptionForElements = sut
                .subscribeForElements(mrFailingRequest, Duration.ofSeconds(1))
                .map(JsonElement:: getAsString);

        StepVerifier.create(subscriptionForElements.take(2))
                .expectError(IllegalStateException.class)
                .verify(TEN_SECONDS);
    }
}
