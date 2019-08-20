/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

public class CbsClientImpl implements CbsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientImpl.class);
    private final RxHttpClient httpClient;
    private final String serviceName;
    private final InetSocketAddress cbsAddress;
    private final String protocol;

    public CbsClientImpl(RxHttpClient httpClient, String serviceName, InetSocketAddress cbsAddress, String protocol) {
        this.httpClient = httpClient;
        this.serviceName = serviceName;
        this.cbsAddress = cbsAddress;
        this.protocol = protocol;
    }

    @Override
    public @NotNull Mono<JsonObject> get(CbsRequest request) {
        return Mono.fromCallable(() -> constructUrl(request, protocol).toString())
                .doOnNext(this::logRequestUrl)
                .map(url -> ImmutableHttpRequest.builder()
                        .method(HttpMethod.GET)
                        .url(url)
                        .diagnosticContext(request.diagnosticContext())
                        .build())
                .flatMap(httpClient::call)
                .doOnNext(HttpResponse::throwIfUnsuccessful)
                .map(resp -> resp.bodyAsJson(JsonObject.class))
                .doOnNext(this::logCbsResponse);
    }


    private URL constructUrl(CbsRequest request, String protocol) {
        LOGGER.info("Protocol in URL set as: {}", protocol);
        try {
            return new URL(
                    protocol,
                    cbsAddress.getHostString(),
                    cbsAddress.getPort(),
                    request.requestPath().getForService(serviceName));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid CBS URL", e);
        }
    }

    private void logRequestUrl(String url) {
        LOGGER.debug("Calling {} for configuration", url);
    }

    private void logCbsResponse(JsonObject json) {
        LOGGER.info("Got successful response from Config Binding Service");
        LOGGER.debug("CBS response: {}", json);
    }
}
