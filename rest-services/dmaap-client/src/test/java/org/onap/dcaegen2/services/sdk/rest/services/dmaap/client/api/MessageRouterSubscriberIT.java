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
import com.google.gson.JsonObject;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import io.vavr.collection.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static eu.rekawek.toxiproxy.model.ToxicDirection.DOWNSTREAM;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.createMRSubscribeRequest;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.createPublishRequest;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.errorSubscribeResponse;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.getAsJsonElements;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.getAsJsonObject;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.jsonBatch;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.registerTopic;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.successSubscribeResponse;

import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DMaapContainer.DMAAP_SERVICE_EXPOSED_PORT;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DMaapContainer.DMAAP_SERVICE_NAME;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DMaapContainer.LOCALHOST;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DMaapContainer.PROXY_SERVICE_EXPOSED_PORT;

@Testcontainers
class MessageRouterSubscriberIT {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String CONSUMER_GROUP = "group1";
    private static final String CONSUMER_ID = "consumer200";
    private static String PROXY_EVENTS_PATH;
    private static Proxy DMAAP_PROXY;
    private static final String DMAAP_404_ERROR_RESPONSE_FORMAT = "404 Not Found\n" +
            "{" +
            "\"mrstatus\":3001," +
            "\"helpURL\":\"http://onap.readthedocs.io\"," +
            "\"message\":\"No such topic exists.-[%s]\"," +
            "\"status\":404" +
            "}";
    private static final String TIMEOUT_ERROR_MESSAGE = "408 Request Timeout\n"
            + "{"
            + "\"requestError\":"
            + "{"
            + "\"serviceException\":"
            + "{"
            + "\"messageId\":\"SVC0001\","
            + "\"text\":\"Client timeout exception occurred, Error code is %1\","
            + "\"variables\":[\"408\"]"
            + "}"
            + "}"
            + "}";

    @Container
    private static final DockerComposeContainer CONTAINER = DMaapContainer.createContainerInstance();

    private static String EVENTS_PATH;

    private MessageRouterPublisher publisher = DmaapClientFactory
            .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
    private MessageRouterSubscriber subscriber = DmaapClientFactory
            .createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());


    @BeforeAll
    static void setUp() throws IOException {
        EVENTS_PATH = String.format("http://%s:%d/events", LOCALHOST, DMAAP_SERVICE_EXPOSED_PORT);
        PROXY_EVENTS_PATH = String.format("http://%s:%d/events", LOCALHOST, PROXY_SERVICE_EXPOSED_PORT);

        DMAAP_PROXY = new ToxiproxyClient().createProxy("dmaapProxy",
                String.format("[::]:%s", PROXY_SERVICE_EXPOSED_PORT),
                String.format("%s:%d", DMAAP_SERVICE_NAME, DMAAP_SERVICE_EXPOSED_PORT));
    }


    @Test
    void subscriber_shouldHandleNoSuchTopicException() {
        //given
        final String topic = "newTopic";
        final MessageRouterSubscribeRequest mrSubscribeRequest = createMRSubscribeRequest(
                String.format("%s/%s", EVENTS_PATH, topic), CONSUMER_GROUP, CONSUMER_ID);
        final MessageRouterSubscribeResponse expectedResponse = errorSubscribeResponse(
                DMAAP_404_ERROR_RESPONSE_FORMAT, topic);

        //when
        Mono<MessageRouterSubscribeResponse> response = subscriber
                .get(mrSubscribeRequest);

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void subscriberShouldHandleSingleItemResponse(){
        //given
        final String topic = "TOPIC";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);
        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, CONSUMER_GROUP, CONSUMER_ID);

        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonObject> jsonMessageBatch = jsonBatch(singleJsonMessage);
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

    @Test
    void subscriber_shouldHandleMultipleItemsResponse() {
        //given
        final String topic = "TOPIC2";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);
        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, CONSUMER_GROUP, CONSUMER_ID);

        final List<String> twoJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final List<JsonElement> expectedElements = getAsJsonElements(twoJsonMessages);
        final Flux<JsonObject> jsonMessageBatch = jsonBatch(twoJsonMessages);
        final MessageRouterSubscribeResponse expectedResponse = successSubscribeResponse(expectedElements);

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

    @Test
    void subscriber_shouldExtractItemsFromResponse() {
        //given
        final String topic = "TOPIC3";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);
        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl,
                CONSUMER_GROUP, CONSUMER_ID);

        final List<String> twoJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final Flux<JsonObject> jsonMessageBatch = jsonBatch(twoJsonMessages);

        //when
        registerTopic(publisher, publishRequest, subscriber, subscribeRequest);
        final Flux<JsonElement> result = publisher.put(publishRequest, jsonMessageBatch)
                .thenMany(subscriber.getElements(subscribeRequest));

        //then
        StepVerifier.create(result)
                .expectNext(getAsJsonObject(twoJsonMessages.get(0)))
                .expectNext(getAsJsonObject(twoJsonMessages.get(1)))
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldSubscribeToTopic(){
        //given
        final String topic = "TOPIC4";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);
        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl,
                CONSUMER_GROUP, CONSUMER_ID);

        final List<String> twoJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final List<JsonElement> messages = getAsJsonElements(twoJsonMessages);
        final Flux<JsonObject> jsonMessageBatch = jsonBatch(twoJsonMessages);

        //when
        registerTopic(publisher, publishRequest, subscriber, subscribeRequest);
        final Flux<JsonElement> result = publisher.put(publishRequest, jsonMessageBatch)
                .thenMany(subscriber.subscribeForElements(subscribeRequest, Duration.ofSeconds(1)));

        //then
        StepVerifier.create(result.take(2))
                .expectNext(messages.get(0))
                .expectNext(messages.get(1))
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void subscriber_shouldHandleTimeoutException() throws IOException {
        //given
        final String topic = "newTopic";
        final MessageRouterSubscribeRequest mrSubscribeRequest = createMRSubscribeRequest(
                String.format("%s/%s", PROXY_EVENTS_PATH, topic), CONSUMER_GROUP, CONSUMER_ID, Duration.ofSeconds(1));
        final MessageRouterSubscribeResponse expectedResponse = errorSubscribeResponse(
                TIMEOUT_ERROR_MESSAGE);

        final String toxic = "latency-toxic";
        DMAAP_PROXY.toxics()
                .latency(toxic, DOWNSTREAM, TimeUnit.SECONDS.toMillis(5));

        //when
        Mono<MessageRouterSubscribeResponse> response = subscriber
                .get(mrSubscribeRequest);

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);

        //cleanup
        DMAAP_PROXY.toxics().get(toxic).remove();
    }
}
