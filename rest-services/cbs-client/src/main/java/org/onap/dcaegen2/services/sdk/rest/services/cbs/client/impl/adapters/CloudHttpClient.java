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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.adapters;

import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpStatusClass;
import io.vavr.collection.Stream;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/15/18
 */

public class CloudHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudHttpClient.class);

    private final Gson gson = new Gson();
    private final HttpClient httpClient;

    public CloudHttpClient() {
        this(HttpClient.create());
    }


    CloudHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public <T> Mono<T> get(String url, RequestDiagnosticContext context, Class<T> bodyClass) {
        final HttpClient clientWithHeaders = httpClient
                .doOnRequest((req, conn) -> logRequest(context, req))
                .doOnResponse((rsp, conn) -> logResponse(context, rsp))
                .headers(hdrs -> context.remoteCallHttpHeaders().forEach((BiConsumer<String, String>) hdrs::set));
        return callHttpGet(clientWithHeaders, url, bodyClass);
    }

    public <T> Mono<T> get(String url, Class<T> bodyClass) {
        return callHttpGet(httpClient, url, bodyClass);
    }

    private <T> Mono<T> callHttpGet(HttpClient client, String url, Class<T> bodyClass) {
        return client.get()
                .uri(url)
                .responseSingle((resp, content) -> HttpStatusClass.SUCCESS.contains(resp.status().code())
                        ? content.asString()
                        : Mono.error(createException(url, resp)))
                .map(body -> parseJson(body, bodyClass));
    }

    private Exception createException(String url, HttpClientResponse response) {
        return new IOException(String.format("Request failed for URL '%s'. Response code: %s",
                url,
                response.status()));
    }

    private <T> T parseJson(String body, Class<T> bodyClass) {
        return gson.fromJson(body, bodyClass);
    }

    private void logRequest(RequestDiagnosticContext context, HttpClientRequest httpClientRequest) {
        context.withSlf4jMdc(LOGGER.isDebugEnabled(), () -> {
            LOGGER.debug("Request: {} {}", httpClientRequest.method(), httpClientRequest.uri());
            if (LOGGER.isTraceEnabled()) {
                final String headers = Stream.ofAll(httpClientRequest.requestHeaders())
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining("\n"));
                LOGGER.trace(headers);
            }
        });
    }

    private void logResponse(RequestDiagnosticContext context, HttpClientResponse httpClientResponse) {
        context.withSlf4jMdc(LOGGER.isDebugEnabled(), () -> {
            LOGGER.debug("Response status: {}", httpClientResponse.status());
        });
    }
}

