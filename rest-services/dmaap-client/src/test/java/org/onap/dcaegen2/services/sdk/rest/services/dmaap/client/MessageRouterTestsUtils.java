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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.List;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterPublisher;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import reactor.core.publisher.Flux;


public final class MessageRouterTestsUtils {
    private static final JsonParser parser = new JsonParser();
    private MessageRouterTestsUtils() {}

    public static MessageRouterPublishRequest createPublishRequest(String topicUrl){
        return createPublishRequest(topicUrl, ContentType.APPLICATION_JSON);
    }

    public static MessageRouterPublishRequest createPublishRequest(String topicUrl, ContentType contentType){
        MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
                .name("the topic")
                .topicUrl(topicUrl)
                .build();

        return ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .contentType(contentType)
                .build();
    }

    public static MessageRouterSubscribeRequest createMRSubscribeRequest(String topicUrl,
            String consumerGroup, String consumerId) {
        ImmutableMessageRouterSource sourceDefinition = ImmutableMessageRouterSource.builder()
                .name("the topic")
                .topicUrl(topicUrl)
                .build();

        return ImmutableMessageRouterSubscribeRequest
                .builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup(consumerGroup)
                .consumerId(consumerId)
                .build();
    }

    public static List<JsonElement> getAsJsonElements(List<String> messages){
        return messages.map(parser::parse);
    }

    public static List<JsonObject> getAsJsonObjects(List<String> messages){
        return getAsJsonElements(messages).map(JsonElement::getAsJsonObject);
    }

    public static List<JsonPrimitive> getAsJsonPrimitives(List<String> messages){
        return getAsJsonElements(messages).map(JsonElement::getAsJsonPrimitive);
    }

    public static JsonObject getAsJsonObject(String item){
        return new Gson().fromJson(item, JsonObject.class);
    }

    public static Flux<JsonElement> plainBatch(List<String> messages){
        return Flux.fromIterable(getAsJsonElements(messages));
    }

    public static Flux<JsonObject> jsonBatch(List<String> messages){
        return plainBatch(messages).map(JsonElement::getAsJsonObject);
    }

    public static MessageRouterSubscribeResponse errorSubscribeResponse(String failReasonFormat, Object... formatArgs){
        return ImmutableMessageRouterSubscribeResponse
                .builder()
                .failReason(String.format(failReasonFormat, formatArgs))
                .build();
    }

    public static MessageRouterSubscribeResponse successSubscribeResponse(List<JsonElement> items){
        return ImmutableMessageRouterSubscribeResponse
                .builder()
                .items(items)
                .build();
    }

    public static MessageRouterPublishResponse errorPublishResponse(String failReasonFormat, Object... formatArgs){
        return ImmutableMessageRouterPublishResponse
                .builder()
                .failReason(String.format(failReasonFormat, formatArgs))
                .build();
    }

    public static MessageRouterPublishResponse successPublishResponse(List<JsonElement> items){
        return ImmutableMessageRouterPublishResponse
                .builder()
                .items(items)
                .build();
    }

    public static void registerTopic(MessageRouterPublisher publisher, MessageRouterPublishRequest publishRequest,
            MessageRouterSubscriber subscriber, MessageRouterSubscribeRequest subscribeRequest) {
        final List<String> sampleJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final Flux<JsonObject> jsonMessageBatch = MessageRouterTestsUtils.jsonBatch(sampleJsonMessages);

        publisher.put(publishRequest, jsonMessageBatch).blockLast();
        subscriber.get(subscribeRequest).block();
    }
}
