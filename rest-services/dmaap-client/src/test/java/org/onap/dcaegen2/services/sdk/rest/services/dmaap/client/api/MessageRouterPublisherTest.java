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

import static org.mockito.Mockito.mock;

import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterPublishRequest;
import reactor.core.publisher.Flux;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
@Disabled
class MessageRouterPublisherTest {

    private final MessageRouterPublisher cut = mock(MessageRouterPublisher.class);
    private final MessageRouterSink sinkDefinition = mock(MessageRouterSink.class);
    private final MessageRouterPublishRequest request = ImmutableMessageRouterPublishRequest.builder()
            .sinkDefinition(sinkDefinition).build();

    @Test
    void apiShouldBeUsableWithTransform() {
        Flux.just(1, 2, 3)
                .map(JsonPrimitive::new)
                .transform(input -> cut.put(request, input));
    }

    @Test
    void apiShouldBeUsableWithSingleCall() {
        final Flux<JsonPrimitive> input = Flux.just(1, 2, 3).map(JsonPrimitive::new);
        cut.put(request, input);
    }
}