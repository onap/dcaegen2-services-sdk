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

import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.Commons.extractFailReason;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import java.time.Duration;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpHeaders;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.utlis.ContentType;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public class MessageRouterPublisherImpl implements MessageRouterPublisher {
    private final RxHttpClient httpClient;
    private final int maxBatchSize;
    private final Duration maxBatchDuration;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRouterPublisherImpl.class);

    public MessageRouterPublisherImpl(RxHttpClient httpClient, int maxBatchSize, Duration maxBatchDuration) {
        this.httpClient = httpClient;
        this.maxBatchSize = maxBatchSize;
        this.maxBatchDuration = maxBatchDuration;
    }

    @Override
    public Flux<MessageRouterPublishResponse> put(
            MessageRouterPublishRequest request,
            Flux<? extends JsonElement> items) {
        return items.bufferTimeout(maxBatchSize, maxBatchDuration)
                .flatMap(subItems -> subItems.isEmpty() ? Mono.empty() : pushBatchToMr(request, List.ofAll(subItems)));
    }

    private Publisher<? extends MessageRouterPublishResponse> pushBatchToMr(
            MessageRouterPublishRequest request,
            List<JsonElement> batch) {
        LOGGER.debug("Sending a batch of {} items to DMaaP MR", batch.size());
        LOGGER.trace("The items to be sent: {}", batch);
        return httpClient.call(buildHttpRequest(request, createRequestBody(batch, request.contentType())))
                .map(httpResponse -> buildResponse(httpResponse, batch));
    }

    private @NotNull RequestBody createRequestBody(List<? extends JsonElement> subItems, String contentType) {
        if(contentType.equals(ContentType.APPLICATION_JSON.getContentType())) {
            final JsonArray elements = new JsonArray(subItems.size());
            subItems.forEach(elements::add);
            return RequestBody.fromJson(elements);
        }else {
            String messages = subItems.map(JsonElement::toString).collect(Collectors.joining("\n"));
            return RequestBody.fromString(messages.replaceAll("\"", ""));
        }
    }

    private @NotNull HttpRequest buildHttpRequest(MessageRouterPublishRequest request, RequestBody body) {
        return ImmutableHttpRequest.builder()
                .method(HttpMethod.POST)
                .url(request.sinkDefinition().topicUrl())
                .diagnosticContext(request.diagnosticContext().withNewInvocationId())
                .customHeaders(HashMap.of(HttpHeaders.CONTENT_TYPE, request.contentType()))
                .body(body)
                .build();
    }

    private MessageRouterPublishResponse buildResponse(
            HttpResponse httpResponse, List<JsonElement> batch) {
        final ImmutableMessageRouterPublishResponse.Builder builder =
                ImmutableMessageRouterPublishResponse.builder();
        return httpResponse.successful()
                ? builder.items(batch).build()
                : builder.failReason(extractFailReason(httpResponse)).build();
    }
}
