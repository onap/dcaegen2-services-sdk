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

import com.google.gson.Gson;
import io.netty.handler.ssl.SslContext;
import io.vavr.collection.HashMap;
import java.util.Collections;
import java.util.Map;
import org.onap.dcaegen2.services.sdk.rest.services.model.ClientModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/15/18
 * @deprecated use {@link SimpleHttpClient} instead
 */
@Deprecated
public class CloudHttpClient {

    private final Gson gson = new Gson();
    private final SimpleHttpClient httpClient;

    public CloudHttpClient() {
        this(new SimpleHttpClient());
    }

    public CloudHttpClient(SslContext sslContext) {
        this(new SimpleHttpClient(sslContext));
    }

    CloudHttpClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    CloudHttpClient(HttpClient httpClient) {
        this(new SimpleHttpClient(httpClient));
    }


    public <T> Mono<T> get(String url, Class<T> bodyClass) {
        return get(url, RequestDiagnosticContext.create(), bodyClass);
    }

    public <T> Mono<T> get(String url, RequestDiagnosticContext context, Class<T> bodyClass) {
        return get(url, context, Collections.emptyMap(), bodyClass);
    }

    public <T> Mono<T> get(
            String url,
            RequestDiagnosticContext context,
            Map<String, String> customHeaders,
            Class<T> bodyClass) {
        return httpClient.call(
                ImmutableHttpRequest.builder()
                        .url(url)
                        .customHeaders(HashMap.ofAll(customHeaders))
                        .diagnosticContext(context)
                        .build())
                .map(resp -> gson.fromJson(resp, bodyClass));
    }


    public Mono<HttpClientResponse> post(
            String url,
            RequestDiagnosticContext context,
            Map<String, String> customHeaders,
            JsonBodyBuilder jsonBodyBuilder,
            ClientModel clientModel) {
        return callForRawResponse(url, context, customHeaders, jsonBodyBuilder, clientModel, HttpMethod.POST);
    }

    public Mono<HttpClientResponse> patch(
            String url,
            RequestDiagnosticContext context,
            Map<String, String> customHeaders,
            JsonBodyBuilder jsonBodyBuilder,
            ClientModel clientModel) {
        return callForRawResponse(url, context, customHeaders, jsonBodyBuilder, clientModel, HttpMethod.PATCH);
    }


    private Mono<HttpClientResponse> callForRawResponse(
            String url,
            RequestDiagnosticContext context,
            Map<String, String> customHeaders,
            JsonBodyBuilder jsonBodyBuilder,
            ClientModel clientModel,
            HttpMethod method) {
        return httpClient.prepareRequest(
                ImmutableHttpRequest.builder()
                        .url(url)
                        .customHeaders(HashMap.ofAll(customHeaders))
                        .diagnosticContext(context)
                        .body(RequestBody.fromString(jsonBodyBuilder.createJsonBody(clientModel)))
                        .method(method)
                        .build())
                .responseSingle((httpClientResponse, byteBufMono) -> Mono.just(httpClientResponse));
    }

}

