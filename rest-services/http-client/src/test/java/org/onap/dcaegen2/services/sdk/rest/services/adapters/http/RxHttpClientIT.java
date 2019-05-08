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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendString;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions.HttpException;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RxHttpClientIT {

    private static final Duration TIMEOUT = Duration.ofHours(5);
    private final RxHttpClient cut = RxHttpClientFactory.create();
    private static DummyHttpServer httpServer;

    @BeforeAll
    static void setUpClass() {
        httpServer = DummyHttpServer.start(routes ->
                routes.get("/sample-get", (req, resp) -> sendString(resp, Mono.just("OK")))
                        .get("/sample-get-500", (req, resp) -> resp.status(HttpResponseStatus.INTERNAL_SERVER_ERROR).send())
                        .post("/headers-post", (req, resp) -> resp
                                .sendString(Mono.just(req.requestHeaders().toString())))
                        .post("/echo-post", (req, resp) -> resp.send(req.receive().retain()))
        );
    }

    @AfterAll
    static void tearDownClass() {
        httpServer.close();
    }

    private ImmutableHttpRequest.Builder requestFor(String path) throws MalformedURLException {
        return ImmutableHttpRequest.builder()
                .url(new URL("http", httpServer.host(), httpServer.port(), path).toString());
    }

    @Test
    void simpleGet() throws Exception {
        // given
        final HttpRequest httpRequest = requestFor("/sample-get").method(HttpMethod.GET).build();

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString).expectNext("OK").expectComplete().verify(TIMEOUT);
    }

    @Test
    void getWithError() throws Exception {
        // given
        final HttpRequest httpRequest = requestFor("/sample-get-500").method(HttpMethod.GET).build();

        // when
        final Mono<String> bodyAsString = cut.call(httpRequest)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(HttpResponse::bodyAsString);

        // then
        StepVerifier.create(bodyAsString).expectError(HttpException.class).verify(TIMEOUT);
    }

    @Test
    void simplePost() throws Exception {
        // given
        final String requestBody = "hello world";
        final HttpRequest httpRequest = requestFor("/echo-post")
                .method(HttpMethod.POST)
                .body(RequestBody.fromString(requestBody))
                .build();

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
}