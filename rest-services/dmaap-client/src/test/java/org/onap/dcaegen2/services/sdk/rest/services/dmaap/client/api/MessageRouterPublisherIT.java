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

import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendString;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.List;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterPublisherConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */
class MessageRouterPublisherIT {
    private MessageRouterPublisher sut = DmaapClientFactory.createMessageRouterPublisher(MessageRouterPublisherConfig.createDefault());
    private static DummyHttpServer server;
    private static MessageRouterSink sinkDefinition;

    @BeforeAll
    static void setUp() {
        server = DummyHttpServer.start(routes ->
                routes.post("/events/TOPIC", (req, resp) -> sendString(resp, Mono.just("TODO")))
        );
        sinkDefinition = ImmutableMessageRouterSink.builder()
                .name("the topic")
                .topicUrl(String.format("http://%s:%d/events/TOPIC", server.host(), server.port()))
                .build();
    }

    @Test
    void testStub() {
        final MessageRouterPublishRequest mrRequest = ImmutableMessageRouterPublishRequest.builder()
                .sinkDefinition(sinkDefinition)
                .build();

        final Flux<MessageRouterPublishResponse> result = sut
                .put(mrRequest, Flux.just("ala", "ma", "kota").map(JsonPrimitive::new));

        final List<JsonElement> expectedItems = List.of("ala", "ma", "kota").map(JsonPrimitive::new);
        StepVerifier.create(result)
                .expectNext(ImmutableMessageRouterPublishResponse.builder().items(expectedItems).build())
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }
}
