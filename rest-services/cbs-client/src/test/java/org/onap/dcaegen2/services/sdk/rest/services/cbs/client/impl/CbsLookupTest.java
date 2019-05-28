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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableEnvProperties;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsLookupTest {

    private static final String cbsAddress = "cbs-service";
    private static final int cbsPort = 10000;
    private final EnvProperties env = ImmutableEnvProperties.builder()
            .cbsHostname(cbsAddress)
            .cbsPort(cbsPort)
            .appName("whatever").build();
    private final CbsLookup cut = new CbsLookup();

    @Test
    void lookupShouldReturnValidSocketAddressFromEnvironment() {
        // when
        final InetSocketAddress result = cut.lookup(env).block();

        // then
        assertThat(result.getHostString()).isEqualTo(cbsAddress);
        assertThat(result.getPort()).isEqualTo(cbsPort);
    }
}