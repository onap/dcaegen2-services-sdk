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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.List;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import reactor.core.publisher.Flux;

class MessageRouterTestsUtils {
    private static final JsonParser parser = new JsonParser();
    private MessageRouterTestsUtils() {}

    static MessageRouterPublishRequest createPublishRequest(String topicUrl){
        MessageRouterSink sinkDefinition = ImmutableMessageRouterSink.builder()
                .name("the topic")
                .topicUrl(topicUrl)
                .build();

        return ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .build();
    }

    static MessageRouterSubscribeRequest createMRSubscribeRequest(String topicUrl,
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

    static List<JsonElement> getAsJsonElements(List<String> messages){
        return messages.map(parser::parse);
    }

    static JsonObject getAsJsonObject(String item){
        return new Gson().fromJson(item, JsonObject.class);
    }

    static Flux<JsonObject> jsonBatch(List<String> messages){
        return Flux.fromIterable(messages).map(parser::parse).map(JsonElement::getAsJsonObject);
    }

    static Flux<JsonPrimitive> plainBatch(List<String> messages){
        return Flux.fromIterable(messages).map(JsonPrimitive::new);
    }

    static void registerTopic(MessageRouterPublisher publisher, MessageRouterPublishRequest publishRequest,
            MessageRouterSubscriber subscriber, MessageRouterSubscribeRequest subscribeRequest) {
        final List<String> sampleJsonMessages = List.of("{\"message\":\"message1\"}",
                "{\"differentMessage\":\"message2\"}");
        final Flux<JsonObject> jsonMessageBatch = MessageRouterTestsUtils.jsonBatch(sampleJsonMessages);

        publisher.put(publishRequest, jsonMessageBatch).blockLast();
        subscriber.get(subscribeRequest).block();
    }
}
