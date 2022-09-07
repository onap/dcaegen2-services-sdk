/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
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


import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableCbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableTrustStoreKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeysStore;
import reactor.core.publisher.Mono;

class CbsClientFactoryTest {

    @Test
    void shouldAllowMultipleSubscriptions() throws URISyntaxException {
        //given
        ImmutableCbsClientConfiguration sampleConfiguration = ImmutableCbsClientConfiguration.builder()
            .appName("dcae-component")
            .build();

        //when
        Mono<CbsClient> cbsClient = CbsClientFactory.createCbsClient(sampleConfiguration);

        //then
        assertThatCode(() -> {
            CbsClient client1 = cbsClient.block();
            CbsClient client2 = cbsClient.block();
            assertThat(client1).isNotNull();
            assertThat(client2).isNotNull();
        }).doesNotThrowAnyException();
    }
}
