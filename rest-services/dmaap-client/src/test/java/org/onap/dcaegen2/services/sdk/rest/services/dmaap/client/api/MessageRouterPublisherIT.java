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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.vavr.collection.List;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.Times;
import org.mockserver.verify.VerificationTimes;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.Commons;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableDmaapConnectionPoolConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableDmaapRetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableMessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mockStatic;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.createMRSubscribeRequest;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.createPublishRequest;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.errorPublishResponse;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.getAsJsonElements;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.jsonBatch;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.plainBatch;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.registerTopic;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.successPublishResponse;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.MessageRouterTestsUtils.successSubscribeResponse;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DMaapContainer.DMAAP_SERVICE_EXPOSED_PORT;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DMaapContainer.LOCALHOST;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DMaapContainer.PROXY_MOCK_SERVICE_EXPOSED_PORT;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.DMaapContainer.createContainerInstance;

@ExtendWith(SystemStubsExtension.class)
@Testcontainers
class MessageRouterPublisherIT {
    @Container
    private static final DockerComposeContainer CONTAINER = createContainerInstance();
    private static final MockServerClient MOCK_SERVER_CLIENT = new MockServerClient(
            LOCALHOST, PROXY_MOCK_SERVICE_EXPOSED_PORT);
    private static String EVENTS_PATH;
    private static String PROXY_MOCK_EVENTS_PATH;

    private static final long REPEAT_SUBSCRIPTION = 20;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String DMAAP_400_ERROR_RESPONSE_FORMAT = "400 Bad Request\n"
            + "{"
            + "\"mrstatus\":5007,"
            + "\"helpURL\":\"http://onap.readthedocs.io\","
            + "\"message\":\"Error while publishing data to topic.:%s."
            + "Successfully published number of messages :0."
            + "Expected { to start an object.\",\"status\":400"
            + "}";
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
    private static final String CONNECTION_POLL_LIMIT_MESSAGE = "429 Too Many Requests\n"
            + "{"
            + "\"requestError\":"
            + "{"
            + "\"serviceException\":"
            + "{"
            + "\"messageId\":\"SVC2000\","
            + "\"text\":\"Pending acquire queue has reached its maximum size\","
            + "\"variables\":[\"429\"]"
            + "}"
            + "}"
            + "}";

    private MessageRouterPublisher publisher;
    private MessageRouterSubscriber subscriber;
    Mono<MessageRouterSubscribeResponse> response;
    
    @SystemStub
    EnvironmentVariables environmentVariables = new EnvironmentVariables();
    
    @BeforeAll
    static void setUp() {
        EVENTS_PATH = String.format("http://%s:%d/events", LOCALHOST, DMAAP_SERVICE_EXPOSED_PORT);
        PROXY_MOCK_EVENTS_PATH = String.format("http://%s:%d/events", LOCALHOST, PROXY_MOCK_SERVICE_EXPOSED_PORT);
        //sleep introduced to wait till all containers are started
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @AfterEach
    void afterEach() {
        publisher.close();
        subscriber.close();
    }
         
    @BeforeEach
    void set() {
        MOCK_SERVER_CLIENT.reset();
        environmentVariables
        .set("BOOTSTRAP_SERVERS", "localhost:9092")
        .set("kafka.auto.offset.reset","earliest");
        publisher = DmaapClientFactory
                .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
        subscriber = DmaapClientFactory
                .createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());
        response=null;
        
    }
    
    @Disabled
    @Test
    void test_put_givenMessageBatch_shouldMakeSuccessfulPostRequestReturningBatch() {
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
    
    @Disabled
    @Test
    void publisher_shouldHandleBadRequestError() {
        //given
        final String topic = "TOPIC2";
        final List<String> threePlainTextMessages = List.of("I", "like", "pizza");
        final Flux<JsonElement> messageBatch = plainBatch(threePlainTextMessages);
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
    void publisher_shouldSuccessfullyPublishSingleMessage() {
        //given
        final String topic = "TOPIC3";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);
        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonObject> jsonMessageBatch = jsonBatch(singleJsonMessage);
        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, "sampleGroup", "sampleId");
        final MessageRouterSubscribeResponse expectedResponse = successSubscribeResponse(expectedItems);
        
        //when
        publisher.put(publishRequest, jsonMessageBatch).repeat(REPEAT_SUBSCRIPTION)
           .subscribe(r -> { subscriber.get(subscribeRequest).subscribe(resp -> {
               if(!resp.items().isEmpty()) {
                   response = Mono.just(resp);
               }
           });
       });
 
        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    @Test
    void publisher_shouldSuccessfullyPublishMultipleMessages() {
        final String topic = "TOPIC5";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);
        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonObject> jsonMessageBatch = jsonBatch(singleJsonMessage);
        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, "sampleGroup", "sampleId");
        final MessageRouterSubscribeResponse expectedResponse = successSubscribeResponse(expectedItems);
                
        //when
        publisher.put(publishRequest, jsonMessageBatch).repeat(REPEAT_SUBSCRIPTION)
            .subscribe(r -> { subscriber.get(subscribeRequest).subscribe(resp -> {
                if(!resp.items().isEmpty()) {
                    response = Mono.just(resp);
                }
            });
        });

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    @Test
    void publisher_shouldSuccessfullyPublishSingleJsonMessageWithPlainContentType() {
        //given
        final String topic = "TOPIC6";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);

        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonElement> plainBatch = plainBatch(singleJsonMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, ContentType.TEXT_PLAIN);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, "sampleGroup", "sampleId");
        final MessageRouterSubscribeResponse expectedResponse = successSubscribeResponse(expectedItems);

        //when
        publisher.put(publishRequest, plainBatch).repeat(REPEAT_SUBSCRIPTION)
            .subscribe(r -> { subscriber.get(subscribeRequest).subscribe(resp -> {
                if(!resp.items().isEmpty()) {
                    response = Mono.just(resp);
                }
            });
        });
        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    @Test
    void publisher_shouldSuccessfullyPublishMultipleJsonMessagesWithPlainContentType() {
        //given
        final String topic = "TOPIC7";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);

        final List<String> twoJsonMessage = List.of("{\"message\":\"message1\"}", "{\"message2\":\"message2\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(twoJsonMessage);
        final Flux<JsonElement> plainBatch = plainBatch(twoJsonMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, ContentType.TEXT_PLAIN);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, "sampleGroup", "sampleId");
        final MessageRouterSubscribeResponse expectedResponse = successSubscribeResponse(expectedItems);

        //when
        publisher.put(publishRequest, plainBatch).repeat(REPEAT_SUBSCRIPTION)
            .subscribe(r -> { subscriber.get(subscribeRequest).subscribe(resp -> {
                if(!resp.items().isEmpty()) {
                    response = Mono.just(resp);
                }
            });
        });

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    @Test
    void publisher_shouldSuccessfullyPublishSinglePlainMessageWithPlainContentType() {
        //given
        final String topic = "TOPIC8";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);

        final List<String> singlePlainMessage = List.of("kebab");
        final List<JsonElement> expectedItems = getAsJsonElements(singlePlainMessage);
        final Flux<JsonElement> plainBatch = plainBatch(singlePlainMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, ContentType.TEXT_PLAIN);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, "sampleGroup", "sampleId");
        final MessageRouterSubscribeResponse expectedResponse = successSubscribeResponse(expectedItems);

        //when
        publisher.put(publishRequest, plainBatch).repeat(REPEAT_SUBSCRIPTION)
            .subscribe(r -> { subscriber.get(subscribeRequest).subscribe(resp -> {
                if(!resp.items().isEmpty()) {
                    response = Mono.just(resp);
                }
            });
        });

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    @Test
    void publisher_shouldSuccessfullyPublishMultiplePlainMessagesWithPlainContentType() {
        //given
        final String topic = "TOPIC9";
        final String topicUrl = String.format("%s/%s", EVENTS_PATH, topic);

        final List<String> singlePlainMessage = List.of("I", "like", "pizza");
        final List<JsonElement> expectedItems = getAsJsonElements(singlePlainMessage);
        final Flux<JsonElement> plainBatch = plainBatch(singlePlainMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, ContentType.TEXT_PLAIN);
        final MessageRouterSubscribeRequest subscribeRequest = createMRSubscribeRequest(topicUrl, "sampleGroup", "sampleId");
        final MessageRouterSubscribeResponse expectedResponse = successSubscribeResponse(expectedItems);

        //when
        publisher.put(publishRequest, plainBatch).repeat(REPEAT_SUBSCRIPTION)
            .subscribe(r -> { subscriber.get(subscribeRequest).subscribe(resp -> {
                if(!resp.items().isEmpty()) {
                    response = Mono.just(resp);
                }
            });
        });

        //then
        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }
    
    @Disabled
    @Test
    void publisher_shouldHandleClientTimeoutErrorWhenTimeoutDefined() {
        //given
        final String topic = "TOPIC10";
        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final Flux<JsonObject> messageBatch = jsonBatch(singleJsonMessage);
        final MessageRouterPublishRequest mrRequest = createPublishRequest(
                String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic), Duration.ofSeconds(1));
        final MessageRouterPublishResponse expectedResponse = errorPublishResponse(TIMEOUT_ERROR_MESSAGE);
        final String path = String.format("/events/%s", topic);
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withDelay(TimeUnit.SECONDS, 2));

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);

        MOCK_SERVER_CLIENT.verify(request().withPath(path), VerificationTimes.exactly(1));
    }
    
    @Disabled
    @Test
    void publisher_shouldRetryWhenRetryableHttpCodeAndSuccessfullyPublish() {
        //given
        final String topic = "TOPIC11";
        final String topicUrl = String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic);

        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonElement> plainBatch = plainBatch(singleJsonMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl);
        final MessageRouterPublishResponse expectedResponse = successPublishResponse(expectedItems);

        final String path = String.format("/events/%s", topic);
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withStatusCode(404));

        final MessageRouterPublisher publisher = DmaapClientFactory.createMessageRouterPublisher(
                retryConfig(1, 1));

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(publishRequest, plainBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();

        MOCK_SERVER_CLIENT.verify(request().withPath(path), VerificationTimes.exactly(2));
    }
    
    @Disabled
    @Test
    void publisher_shouldRetryWhenClientTimeoutAndSuccessfullyPublish() {
        //given
        final String topic = "TOPIC12";
        final String topicUrl = String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic);

        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonElement> plainBatch = plainBatch(singleJsonMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, Duration.ofSeconds(1));
        final MessageRouterPublishResponse expectedResponse = successPublishResponse(expectedItems);

        final String path = String.format("/events/%s", topic);
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withDelay(TimeUnit.SECONDS, 2));
        final MessageRouterPublisher publisher = DmaapClientFactory.createMessageRouterPublisher(
                retryConfig(1, 1));

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(publishRequest, plainBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();

        MOCK_SERVER_CLIENT.verify(request().withPath(path), VerificationTimes.exactly(2));
    }
    
    @Disabled
    @Test
    void publisher_shouldRetryManyTimesAndSuccessfullyPublish() {
        //given
        final String topic = "TOPIC13";
        final String topicUrl = String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic);

        final List<String> twoJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(twoJsonMessages);
        final Flux<JsonElement> plainBatch = plainBatch(twoJsonMessages);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, Duration.ofSeconds(1));
        final MessageRouterPublishResponse expectedResponse = successPublishResponse(expectedItems);

        final String path = String.format("/events/%s", topic);
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withDelay(TimeUnit.SECONDS, 2));
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withStatusCode(404));
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withDelay(TimeUnit.SECONDS, 2));
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withStatusCode(500));
        final MessageRouterPublisher publisher = DmaapClientFactory.createMessageRouterPublisher(retryConfig(1, 5));

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(publishRequest, plainBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();

        MOCK_SERVER_CLIENT.verify(request().withPath(path), VerificationTimes.exactly(5));
    }
    
    @Disabled
    @Test
    void publisher_shouldHandleLastRetryError500() {
        //given
        final String topic = "TOPIC14";
        final String topicUrl = String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic);

        final List<String> twoJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final Flux<JsonElement> plainBatch = plainBatch(twoJsonMessages);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, Duration.ofSeconds(1));
        final String responseMessage = "Response Message";
        final MessageRouterPublishResponse expectedResponse = errorPublishResponse(
                "500 Internal Server Error\n%s", responseMessage);

        final String path = String.format("/events/%s", topic);
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withStatusCode(404));
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withStatusCode(500).withBody(responseMessage));
        final MessageRouterPublisher publisher = DmaapClientFactory.createMessageRouterPublisher(retryConfig(1, 1));

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(publishRequest, plainBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();

        MOCK_SERVER_CLIENT.verify(request().withPath(path), VerificationTimes.exactly(2));
    }
    
    @Disabled
    @Test
    void publisher_shouldSuccessfullyPublishWhenConnectionPoolConfigurationIsSet() {
        //given
        final String topic = "TOPIC15";
        final String topicUrl = String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic);

        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonElement> plainBatch = plainBatch(singleJsonMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, Duration.ofSeconds(1));
        final MessageRouterPublishResponse expectedResponse = successPublishResponse(expectedItems);

        final String path = String.format("/events/%s", topic);
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withStatusCode(200));

        final MessageRouterPublisher publisher = DmaapClientFactory.createMessageRouterPublisher(connectionPoolConfiguration());

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(publishRequest, plainBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();

        MOCK_SERVER_CLIENT.verify(request().withPath(path).withKeepAlive(true), VerificationTimes.exactly(1));
    }
    
    @Disabled
    @Test
    void publisher_shouldRetryWhenClientTimeoutAndSuccessfullyPublishWithConnectionPoolConfiguration() {
        //given
        final String topic = "TOPIC16";
        final String topicUrl = String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic);

        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonElement> plainBatch = plainBatch(singleJsonMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, Duration.ofSeconds(1));
        final MessageRouterPublishResponse expectedResponse = successPublishResponse(expectedItems);

        final String path = String.format("/events/%s", topic);
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withDelay(TimeUnit.SECONDS, 10));

        final MessageRouterPublisher publisher = DmaapClientFactory.createMessageRouterPublisher(connectionPoolAndRetryConfiguration());

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(publishRequest, plainBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();

        MOCK_SERVER_CLIENT.verify(request().withPath(path).withKeepAlive(true), VerificationTimes.exactly(2));
    }
    
    @Disabled
    @Test
    void publisher_shouldSuccessfullyPublishSingleMessageWithBasicAuthHeader() {
        //given
        final String topic = "TOPIC17";
        final String topicUrl = String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic);

        final List<String> singleJsonMessage = List.of("{\"message\":\"message1\"}");
        final List<JsonElement> expectedItems = getAsJsonElements(singleJsonMessage);
        final Flux<JsonElement> plainBatch = plainBatch(singleJsonMessage);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, "username","password");
        final MessageRouterPublishResponse expectedResponse = successPublishResponse(expectedItems);

        final String path = String.format("/events/%s", topic);

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(publishRequest, plainBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();

        MOCK_SERVER_CLIENT.verify(request().withPath(path)
                .withHeader("Authorization" ,"Basic dXNlcm5hbWU6cGFzc3dvcmQ="), VerificationTimes.exactly(1));
    }
    
    @Disabled
    @Test
    void publisher_shouldHandleError429WhenConnectionPollLimitsHasBeenReached() {
        //given
        final String topic = "TOPIC17";
        final String topicUrl = String.format("%s/%s", PROXY_MOCK_EVENTS_PATH, topic);

        final List<String> twoJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final Flux<JsonElement> plainBatch = plainBatch(twoJsonMessages);

        final MessageRouterPublishRequest publishRequest = createPublishRequest(topicUrl, Duration.ofSeconds(1));

        final MessageRouterPublishResponse expectedResponse = errorPublishResponse(
                CONNECTION_POLL_LIMIT_MESSAGE);

        final String path = String.format("/events/%s", topic);

        //maxConnectionPoll + pendingAcquireMaxCount(default 2*maxConnectionPoll)
        final int maxNumberOfConcurrentRequest = 3;
        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.exactly(maxNumberOfConcurrentRequest))
                .respond(response().withStatusCode(429).withDelay(TimeUnit.SECONDS,1));

        MOCK_SERVER_CLIENT
                .when(request().withPath(path), Times.once())
                .respond(response().withStatusCode(200));

        final MessageRouterPublisher publisher = DmaapClientFactory.createMessageRouterPublisher(connectionPoolConfiguration());

        //when
        final Flux<MessageRouterPublishResponse> result = publisher.put(publishRequest, plainBatch);

        for(int i = 0; i < maxNumberOfConcurrentRequest; i++) {
            publisher.put(publishRequest, plainBatch).subscribe();
        }

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    private MessageRouterPublisherConfig retryConfig(int retryInterval, int retryCount) {
        return ImmutableMessageRouterPublisherConfig.builder()
                .retryConfig(ImmutableDmaapRetryConfig.builder()
                        .retryIntervalInSeconds(retryInterval)
                        .retryCount(retryCount)
                        .build())
                .build();
    }
    private MessageRouterPublisherConfig connectionPoolConfiguration() {
        return ImmutableMessageRouterPublisherConfig.builder()
                .connectionPoolConfig(ImmutableDmaapConnectionPoolConfig.builder()
                        .connectionPool(1)
                        .maxIdleTime(10)
                        .maxLifeTime(20)
                        .build())
                .build();
    }

    private MessageRouterPublisherConfig connectionPoolAndRetryConfiguration() {
        return ImmutableMessageRouterPublisherConfig.builder()
                .connectionPoolConfig(ImmutableDmaapConnectionPoolConfig.builder()
                        .connectionPool(1)
                        .maxIdleTime(10)
                        .maxLifeTime(20)
                        .build())
                .retryConfig(ImmutableDmaapRetryConfig.builder()
                        .retryIntervalInSeconds(1)
                        .retryCount(1)
                        .build())
                .build();
    }
}
