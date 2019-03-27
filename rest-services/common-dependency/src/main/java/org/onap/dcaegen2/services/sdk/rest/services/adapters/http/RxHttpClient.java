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

import io.netty.handler.ssl.SslContext;
import io.vavr.collection.Stream;
import java.util.stream.Collectors;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClient.ResponseReceiver;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

/**
 * @since 1.1.4
 */
public class RxHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RxHttpClient.class);
    private final HttpClient httpClient;

    public static RxHttpClient create() {
        return new RxHttpClient(HttpClient.create());
    }

    // TODO: hide netty from public api (io.netty.handler.ssl.SslContext)
    public static RxHttpClient create(SslContext sslContext) {
        return new RxHttpClient(HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext)));
    }

    RxHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Mono<HttpResponse> call(HttpRequest request) {
        return prepareRequest(request)
                .responseSingle((resp, content) ->
                        content.asByteArray()
                                .defaultIfEmpty(new byte[0])
                                .map(bytes -> new NettyHttpResponse(request.url(), resp.status(), bytes)));
    }

    ResponseReceiver<?> prepareRequest(HttpRequest request) {
        final HttpClient theClient = httpClient
                .doOnRequest((req, conn) -> logRequest(request.diagnosticContext(), req))
                .doOnResponse((rsp, conn) -> logResponse(request.diagnosticContext(), rsp))
                .headers(hdrs -> request.headers().forEach(hdr -> hdrs.set(hdr._1, hdr._2)));

        return prepareBody(request, theClient);
    }

    private ResponseReceiver<?> prepareBody(HttpRequest request, HttpClient theClient) {
        if (request.body() == null) {
            return prepareBodyWithoutContents(request, theClient);
        } else {
            return request.body().length() == null
                    ? prepareBodyChunked(request, theClient)
                    : prepareBodyUnchunked(request, theClient);
        }
    }

    private ResponseReceiver<?> prepareBodyChunked(HttpRequest request, HttpClient theClient) {
        return theClient
                .chunkedTransfer(true)
                .request(request.method().asNetty())
                .send(request.body().contents())
                .uri(request.url());
    }

    private ResponseReceiver<?> prepareBodyUnchunked(HttpRequest request, HttpClient theClient) {
        return theClient
                .headers(hdrs -> hdrs.set(HttpHeaders.CONTENT_LENGTH, request.body().length().toString()))
                .request(request.method().asNetty())
                .send(request.body().contents())
                .uri(request.url());
    }

    private ResponseReceiver<?> prepareBodyWithoutContents(HttpRequest request, HttpClient theClient) {
        return theClient
                .request(request.method().asNetty())
                .uri(request.url());
    }

    private void logRequest(RequestDiagnosticContext context, HttpClientRequest httpClientRequest) {
        context.withSlf4jMdc(LOGGER.isDebugEnabled(), () -> {
            LOGGER.debug("Request: {} {} {}", httpClientRequest.method(), httpClientRequest.uri(),
                    httpClientRequest.requestHeaders());
            if (LOGGER.isTraceEnabled()) {
                final String headers = Stream.ofAll(httpClientRequest.requestHeaders())
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining("\n"));
                LOGGER.trace(headers);
            }
        });
    }

    private void logResponse(RequestDiagnosticContext context, HttpClientResponse httpClientResponse) {
        context.withSlf4jMdc(LOGGER.isDebugEnabled(),
                () -> LOGGER.debug("Response status: {}", httpClientResponse.status()));
    }
}
