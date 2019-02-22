/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.http.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;

import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/15/18
 */

public class CloudHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudHttpClient.class);

    private final Gson gson;
    private final HttpClient httpClient;


    public CloudHttpClient() {
        this(HttpClient.create().doOnRequest(logRequest()).doOnResponse(logResponse()));
    }


    CloudHttpClient(HttpClient httpClient) {
        this.gson = new Gson();
        this.httpClient = httpClient;
    }


    public <T> Mono<T> callHttpGet(String url, Class<T> genericClassDeclaration) {
        return httpClient
            .baseUrl(url)
            .doOnResponseError(doOnError())
            .get()
            .responseSingle((httpClientResponse, content) -> getJsonFromRequest(content.toString(), genericClassDeclaration) );

            /*
            .baseUrl(url)
            .get()
            .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(getException(response)))
            .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(getException(response)))
            .bodyToMono(String.class)
            .flatMap(body -> getJsonFromRequest(body, genericClassDeclaration));
            */
    }

    @NotNull
    private BiConsumer<HttpClientResponse, Throwable> doOnError() {
        return (httpClientResponse, throwable) -> {
            Mono.error(getException(httpClientResponse));
        };
    }


    private RuntimeException getException(HttpClientResponse response) {
        return new RuntimeException(String.format("Request for cloud config failed: HTTP %d",
            response.status().code()));
    }

    private <T> Mono<T> getJsonFromRequest(String body, Class<T> genericClassDeclaration) {
        try {
            return Mono.just(parseJson(body, genericClassDeclaration));
        } catch (JsonSyntaxException | IllegalStateException e) {
            return Mono.error(e);
        }
    }

    private <T> T parseJson(String body, Class<T> genericClassDeclaration) {
        return gson.fromJson(body, genericClassDeclaration);
    }


    private static BiConsumer<HttpClientRequest, Connection> logRequest() {
        return (httpClientRequest, connection) -> {
            LOGGER.info("Request: {} {}", httpClientRequest.method(), httpClientRequest.uri());
            httpClientRequest.requestHeaders().forEach(stringStringEntry -> {
                LOGGER.info("{}={}", stringStringEntry.getKey(), stringStringEntry.getValue());
            });

        };
    }

    private static BiConsumer<? super HttpClientResponse, ? super Connection> logResponse() {
        return (httpClientresponse, connection) -> {

            LOGGER.info("Response status {}", httpClientresponse.status().code());

        };
    }


}
