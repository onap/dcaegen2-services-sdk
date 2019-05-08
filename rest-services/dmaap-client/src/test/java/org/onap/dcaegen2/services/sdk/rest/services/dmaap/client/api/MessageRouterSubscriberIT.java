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

import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendResource;

import com.google.gson.JsonElement;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.ImmutableMessageRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.MessageRouterSubscriberConfig;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since May 2019
 */
class MessageRouterSubscriberIT {
    private MessageRouterSubscriber sut = DmaapClientFactory.createMessageRouterSubscriber(MessageRouterSubscriberConfig.createDefault());
    private static DummyHttpServer server;
    private static MessageRouterSource sourceDefinition;

    @BeforeAll
    static void setUp() {
        server = DummyHttpServer.start(routes ->
                routes.get("/events/TOPIC/group1/consumer8", (req, resp) -> sendResource(resp, "/sample-mr-subscribe-response.json"))
        );
        sourceDefinition = ImmutableMessageRouterSource.builder()
                .name("the topic")
                .topicUrl(String.format("http://%s:%d/events/TOPIC", server.host(), server.port()))
                .build();
    }

    @Test
    void testStub() {
        final MessageRouterSubscribeRequest mrRequest = ImmutableMessageRouterSubscribeRequest.builder()
                .sourceDefinition(sourceDefinition)
                .consumerGroup("group1")
                .consumerId("consumer8")
                .build();

        final Flux<String> result = sut
                .getElements(mrRequest)
                .map(JsonElement::getAsString);

        StepVerifier.create(result)
                .expectNext("I", "like", "pizza")
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }
}
