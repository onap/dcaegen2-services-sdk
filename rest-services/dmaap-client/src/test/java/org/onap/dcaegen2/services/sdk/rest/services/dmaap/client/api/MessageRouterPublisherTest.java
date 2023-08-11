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
import com.google.gson.JsonPrimitive;
import io.vavr.collection.List;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.MessageRouterPublisherImpl;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.DmaapResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.ImmutableDmaapTimeoutConfig;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.time.Duration;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendError;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendString;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendWithDelay;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */
@ExtendWith(SystemStubsExtension.class)
class MessageRouterPublisherTest {

    private static final String ERROR_MESSAGE = "Something went wrong";
    private static final String TIMEOUT_ERROR_MESSAGE = "408 Request Timeout";
    private static final String CONNECTION_ERROR_MESSAGE = "503 Service unavailable";
    private static final String SUCCESS_RESP_TOPIC_PATH = "/events/TOPIC";
    private static final String DELAY_RESP_TOPIC_PATH = "/events/DELAY";
    private static final String FAILING_WITH_400_RESP_PATH = "/events/TOPIC400";
    private static final String FAILING_WITH_401_RESP_PATH = "/events/TOPIC401";
    private static final String FAILING_WITH_403_RESP_PATH = "/events/TOPIC403";
    private static final String FAILING_WITH_404_RESP_PATH = "/events/TOPIC404";
    private static final String FAILING_WITH_500_RESP_PATH = "/events/TOPIC500";
    private static final String FAILING_WITH_429_RESP_PATH = "/events/TOPIC429";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Flux<JsonPrimitive> messageBatch = Flux.just("ala", "ma", "kota")
            .map(JsonPrimitive::new);
    private static final List<String> messageBatchItems = List.of("ala", "ma", "kota");
    private static final DummyHttpServer DISPOSED_HTTP_SERVER = initialize().closeAndGet();
    private static final DummyHttpServer SERVER = initialize();
    private MessageRouterPublisher sut;
    MockProducer<String, String> mockProducer = 
            new MockProducer<>(true, new StringSerializer(), new StringSerializer());
    private static DummyHttpServer initialize() {
        return DummyHttpServer.start(routes -> routes
                .post(SUCCESS_RESP_TOPIC_PATH, (req, resp) -> sendString(resp, Mono.just("OK")))
                .post(DELAY_RESP_TOPIC_PATH, (req, resp) -> sendWithDelay(resp, 200, TIMEOUT))
                .post(FAILING_WITH_400_RESP_PATH, (req, resp) -> sendError(resp, 400, ERROR_MESSAGE))
                .post(FAILING_WITH_401_RESP_PATH, (req, resp) -> sendError(resp, 401, ERROR_MESSAGE))
                .post(FAILING_WITH_403_RESP_PATH, (req, resp) -> sendError(resp, 403, ERROR_MESSAGE))
                .post(FAILING_WITH_404_RESP_PATH, (req, resp) -> sendError(resp, 404, ERROR_MESSAGE))
                .post(FAILING_WITH_500_RESP_PATH, (req, resp) -> sendError(resp, 500, ERROR_MESSAGE))
                .post(FAILING_WITH_429_RESP_PATH, (req, resp) -> sendError(resp, 429, ERROR_MESSAGE))
        );
    }
    
    @SystemStub
    EnvironmentVariables environmentVariables = new EnvironmentVariables();
    
    @BeforeEach
    void setUp() {
    	environmentVariables
		.set("BOOTSTRAP_SERVERS", "localhost:9092")
		.set("JAAS_CONFIG", "jaas.config");
    	
    	sut = DmaapClientFactory
                .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
    	sut.setKafkaProducer(mockProducer);
    }
    @AfterEach
    void afterEach() {
    	sut.close();
    }
    @Test
    void test_put_givenMessageBatch_shouldMakeSuccessfulPostRequestReturningBatch() {
    	//given
        final MessageRouterPublishRequest mrRequest = createTextPlainMRRequest(SUCCESS_RESP_TOPIC_PATH, SERVER);
        final List<JsonElement> expectedItems = messageBatchItems.map(JsonPrimitive::new);

        //when
        final Flux<MessageRouterPublishResponse> result = sut.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(ImmutableMessageRouterPublishResponse.builder().items(expectedItems).build())
                .expectComplete()
                .verify(TIMEOUT);
    }
    
    @Test
    void publisher_shouldHandleError() {
    	 
    	 sut.setKafkaProducer(mockProducer);
    	 
    	 final MessageRouterPublishRequest mrRequest = createTextPlainMRRequest(SUCCESS_RESP_TOPIC_PATH, SERVER);
    	 
         //when
         final Flux<MessageRouterPublishResponse> result = sut.put(mrRequest, messageBatch);
    	 RuntimeException e = new RuntimeException();
    	 mockProducer.errorNext(e);
    	 Future<RecordMetadata> record =MessageRouterPublisherImpl.getFuture();
    	 try{
    		 record.get();
    	 }catch(Exception ex) {
    		assertEquals(e, ex);
    	 }
    	 assertTrue(record.isDone());
    	 
    }
    
    @Disabled
    @ParameterizedTest
    @CsvSource({
            FAILING_WITH_400_RESP_PATH + "," + "400 Bad Request",
            FAILING_WITH_401_RESP_PATH + "," + "401 Unauthorized",
            FAILING_WITH_403_RESP_PATH + "," + "403 Forbidden",
            FAILING_WITH_404_RESP_PATH + "," + "404 Not Found",
            FAILING_WITH_500_RESP_PATH + "," + "500 Internal Server Error",
            FAILING_WITH_429_RESP_PATH + "," + "429 Too Many Requests"
    })
    void publisher_shouldHandleError(String failingPath, String failReason) {
        //given
        final MessageRouterPublishRequest mrRequest = createTextPlainMRRequest(failingPath, SERVER);
        final MessageRouterPublishResponse expectedResponse = createErrorResponse(failReason);

        //when
        final Flux<MessageRouterPublishResponse> result = sut.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }
    
    @Disabled
    @Test
    void publisher_shouldHandleClientTimeoutError() {
        //given
        final Duration requestTimeout = Duration.ofMillis(1);
        final MessageRouterPublishRequest mrRequest = createTextPlainMRRequest(DELAY_RESP_TOPIC_PATH, requestTimeout);

        //when
        final Flux<MessageRouterPublishResponse> result = sut.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .consumeNextWith(this::assertTimeoutError)
                .expectComplete()
                .verify(TIMEOUT);
    }
    
    @Disabled
    @Test
    void publisher_shouldHandleConnectionError() {
        //given
        final MessageRouterPublishRequest mrRequest = createTextPlainMRRequest(
                SUCCESS_RESP_TOPIC_PATH, DISPOSED_HTTP_SERVER);

        //when
        final Flux<MessageRouterPublishResponse> result = sut.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .consumeNextWith(this::assertConnectionError)
                .expectComplete()
                .verify(TIMEOUT);
    }

    private static MessageRouterPublishRequest createTextPlainMRRequest(String topicPath, DummyHttpServer dummyHttpServer) {
        final MessageRouterSink sinkDefinition = createMRSink(topicPath, dummyHttpServer);
        return ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .contentType(ContentType.TEXT_PLAIN)
                .build();
    }

    private static MessageRouterPublishRequest createTextPlainMRRequest(String topicPath, Duration timeout) {
        final MessageRouterSink sinkDefinition = createMRSink(topicPath, SERVER);
        return ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .contentType(ContentType.TEXT_PLAIN)
                .timeoutConfig(ImmutableDmaapTimeoutConfig.builder().timeout(timeout).build())
                .build();
    }

    private static MessageRouterSink createMRSink(String topicPath, DummyHttpServer dummyHttpServer) {
        return ImmutableMessageRouterSink.builder()
                .name("the topic")
                .topicUrl(String.format("http://dmaap-mr%s",
                        topicPath)
                )
                .build();
    }

    private static MessageRouterPublishResponse createErrorResponse(String failReason) {
        String failReasonFormat = failReason + "\n%s";
        return ImmutableMessageRouterPublishResponse
                .builder()
                .failReason(String.format(failReasonFormat, ERROR_MESSAGE))
                .build();
    }

    private void assertTimeoutError(DmaapResponse response) {
        assertThat(response.failed()).isTrue();
        assertThat(response.failReason()).startsWith(TIMEOUT_ERROR_MESSAGE);
    }

    private void assertConnectionError(DmaapResponse response) {
        assertThat(response.failed()).isTrue();
        assertThat(response.failReason()).startsWith(CONNECTION_ERROR_MESSAGE);
    }
}

