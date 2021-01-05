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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl;

import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.Commons.extractFailReason;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.handler.timeout.ReadTimeoutException;
import io.vavr.collection.List;
import java.nio.charset.StandardCharsets;

import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReason;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReasonPresenter;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReasons;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public class MessageRouterSubscriberImpl implements MessageRouterSubscriber {
    private final RxHttpClient httpClient;
    private final Gson gson;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRouterSubscriberImpl.class);

    public MessageRouterSubscriberImpl(RxHttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    @Override
    public Mono<MessageRouterSubscribeResponse> get(MessageRouterSubscribeRequest request) {
        LOGGER.debug("Requesting new items from DMaaP MR: {}", request);
        return httpClient.call(buildGetHttpRequest(request))
                .map(this::buildGetResponse)
                .doOnError(ReadTimeoutException.class, e -> LOGGER.error("Timeout exception occurred when subscribe items from DMaaP MR", e))
                .onErrorResume(ReadTimeoutException.class, e -> createErrorResponse(ClientErrorReasons.TIMEOUT));
    }


    private @NotNull HttpRequest buildGetHttpRequest(MessageRouterSubscribeRequest request) {
        ImmutableHttpRequest.Builder requestBuilder = ImmutableHttpRequest.builder()
                .method(HttpMethod.GET)
                .url(buildSubscribeUrl(request))
                .diagnosticContext(request.diagnosticContext().withNewInvocationId());

        return Option.of(request.timeoutConfig())
                .map(timeoutConfig -> requestBuilder.timeout(timeoutConfig.getTimeout()).build())
                .getOrElse(requestBuilder::build);
    }

    private @NotNull MessageRouterSubscribeResponse buildGetResponse(HttpResponse httpResponse) {
        final ImmutableMessageRouterSubscribeResponse.Builder builder =
                ImmutableMessageRouterSubscribeResponse.builder();
        return httpResponse.successful()
                ? builder.items(getAsJsonElements(httpResponse)).build()
                : builder.failReason(extractFailReason(httpResponse)).build();
    }

    private List<JsonElement> getAsJsonElements(HttpResponse httpResponse){
        JsonArray bodyAsJsonArray = httpResponse
                .bodyAsJson(StandardCharsets.UTF_8, gson, JsonArray.class);

        return List.ofAll(bodyAsJsonArray).map(arrayElement -> JsonParser.parseString(arrayElement.getAsString()));
    }

    private String buildSubscribeUrl(MessageRouterSubscribeRequest request) {
        return String.format("%s/%s/%s", request.sourceDefinition().topicUrl(), request.consumerGroup(),
                request.consumerId());
    }

    private Mono<MessageRouterSubscribeResponse> createErrorResponse(ClientErrorReason clientErrorReason) {
        String failReason = ClientErrorReasonPresenter.present(clientErrorReason);
        return Mono.just(ImmutableMessageRouterSubscribeResponse.builder()
                .failReason(failReason)
                .build());
    }
}
