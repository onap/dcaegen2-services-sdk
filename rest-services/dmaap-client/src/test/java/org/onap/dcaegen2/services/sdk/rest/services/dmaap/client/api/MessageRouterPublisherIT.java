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

import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterTestsUtils.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.List;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
class MessageRouterPublisherIT {
    @Container
    private static final DockerComposeContainer CONTAINER = DMaapContainer.createContainerInstance();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String DMAAP_400_ERROR_RESPONSE_FORMAT = "400 Bad Request\n"
            + "{"
            + "\"mrstatus\":5007,"
            + "\"helpURL\":\"http://onap.readthedocs.io\","
            + "\"message\":\"Error while publishing data to topic.:%s."
            + "Successfully published number of messages :0."
            + "Expected { to start an object.\",\"status\":400"
            + "}";
    private static String EVENTS_PATH;
    private final MessageRouterPublisher publisher = DmaapClientFactory
            .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
    private MessageRouterSubscriber subscriber = DmaapClientFactory
            .createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());

    @BeforeAll
    static void setUp() {
        EVENTS_PATH = String.format("http://%s:%d/events",
                CONTAINER.getServiceHost(DMaapContainer.DMAAP_SERVICE_NAME,
                        DMaapContainer.DMAAP_SERVICE_EXPOSED_PORT),
                CONTAINER.getServicePort(DMaapContainer.DMAAP_SERVICE_NAME,
                        DMaapContainer.DMAAP_SERVICE_EXPOSED_PORT));
    }

    @Test
    void test_put_givenMessageBatch_shouldMakeSuccessfulPostRequestReturningBatch(){
        //given
        final String topic = "TOPIC";
        final List<String> twoJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final Flux<JsonObject> messageBatch = jsonBatch(twoJsonMessages);
        final MessageRouterPublishRequest mrRequest = createPublishRequest(String.format("%s/%s", EVENTS_PATH, topic));
        final MessageRouterPublishResponse expectedResponse = successPublishResponse(getAsJsonElements(twoJsonMessages));

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void publisher_shouldHandleBadRequestError(){
        //given
        final String topic = "TOPIC2";
        final List<String> threePlainTextMessages = List.of("I", "like", "pizza");
        final Flux<JsonPrimitive> messageBatch = plainBatch(threePlainTextMessages);
        final MessageRouterPublishRequest mrRequest = createPublishRequest(String.format("%s/%s", EVENTS_PATH, topic));
        final MessageRouterPublishResponse expectedResponse = errorPublishResponse(
                DMAAP_400_ERROR_RESPONSE_FORMAT, topic);

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void publisher_shouldSuccessfullyPublishMessages(){
        final String topic = "TOPIC3";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);
        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonObject> jsonMessageBatch = jsonBatch(singleJsonMessage);
        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, "sampleGroup", "sampleId");
        final MessageRouterSubscribeResponse expectedResponse = successSubscribeResponse(expectedItems);

        //when
        registerTopic(publisher, publishRequest, subscriber, subscribeRequest);
        Mono<MessageRouterSubscribeResponse> response = publisher
                .put(publishRequest, jsonMessageBatch)
                .then(subscriber.get(subscribeRequest));

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }
}
