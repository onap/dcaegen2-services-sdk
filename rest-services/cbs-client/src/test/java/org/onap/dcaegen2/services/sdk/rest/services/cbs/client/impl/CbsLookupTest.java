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


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.ServiceLookupException;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableEnvProperties;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsLookupTest {

    private final EnvProperties env = ImmutableEnvProperties.builder()
            .cbsName("cbs-service")
            .consulHost("consul.local")
            .consulPort(8050)
            .appName("whatever").build();
    private final CloudHttpClient httpClient = mock(CloudHttpClient.class);
    private final CbsLookup cut = new CbsLookup(httpClient);

    @Test
    void lookupShouldReturnValidConfiguration() {
        // given
        givenConsulResponse(parseResource("/consul_cbs_service.json").getAsJsonArray());

        // when
        final InetSocketAddress result = cut.lookup(env).block();

        // then
        assertThat(result.getHostString()).isEqualTo("config-binding-service");
        assertThat(result.getPort()).isEqualTo(10000);
    }

    @Test
    void lookupShouldEmitErrorWhenServiceArrayIsEmpty() {
        // given
        givenConsulResponse(new JsonArray());

        // when
        final Mono<InetSocketAddress> result = cut.lookup(env);

        // then
        StepVerifier.create(result).verifyError(ServiceLookupException.class);
    }

    private JsonElement parseResource(String resource) {
        return new JsonParser().parse(new InputStreamReader(CbsLookupTest.class.getResourceAsStream(resource)));
    }

    private void givenConsulResponse(JsonArray jsonArray) {
        final String url = "http://"
                + env.consulHost()
                + ":"
                + env.consulPort()
                + "/v1/catalog/service/"
                + env.cbsName();
        given(httpClient.get(url, JsonArray.class))
                .willReturn(Mono.just(jsonArray));
    }

}