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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.List;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
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
        final Flux<JsonObject> messageBatch = MessageRouterTestsUtils.jsonBatch(twoJsonMessages);
        final MessageRouterPublishRequest mrRequest = MessageRouterTestsUtils
                .createPublishRequest(String.format("%s/%s", EVENTS_PATH, topic));
        final ImmutableMessageRouterPublishResponse expectedResponse = ImmutableMessageRouterPublishResponse
                .builder()
                .items(MessageRouterTestsUtils.getAsJsonElements(twoJsonMessages))
                .build();

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
        final Flux<JsonPrimitive> messageBatch = MessageRouterTestsUtils
                .plainBatch(threePlainTextMessages);
        final MessageRouterPublishRequest mrRequest = MessageRouterTestsUtils
                .createPublishRequest(String.format("%s/%s", EVENTS_PATH, topic));
        final MessageRouterPublishResponse expectedResponse = createErrorResponse(
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
        final List<JsonElement> expectedItems = MessageRouterTestsUtils
                .getAsJsonElements(singleJsonMessage);
        final Flux<JsonObject> jsonMessageBatch = MessageRouterTestsUtils.jsonBatch(singleJsonMessage);
        final MessageRouterPublishRequest publishRequest = MessageRouterTestsUtils
                .createPublishRequest(topicUrl);
        final MessageRouterSubscribeRequest subscribeRequest = MessageRouterTestsUtils
                .createMRSubscribeRequest(topicUrl, "sampleGroup", "sampleId");
        final ImmutableMessageRouterSubscribeResponse expectedResponse = ImmutableMessageRouterSubscribeResponse
                .builder()
                .items(expectedItems)
                .build();

        //when
        MessageRouterTestsUtils.registerTopic(publisher, publishRequest, subscriber, subscribeRequest);
        Mono<MessageRouterSubscribeResponse> response = publisher
                .put(publishRequest, jsonMessageBatch)
                .then(subscriber.get(subscribeRequest));

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    private MessageRouterPublishResponse createErrorResponse(String failReasonFormat, Object... formatArgs){
        return ImmutableMessageRouterPublishResponse
                .builder()
                .failReason(String.format(failReasonFormat, formatArgs))
                .build();
    }
}
