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

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.time.Duration;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@org.testcontainers.junit.jupiter.Testcontainers
class MessageRouterSubscriberTC {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int DMAAP_SERVICE_EXPOSED_PORT = 3904;
    private static final String CONSUMER_GROUP = "group1";
    private static final String SUCCESS_CONSUMER_ID = "consumer200";
    private static final String DMAAP_SERVICE_NAME = "dmaap";
    private static final String DOCKER_CONTAINER_FILE_PATH = Objects.requireNonNull(
            MessageRouterSubscriberIT.class.getClassLoader()
                    .getResource("message-router-compose.yml")).getFile();

    @Container
    private static final DockerComposeContainer CONTAINER = new DockerComposeContainer(
            new File(DOCKER_CONTAINER_FILE_PATH))
            .withExposedService(DMAAP_SERVICE_NAME, DMAAP_SERVICE_EXPOSED_PORT);

    private static MessageRouterPublishRequest mrRequest;
    private static String EVENTS_PATH;

    private MessageRouterPublisher publisher = DmaapClientFactory
            .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
    private MessageRouterSubscriber sut = DmaapClientFactory
            .createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());


    @BeforeAll
    static void setUp(){
        EVENTS_PATH = String.format("http://%s:%d/events",
                CONTAINER.getServiceHost(DMAAP_SERVICE_NAME, DMAAP_SERVICE_EXPOSED_PORT),
                CONTAINER.getServicePort(DMAAP_SERVICE_NAME, DMAAP_SERVICE_EXPOSED_PORT));

        MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
                .name("the topic")
                .topicUrl(String.format("%s/TOPIC", EVENTS_PATH))
                .build();

        mrRequest = ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .contentType("text/plain")
                .build();
    }


    @BeforeEach
    void beforeEach(){
        publisher.put(mrRequest, Flux.just("I", "like", "pizza").map(JsonPrimitive::new)).blockLast();
    }


    @Test
    void subscriber_shouldHandleNoSuchTopicException(){
        final MessageRouterSource sourceDefinition = ImmutableMessageRouterSource.builder()
                .name("the topic")
                .topicUrl(String.format("%s/newTopic", EVENTS_PATH))
                .build();

        ImmutableMessageRouterSubscribeRequest mrSuccessRequest = ImmutableMessageRouterSubscribeRequest
                .builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(CONSUMER_GROUP)
                .consumerId(SUCCESS_CONSUMER_ID)
                .build();

        Mono<MessageRouterSubscribeResponse> response = sut
                .get(mrSuccessRequest);


        MessageRouterSubscribeResponse expectedResponse = ImmutableMessageRouterSubscribeResponse
                .builder()
                .failReason("404 Not Found\n{\"mrstatus\":3001,\"helpURL\":\"http://onap.readthedocs.io\",\"message\":\"No such topic exists.-[newTopic]\",\"status\":404}")
                .build();

        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldGetCorrectResponse(){

        ImmutableMessageRouterSource sourceDefinition = ImmutableMessageRouterSource.builder()
                .name("the topic")
                .topicUrl(String.format("%s/TOPIC", EVENTS_PATH))
                .build();

        ImmutableMessageRouterSubscribeRequest mrSuccessRequest = ImmutableMessageRouterSubscribeRequest
                .builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(CONSUMER_GROUP)
                .consumerId(SUCCESS_CONSUMER_ID)
                .build();

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
                .verify();
    }
}
