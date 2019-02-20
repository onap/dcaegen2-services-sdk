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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api;


import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class PublisherTest {
    @Test
    void correctMessageRouterPublisher_shouldSucceed() throws MalformedURLException {
        ImmutableAafCredentials aafCredentials = ImmutableAafCredentials.builder()
                .aafPassword("dummy_passwd")
                .aafUsername("dummy_user")
                .build();
        ImmutableDmaapPublisher dmaapPublisherInfo = ImmutableDmaapPublisher.builder()
                .location("somwhere")
                .clientRole("com.dcae.member")
                .clientId("1122335454")
                .topicURL(new URL("https://we-are-message-router.us:3905/events/some-topic"))
                .build();
        Stream stream = ImmutablePublisher.builder()
                .streamType(Stream.StreamType.MESSAGE_ROUTER)
                .aafCredentials(aafCredentials)
                .dmaapInfo(dmaapPublisherInfo)
                .build();
        assertThat(stream).hasFieldOrProperty("aafCredentials");
        assertThat(stream).isInstanceOf(Publisher.class);
    }

    @Test
    void correctDataRouterPublisher_shouldSucceed() throws MalformedURLException {
        ImmutableDataRouterCredentials credentials = ImmutableDataRouterCredentials.builder()
                .password("dummy_passwd")
                .username("dummy_user")
                .build();
        ImmutableDmaapPublisher dmaapPublisherInfo = ImmutableDmaapPublisher.builder()
                .location("somwhere")
                .deliveryURL(new URL("https://my-subscriber-app.dcae:8080/target-path"))
                .subscriberId("787871234")
                .build();
        Stream stream = ImmutablePublisher.builder()
                .streamType(Stream.StreamType.MESSAGE_ROUTER)
                .dataRouterCredentials(credentials)
                .dmaapInfo(dmaapPublisherInfo)
                .build();
        assertThat(stream).isInstanceOf(Publisher.class);
    }

    @Test
    void incorrectDataRouterPublisher_shouldThrowException() throws MalformedURLException {
        ImmutableDataRouterCredentials credentials = ImmutableDataRouterCredentials.builder()
                .password("dummy_passwd")
                .username("dummy_user")
                .build();
        ImmutableDmaapPublisher dmaapPublisherInfo = ImmutableDmaapPublisher.builder()
                .location("somwhere")
                .deliveryURL(new URL("https://my-subscriber-app.dcae:8080/target-path"))
                .subscriberId("787871234")
                .build();
        ImmutablePublisher.Builder publisherBuilder = ImmutablePublisher.builder()
                .streamType(Stream.StreamType.MESSAGE_ROUTER)
                .dataRouterCredentials(credentials);
        assertThatThrownBy(() -> publisherBuilder.build())
                .hasMessage("Cannot build Publisher, some of required attributes are not set [dmaapInfo]");
    }
}