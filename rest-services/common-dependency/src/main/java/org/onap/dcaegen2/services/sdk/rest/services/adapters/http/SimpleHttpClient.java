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

import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.ssl.SslContext;
import io.vavr.collection.Stream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions.HttpException;
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
public class SimpleHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);
    private final HttpClient httpClient;

    public static SimpleHttpClient create() {
        return new SimpleHttpClient(HttpClient.create());
    }

    public static SimpleHttpClient create(SslContext sslContext) {
        return new SimpleHttpClient(HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext)));
    }

    SimpleHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Mono<String> call(HttpRequest request) {
        return call(request, StandardCharsets.UTF_8);
    }

    public Mono<String> call(HttpRequest request, Charset charset) {
        return call(request, ResponseTransformer.asString(charset));
    }

    public <T> Mono<T> call(HttpRequest request, ResponseTransformer<T> responseTransformer) {
        return prepareRequest(request)
                .responseSingle((resp, content) -> HttpStatusClass.SUCCESS.contains(resp.status().code())
                        ? content.as(responseTransformer)
                        : Mono.error(createException(request, resp)));
    }

    ResponseReceiver<?> prepareRequest(HttpRequest request) {
        return httpClient
                .doOnRequest((req, conn) -> logRequest(request.diagnosticContext(), req))
                .doOnResponse((rsp, conn) -> logResponse(request.diagnosticContext(), rsp))
                .headers(hdrs -> request.headers().forEach(hdr -> hdrs.set(hdr._1, hdr._2)))
                .request(request.method().asNetty())
                .send(request.body())
                .uri(request.url());

    }

    private Exception createException(HttpRequest request, HttpClientResponse response) {
        return new HttpException(request.url(), response.status().code(), response.status().reasonPhrase());
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
        context.withSlf4jMdc(LOGGER.isDebugEnabled(), () -> LOGGER.debug("Response status: {}", httpClientResponse.status()));
    }
}

