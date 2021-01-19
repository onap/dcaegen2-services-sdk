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
import io.vavr.collection.HashSet;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.config.RetryConfig;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClient.ResponseReceiver;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.util.stream.Collectors;

/**
 * @since 1.1.4
 */
public class RxHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RxHttpClient.class);
    private final HttpClient httpClient;
    private RetryConfig retryConfig;

    RxHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    RxHttpClient(HttpClient httpClient, RetryConfig retryConfig) {
        this(httpClient);
        this.retryConfig = retryConfig;
    }

    public Mono<HttpResponse> call(HttpRequest request) {
        Mono<HttpResponse> httpResponseMono = response(request);
        return Option.of(retryConfig)
                .map(rc -> retryConfig(rc, request.diagnosticContext()))
                .map(httpResponseMono::retryWhen)
                .getOrElse(() -> httpResponseMono);
    }

    ResponseReceiver<?> prepareRequest(HttpRequest request) {
        final HttpClient simpleClient = httpClient
                .doOnRequest((req, conn) -> logRequest(request.diagnosticContext(), req))
                .doOnResponse((rsp, conn) -> logResponse(request.diagnosticContext(), rsp))
                .headers(hdrs -> request.headers().forEach(hdr -> hdrs.set(hdr._1, hdr._2)));

        final HttpClient theClient = Option.of(request.timeout())
                .map(simpleClient::responseTimeout)
                .getOrElse(simpleClient);

        return prepareBody(request, theClient);
    }

    private Mono<HttpResponse> response(HttpRequest request) {
        return prepareRequest(request)
                .responseSingle((resp, content) -> mapResponse(request.url(), resp.status(), content));
    }

    private Mono<HttpResponse> mapResponse(String url, HttpResponseStatus status, reactor.netty.ByteBufMono content) {
        if (shouldRetry(status.code())) {
            return Mono.error(new RetryConfig.RetryableException());
        }
        return content.asByteArray()
                .defaultIfEmpty(new byte[0])
                .map(bytes -> new NettyHttpResponse(url, status, bytes));
    }

    private boolean shouldRetry(int code) {
        return Option.of(retryConfig)
                .map(RetryConfig::retryableHttpResponseCodes)
                .getOrElse(HashSet::empty)
                .toStream()
                .exists(c -> Integer.valueOf(code).equals(c));
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
                .headers(hdrs -> hdrs.set(HttpHeaders.TRANSFER_ENCODING_TYPE, HttpHeaders.CHUNKED))
                .request(request.method().asNetty())
                .send(Flux.from(request.body().contents()))
                .uri(request.url());
    }

    private ResponseReceiver<?> prepareBodyUnchunked(HttpRequest request, HttpClient theClient) {
        return theClient
                .headers(hdrs -> hdrs.set(HttpHeaders.CONTENT_LENGTH, request.body().length().toString()))
                .request(request.method().asNetty())
                .send(Flux.from(request.body().contents()))
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

    private RetryBackoffSpec retryConfig(RetryConfig retryConfig, RequestDiagnosticContext context) {
        RetryBackoffSpec retry = Retry
                .fixedDelay(retryConfig.retryCount(), retryConfig.retryInterval())
                .doBeforeRetry(retrySignal -> context.withSlf4jMdc(
                        LOGGER.isTraceEnabled(), () -> LOGGER.trace("Retry: {}", retrySignal)))
                .filter(ex -> isRetryable(retryConfig, ex));

        return Option.of(retryConfig.onRetryExhaustedException())
                .map(ex -> retry.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> ex))
                .getOrElse(retry);
    }

    private boolean isRetryable(RetryConfig retryConfig, Throwable ex) {
        return retryConfig.retryableExceptions()
                .toStream()
                .exists(clazz -> clazz.isAssignableFrom(ex.getClass()));
    }
}
