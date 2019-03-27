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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.vavr.collection.HashMap;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpHeaders;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
// TODO: This is a PoC. It's untested.
public class MessageRouterClientImpl implements MessageRouterPublisher, MessageRouterSubscriber {

    private static final Duration WINDOW_MAX_TIME = Duration.ofSeconds(1);
    private static final int WINDOW_MAX_SIZE = 512;
    private final RxHttpClient httpClient;
    private final Gson gson;

    public MessageRouterClientImpl(RxHttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    @Override
    public Flux<MessageRouterPublishResponse> put(
            MessageRouterPublishRequest request,
            Flux<? extends JsonElement> items) {
        return items.windowTimeout(WINDOW_MAX_SIZE, WINDOW_MAX_TIME).flatMap(subItems ->
                subItems.collect(JsonArray::new, JsonArray::add)
                        .filter(arr -> arr.size() > 0)
                        .map(RequestBody::fromJson)
                        .flatMap(body -> httpClient.call(buildPostHttpRequest(request, body)))
                        .map(this::buildPutResponse));
    }

    @Override
    public Mono<MessageRouterSubscribeResponse> get(MessageRouterSubscribeRequest request) {
        return httpClient.call(buildGetHttpRequest(request)).map(this::buildGetResponse);
    }

    private @NotNull MessageRouterPublishResponse buildPutResponse(HttpResponse httpResponse) {
        final ImmutableMessageRouterPublishResponse.Builder builder =
                ImmutableMessageRouterPublishResponse.builder();
        return httpResponse.successful()
                ? builder.build()
                : builder.failReason(extractFailReason(httpResponse)).build();
    }

    private @NotNull MessageRouterSubscribeResponse buildGetResponse(HttpResponse httpResponse) {
        final ImmutableMessageRouterSubscribeResponse.Builder builder =
                ImmutableMessageRouterSubscribeResponse.builder();
        return httpResponse.successful()
                ? builder.items(httpResponse.bodyAsJson(StandardCharsets.UTF_8, gson, JsonArray.class)).build()
                : builder.failReason(extractFailReason(httpResponse)).build();
    }

    private String extractFailReason(HttpResponse httpResponse) {
        return String.format("%d %s%n%s", httpResponse.statusCode(), httpResponse.statusReason(),
                httpResponse.bodyAsString());
    }

    private @NotNull HttpRequest buildPostHttpRequest(MessageRouterPublishRequest request, RequestBody body) {
        return ImmutableHttpRequest.builder()
                .method(HttpMethod.POST)
                .url(request.sinkDefinition().topicUrl())
                .diagnosticContext(request.diagnosticContext())
                .customHeaders(HashMap.of(HttpHeaders.CONTENT_TYPE, request.contentType()))
                .body(body)
                .build();
    }

    private @NotNull HttpRequest buildGetHttpRequest(MessageRouterSubscribeRequest request) {
        return ImmutableHttpRequest.builder()
                .method(HttpMethod.GET)
                .url(buildSubscribeUrl(request))
                .diagnosticContext(request.diagnosticContext())
                .build();
    }

    private String buildSubscribeUrl(MessageRouterSubscribeRequest request) {
        return String.format("%s/%s/%s", request.sourceDefinition().topicUrl(), request.consumerGroup(), request.consumerId());
    }
}
