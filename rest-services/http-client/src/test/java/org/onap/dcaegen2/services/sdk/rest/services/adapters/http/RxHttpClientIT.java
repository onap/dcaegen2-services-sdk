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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.ReadTimeoutException;
import io.vavr.Tuple;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.ImmutableRetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.ImmutableRxHttpClientConfig;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions.HttpException;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendInOrderWithDelay;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendString;

class RxHttpClientIT {

    private static final Duration TIMEOUT = Duration.ofHours(5);
    private static final Duration NO_DELAY = Duration.ofSeconds(0);
    private static final int RETRY_COUNT = 1;
    private static final int EXPECTED_REQUESTS_WHEN_RETRY = RETRY_COUNT + 1;
    private static final DummyHttpServer HTTP_SERVER = initialize();
    private static final DummyHttpServer DISPOSED_HTTP_SERVER = initialize().closeAndGet();
    public static final Mono<String> OK = Mono.just("OK");
    public static final Duration RETRY_INTERVAL = Duration.ofMillis(1);
    private static AtomicInteger REQUEST_COUNTER;

    private static DummyHttpServer initialize() {
        return DummyHttpServer.start(routes -> routes
                .get("/sample-get", (req, resp) -> sendString(resp, OK))
                .get("/delay-get", (req, resp) ->
                        sendInOrderWithDelay(REQUEST_COUNTER, Tuple.of(resp, 200, Duration.ofSeconds(3))))
                .get("/sample-get-500", (req, resp) -> resp.status(HttpResponseStatus.INTERNAL_SERVER_ERROR).send())
                .get("/retry-get-500", (req, resp) ->
                        sendInOrderWithDelay(REQUEST_COUNTER,
                                Tuple.of(resp, 500, NO_DELAY), Tuple.of(resp, 500, NO_DELAY)))
                .get("/retry-get-400", (req, resp) ->
                        sendInOrderWithDelay(REQUEST_COUNTER, Tuple.of(resp, 400, NO_DELAY)))
                .get("/retry-get-500-200", (req, resp) ->
                        sendInOrderWithDelay(REQUEST_COUNTER,
                                Tuple.of(resp, 500, NO_DELAY), Tuple.of(resp, 200, NO_DELAY)))
                .get("/retry-get-200", (req, resp) ->
                        sendInOrderWithDelay(REQUEST_COUNTER, Tuple.of(resp, 200, NO_DELAY)))
                .post("/headers-post", (req, resp) -> resp
                        .sendString(Mono.just(req.requestHeaders().toString())))
                .post("/echo-post", (req, resp) -> resp.send(req.receive().retain()))
        );
    }

    @AfterAll
    static void tearDownClass() {
        HTTP_SERVER.close();
    }

    @Test
    void simpleGet() throws Exception {
        // given
        final HttpRequest httpRequest = requestFor("/sample-get")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create();

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString)
                .expectNext("OK")
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void getWithError() throws Exception {
        // given
        final HttpRequest httpRequest = requestFor("/sample-get-500")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create();

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString)
                .expectError(HttpException.class)
                .verify(TIMEOUT);
    }

    @Test
    void simplePost() throws Exception {
        // given
        final String requestBody = "hello world";
        final HttpRequest httpRequest = requestFor("/echo-post")
                .method(HttpMethod.POST)
                .body(RequestBody.fromString(requestBody))
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create();

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString)
                .expectNext(requestBody)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void testChunkedEncoding() throws Exception {
        // given
        final String requestBody = "hello world";
        final HttpRequest httpRequest = requestFor("/headers-post")
                .method(HttpMethod.POST)
                .body(RequestBody.chunkedFromString(Mono.just(requestBody)))
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create();

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString.map(String::toLowerCase))
                .consumeNextWith(responseBody -> {
                    assertThat(responseBody).contains("transfer-encoding: chunked");
                    assertThat(responseBody).doesNotContain("content-length");
                })
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void testUnchunkedEncoding() throws Exception {
        // given
        final String requestBody = "hello world";
        final HttpRequest httpRequest = requestFor("/headers-post")
                .method(HttpMethod.POST)
                .body(RequestBody.fromString(requestBody))
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create();

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString.map(String::toLowerCase))
                .consumeNextWith(responseBody -> {
                    assertThat(responseBody).doesNotContain("transfer-encoding");
                    assertThat(responseBody).contains("content-length");
                })
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void getWithTimeoutError() throws Exception {
        // given
        REQUEST_COUNTER = new AtomicInteger();
        final HttpRequest httpRequest = requestFor("/delay-get")
                .method(HttpMethod.GET)
                .timeout(Duration.ofMillis(1))
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create(ImmutableRxHttpClientConfig.builder().build());

        // when
        final Mono<HttpResponse> response = cut.call(httpRequest);

        // then
        StepVerifier.create(response)
                .expectError(ReadTimeoutException.class)
                .verify(TIMEOUT);
        assertNoServerResponse();
    }

    @Test
    void getWithRetryExhaustedExceptionWhenClosedServer() throws Exception {
        // given
        REQUEST_COUNTER = new AtomicInteger();
        final HttpRequest httpRequest = requestForClosedServer("/sample-get")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create(ImmutableRxHttpClientConfig.builder()
                .retryConfig(defaultRetryConfig()
                        .customRetryableExceptions(HashSet.of(ConnectException.class))
                        .build())
                .build());

        // when
        final Mono<HttpResponse> response = cut.call(httpRequest);

        // then
        StepVerifier.create(response)
                .expectError(IllegalStateException.class)
                .verify(TIMEOUT);
        assertNoServerResponse();
    }

    @Test
    void getWithCustomRetryExhaustedExceptionWhenClosedServer() throws Exception {
        // given
        REQUEST_COUNTER = new AtomicInteger();
        final HttpRequest httpRequest = requestForClosedServer("/sample-get")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create(ImmutableRxHttpClientConfig.builder()
                .retryConfig(defaultRetryConfig()
                        .customRetryableExceptions(HashSet.of(ConnectException.class))
                        .onRetryExhaustedException(ReadTimeoutException.INSTANCE)
                        .build())
                .build());

        // when
        final Mono<HttpResponse> response = cut.call(httpRequest);

        // then
        StepVerifier.create(response)
                .expectError(ReadTimeoutException.class)
                .verify(TIMEOUT);
        assertNoServerResponse();
    }

    @Test
    void getWithRetryExhaustedExceptionWhen500() throws Exception {
        // given
        REQUEST_COUNTER = new AtomicInteger();
        final HttpRequest httpRequest = requestFor("/retry-get-500")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create(ImmutableRxHttpClientConfig.builder()
                .retryConfig(defaultRetryConfig()
                        .retryableHttpResponseCodes(HashSet.of(500))
                        .build())
                .build());

        // when
        final Mono<HttpResponse> response = cut.call(httpRequest);

        // then
        StepVerifier.create(response)
                .expectError(IllegalStateException.class)
                .verify(TIMEOUT);
        assertRetry();
    }

    @Test
    void getWithCustomRetryExhaustedExceptionWhen500() throws Exception {
        // given
        REQUEST_COUNTER = new AtomicInteger();
        final HttpRequest httpRequest = requestFor("/retry-get-500")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create(ImmutableRxHttpClientConfig.builder()
                .retryConfig(defaultRetryConfig()
                        .onRetryExhaustedException(ReadTimeoutException.INSTANCE)
                        .retryableHttpResponseCodes(HashSet.of(500))
                        .build())
                .build());

        // when
        final Mono<HttpResponse> response = cut.call(httpRequest);

        // then
        StepVerifier.create(response)
                .expectError(ReadTimeoutException.class)
                .verify(TIMEOUT);
        assertRetry();
    }

    @Test
    void getWithRetryWhen500AndThen200() throws Exception {
        // given
        REQUEST_COUNTER = new AtomicInteger();
        final HttpRequest httpRequest = requestFor("/retry-get-500-200")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create(ImmutableRxHttpClientConfig.builder()
                .retryConfig(defaultRetryConfig()
                        .retryableHttpResponseCodes(HashSet.of(500))
                        .build())
                .build());

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString)
                .expectNext("OK")
                .expectComplete()
                .verify(TIMEOUT);
        assertRetry();
    }

    @Test
    void getWithoutRetryWhen200() throws Exception {
        // given
        REQUEST_COUNTER = new AtomicInteger();
        final HttpRequest httpRequest = requestFor("/retry-get-200")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create(ImmutableRxHttpClientConfig.builder()
                .retryConfig(defaultRetryConfig()
                        .retryableHttpResponseCodes(HashSet.of(500))
                        .build())
                .build());

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString)
                .expectNext("OK")
                .expectComplete()
                .verify(TIMEOUT);
        assertNoRetry();
    }

    @Test
    void getWithoutRetryWhen400() throws Exception {
        // given
        REQUEST_COUNTER = new AtomicInteger();
        final HttpRequest httpRequest = requestFor("/retry-get-400")
                .method(HttpMethod.GET)
                .build();
        final RxHttpClient cut = RxHttpClientFactory.create(ImmutableRxHttpClientConfig.builder()
                .retryConfig(defaultRetryConfig()
                        .retryableHttpResponseCodes(HashSet.of(500))
                        .build())
                .build());

        // when
        Mono<HttpResponse> result = cut.call(httpRequest);

        // then
        StepVerifier.create(result)
                .consumeNextWith(this::assert400)
                .expectComplete()
                .verify(TIMEOUT);
        assertNoRetry();
    }

    private ImmutableHttpRequest.Builder requestFor(String path) throws MalformedURLException {
        return ImmutableHttpRequest.builder()
                .url(new URL("http", HTTP_SERVER.host(), HTTP_SERVER.port(), path).toString());
    }

    private ImmutableHttpRequest.Builder requestForClosedServer(String path) throws MalformedURLException {
        return ImmutableHttpRequest.builder()
                .url(new URL("http", DISPOSED_HTTP_SERVER.host(), DISPOSED_HTTP_SERVER.port(), path).toString());
    }

    private ImmutableRetryConfig.Builder defaultRetryConfig() {
        return ImmutableRetryConfig.builder()
                .retryCount(RETRY_COUNT)
                .retryInterval(RETRY_INTERVAL);
    }

    private void assertRetry() {
        assertThat(REQUEST_COUNTER.get()).isEqualTo(EXPECTED_REQUESTS_WHEN_RETRY);
    }

    private void assertNoRetry() {
        assertThat(REQUEST_COUNTER.get()).isOne();
    }

    private void assertNoServerResponse() {
        assertThat(REQUEST_COUNTER.get()).isZero();
    }
    
    private void assert400(HttpResponse httpResponse) {
        assertThat(httpResponse.statusCode()).isEqualTo(400);
    }
}
