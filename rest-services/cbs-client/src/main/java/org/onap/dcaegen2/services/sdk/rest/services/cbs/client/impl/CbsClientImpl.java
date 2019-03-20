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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ResponseTransformer;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.SimpleHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;

public class CbsClientImpl implements CbsClient {

    private final SimpleHttpClient httpClient;
    private final String fetchUrl;

    CbsClientImpl(SimpleHttpClient httpClient, URL fetchUrl) {
        this.httpClient = httpClient;
        this.fetchUrl = fetchUrl.toString();
    }

    public static CbsClientImpl create(SimpleHttpClient httpClient, InetSocketAddress cbsAddress, String serviceName) {
        return new CbsClientImpl(httpClient, constructUrl(cbsAddress, serviceName));
    }

    private static URL constructUrl(InetSocketAddress cbsAddress, String serviceName) {
        try {
            return new URL(
                    "http",
                    cbsAddress.getHostString(),
                    cbsAddress.getPort(),
                    "/service_component/" + serviceName);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid CBS URL", e);
        }
    }

    @Override
    public @NotNull Mono<JsonObject> get(RequestDiagnosticContext diagnosticContext) {
        return Mono.defer(() ->
                httpClient.call(
                        ImmutableHttpRequest.builder()
                                .method(HttpMethod.GET)
                                .url(fetchUrl)
                                .diagnosticContext(diagnosticContext)
                                .build(),
                        ResponseTransformer.fromJson(JsonObject.class))
        );
    }
}
