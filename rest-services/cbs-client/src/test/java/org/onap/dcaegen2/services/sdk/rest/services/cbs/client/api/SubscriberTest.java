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

class SubscriberTest {

    @Test
    void correctDataRouterSubscriber_shouldSucceed() throws MalformedURLException {
        ImmutableDataRouterCredentials routerCredentials = ImmutableDataRouterCredentials.builder()
                .password("dummy_passwd")
                .username("dummy_user")
                .build();
        ImmutableDmaapSubscriber dmaapPublisherInfo = ImmutableDmaapSubscriber.builder()
                .location("somwhere")
                .deliveryURL(new URL("https://my-subscriber-app.dcae:8080/target-path"))
                .subscriberId("1414174149")
                .build();
        Stream stream = ImmutableSubscriber.builder()
                .streamType(Stream.StreamType.DATA_ROUTER)
                .dataRouterCredentials(routerCredentials)
                .dmaapInfo(dmaapPublisherInfo)
                .build();
        assertThat(stream).hasFieldOrProperty("dataRouterCredentials");
        assertThat(stream).isInstanceOf(Subscriber.class);
    }

    @Test
    void incorrectDataRouterSubscriber_shouldNotSucceed() throws MalformedURLException {
        ImmutableDataRouterCredentials routerCredentials = ImmutableDataRouterCredentials.builder()
                .password("dummy_passwd")
                .username("dummy_user")
                .build();
        ImmutableSubscriber.Builder streamBuilder = ImmutableSubscriber.builder()
                .streamType(Stream.StreamType.DATA_ROUTER)
                .dataRouterCredentials(routerCredentials);

        assertThatThrownBy(() -> streamBuilder.build())
                .hasMessage("Cannot build Subscriber, some of required attributes are not set [dmaapInfo]");
    }

}