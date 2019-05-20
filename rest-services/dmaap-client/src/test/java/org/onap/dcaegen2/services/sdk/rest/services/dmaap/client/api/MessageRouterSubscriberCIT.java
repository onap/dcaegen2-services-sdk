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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
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

@Disabled("Disabled until fix messages formatting in MessageRouterPublisher::put ")
@Testcontainers
class MessageRouterSubscriberCIT {
    private static final Gson gson = new Gson();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int DMAAP_SERVICE_EXPOSED_PORT = 3904;
    private static final List<String> messageBatchItems = Arrays.asList("I", "like", "pizza");
    private static final Flux<JsonPrimitive> messageBatch = Flux.fromIterable(messageBatchItems)
            .map(JsonPrimitive::new);
    private static final String CONSUMER_GROUP = "group1";
    private static final String CONSUMER_ID = "consumer200";
    private static final String DMAAP_SERVICE_NAME = "dmaap";
    private static final String DOCKER_CONTAINER_FILE_PATH = Objects.requireNonNull(
            MessageRouterSubscriberCIT.class.getClassLoader()
                    .getResource("dmaap-msg-router/message-router-compose.yml")).getFile();
    private static final String DMAAP_404_ERROR_RESPONSE_FORMAT = "404 Not Found\n" +
            "{" +
            "\"mrstatus\":3001," +
            "\"helpURL\":\"http://onap.readthedocs.io\"," +
            "\"message\":\"No such topic exists.-[%s]\"," +
            "\"status\":404" +
            "}";

    @Container
    private static final DockerComposeContainer CONTAINER = new DockerComposeContainer(
            new File(DOCKER_CONTAINER_FILE_PATH))
            .withExposedService(DMAAP_SERVICE_NAME, DMAAP_SERVICE_EXPOSED_PORT);

    private static String EVENTS_PATH;

    private MessageRouterPublisher publisher = DmaapClientFactory
            .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
    private MessageRouterSubscriber sut = DmaapClientFactory
            .createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());


    @BeforeAll
    static void setUp() {
        EVENTS_PATH = String.format("http://%s:%d/events",
                CONTAINER.getServiceHost(DMAAP_SERVICE_NAME, DMAAP_SERVICE_EXPOSED_PORT),
                CONTAINER.getServicePort(DMAAP_SERVICE_NAME, DMAAP_SERVICE_EXPOSED_PORT));
    }

    @Test
    void subscriber_shouldHandleNoSuchTopicException() {
        //given
        final String topic = "newTopic";
        final MessageRouterSubscribeRequest mrSubscribeRequest = createMRSubscribeRequest(topic);
        final String expectedFailReason = String.format(DMAAP_404_ERROR_RESPONSE_FORMAT, topic);
        final MessageRouterSubscribeResponse expectedResponse = ImmutableMessageRouterSubscribeResponse
                .builder()
                .failReason(expectedFailReason)
                .build();

        //when
        Mono<MessageRouterSubscribeResponse> response = sut
                .get(mrSubscribeRequest);

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldGetCorrectResponse() {
        //given
        final String topic = "TOPIC";
        final MessageRouterPublishRequest publishRequest = createMRPublishRequest(topic, "text/plain");
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topic);
        final JsonArray expectedItems = getAsJsonArray(messageBatchItems);
        final ImmutableMessageRouterSubscribeResponse expectedResponse = ImmutableMessageRouterSubscribeResponse
                .builder()
                .items(expectedItems)
                .build();

        //when
        registerTopic(publishRequest, subscribeRequest);
        Mono<MessageRouterSubscribeResponse> response = publisher
                .put(publishRequest, messageBatch)
                .then(sut.get(subscribeRequest));

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    private static MessageRouterPublishRequest createMRPublishRequest(String topic,
            String contentType) {
        MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
                .name("the topic")
                .topicUrl(String.format("%s/%s", EVENTS_PATH, topic))
                .build();

        return ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .contentType(contentType)
                .build();
    }

    private MessageRouterSubscribeRequest createMRSubscribeRequest(String topic) {
        ImmutableMessageRouterSource sourceDefinition = ImmutableMessageRouterSource.builder()
                .name("the topic")
                .topicUrl(String.format("%s/%s", EVENTS_PATH, topic))
                .build();

        return ImmutableMessageRouterSubscribeRequest
                .builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(CONSUMER_GROUP)
                .consumerId(CONSUMER_ID)
                .build();
    }

    private void registerTopic(MessageRouterPublishRequest publishRequest,
            MessageRouterSubscribeRequest subscribeRequest) {
        Flux<JsonPrimitive> sampleMessage = Flux.just("sample message").map(JsonPrimitive::new);

        publisher.put(publishRequest, sampleMessage).blockLast();
        sut.get(subscribeRequest).block();
    }

    private JsonArray getAsJsonArray(List<String> list) {
        String listsJsonString = gson.toJson(list);
        return new JsonParser().parse(listsJsonString).getAsJsonArray();
    }
}
