/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2020 Nokia. All rights reserved.
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
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.ContentType;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendError;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendString;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */
class MessageRouterPublisherTest {

    private static final String ERROR_MESSAGE = "Something went wrong";
    private static final String SUCCESS_RESP_TOPIC_PATH = "/events/TOPIC";
    private static final String FAILING_WITH_400_RESP_PATH = "/events/TOPIC400";
    private static final String FAILING_WITH_401_RESP_PATH = "/events/TOPIC401";
    private static final String FAILING_WITH_403_RESP_PATH = "/events/TOPIC403";
    private static final String FAILING_WITH_404_RESP_PATH = "/events/TOPIC404";
    private static final String FAILING_WITH_500_TOPIC_PATH = "/events/TOPIC500";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Flux<JsonPrimitive> messageBatch = Flux.just("ala", "ma", "kota")
            .map(JsonPrimitive::new);
    private static final List<String> messageBatchItems = List.of("ala", "ma", "kota");

    private static DummyHttpServer server;
    private MessageRouterPublisher sut = DmaapClientFactory
            .createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());


    @BeforeAll
    static void setUp() {
        server = DummyHttpServer.start(routes -> routes
                .post(SUCCESS_RESP_TOPIC_PATH, (req, resp) ->
                        sendString(resp, Mono.just("OK")))
                .post(FAILING_WITH_400_RESP_PATH, (req, resp) ->
                        sendError(resp, 400, ERROR_MESSAGE))
                .post(FAILING_WITH_401_RESP_PATH, (req, resp) ->
                        sendError(resp, 401, ERROR_MESSAGE))
                .post(FAILING_WITH_403_RESP_PATH, (req, resp) ->
                        sendError(resp, 403, ERROR_MESSAGE))
                .post(FAILING_WITH_404_RESP_PATH, (req, resp) ->
                        sendError(resp, 404, ERROR_MESSAGE))
                .post(FAILING_WITH_500_TOPIC_PATH, (req, resp) ->
                        sendError(resp, 500, ERROR_MESSAGE))
        );
    }

    @Test
    void test_put_givenMessageBatch_shouldMakeSuccessfulPostRequestReturningBatch() {
        //given
        final MessageRouterPublishRequest mrRequest = createMRRequest(SUCCESS_RESP_TOPIC_PATH,
                ContentType.TEXT_PLAIN);
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
    void publisher_shouldHandleBadRequestError() {
        //given
        final MessageRouterPublishRequest mrRequest = createMRRequest(FAILING_WITH_400_RESP_PATH,
                ContentType.TEXT_PLAIN);
        final MessageRouterPublishResponse expectedResponse = createErrorResponse(
                "400 Bad Request\n%s", ERROR_MESSAGE);

        //when
        final Flux<MessageRouterPublishResponse> result = sut.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void publisher_shouldHandleUnauthorizedError() {
        //given
        final MessageRouterPublishRequest mrRequest = createMRRequest(FAILING_WITH_401_RESP_PATH,
                ContentType.TEXT_PLAIN);
        final MessageRouterPublishResponse expectedResponse = createErrorResponse(
                "401 Unauthorized\n%s", ERROR_MESSAGE);

        //when
        final Flux<MessageRouterPublishResponse> result = sut.put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void publisher_shouldHandleForbiddenError() {
        //given
        final MessageRouterPublishRequest mrRequest = createMRRequest(FAILING_WITH_403_RESP_PATH,
                ContentType.TEXT_PLAIN);
        final MessageRouterPublishResponse expectedResponse = createErrorResponse(
                "403 Forbidden\n%s", ERROR_MESSAGE);

        //when
        final Flux<MessageRouterPublishResponse> result = sut
                .put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void publisher_shouldHandleNotFoundError() {
        //given
        final MessageRouterPublishRequest mrRequest = createMRRequest(FAILING_WITH_404_RESP_PATH,
                ContentType.TEXT_PLAIN);
        final MessageRouterPublishResponse expectedResponse = createErrorResponse(
                "404 Not Found\n%s", ERROR_MESSAGE);

        //when
        final Flux<MessageRouterPublishResponse> result = sut
                .put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void publisher_shouldHandleInternalServerError() {
        //given
        final MessageRouterPublishRequest mrRequest = createMRRequest(FAILING_WITH_500_TOPIC_PATH,
                ContentType.TEXT_PLAIN);
        final MessageRouterPublishResponse expectedResponse = createErrorResponse(
                "500 Internal Server Error\n%s", ERROR_MESSAGE);

        //when
        final Flux<MessageRouterPublishResponse> result = sut
                .put(mrRequest, messageBatch);

        //then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify(TIMEOUT);
    }


    private MessageRouterPublishRequest createMRRequest(String topicPath, ContentType contentType) {
        final MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
                .name("the topic")
                .topicUrl(String.format("http://%s:%d%s",
                        server.host(),
                        server.port(),
                        topicPath)
                )
                .build();

        return ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .contentType(contentType)
                .build();
    }

    private MessageRouterPublishResponse createErrorResponse(String failReasonFormat, Object... formatArgs) {
        return ImmutableMessageRouterPublishResponse
                .builder()
                .failReason(String.format(failReasonFormat, formatArgs))
                .build();
    }
}

