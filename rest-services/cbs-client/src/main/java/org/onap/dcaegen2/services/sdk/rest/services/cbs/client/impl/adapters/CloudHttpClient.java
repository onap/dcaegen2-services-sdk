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
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.DiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableDiagnosticContext;
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
        this(HttpClient.create()
                .doOnRequest(CloudHttpClient::logRequest)
                .doOnResponse(CloudHttpClient::logResponse));
    }


    CloudHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public <T> Mono<T> callHttpGet(String url, Class<T> bodyClass) {
        return callHttpGet(url, ImmutableDiagnosticContext.builder().build(), bodyClass);
    }

    public <T> Mono<T> callHttpGet(String url, DiagnosticContext context, Class<T> bodyClass) {
        return httpClient
                .headers(headers -> context.remoteCallHttpHeaders().forEach((BiConsumer<String, String>) headers::set))
                .get()
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

    private static void logRequest(HttpClientRequest httpClientRequest, Connection connection) {
        LOGGER.debug("Request: {} {}", httpClientRequest.method(), httpClientRequest.uri());
        if (LOGGER.isTraceEnabled()) {
            final String headers = Stream.ofAll(httpClientRequest.requestHeaders())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("\n"));
            LOGGER.trace(headers);
        }
    }

    private static void logResponse(HttpClientResponse httpClientResponse, Connection connection) {
        LOGGER.debug("Response status: {}", httpClientResponse.status());
    }

}
