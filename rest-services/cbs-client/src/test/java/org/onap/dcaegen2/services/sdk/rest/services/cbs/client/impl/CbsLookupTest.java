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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.vavr.Function1;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsLookupTest {
    private Environment env = mock(Environment.class);
    private Function1<String, JsonArray> httpClient = mock(Function1.class);
    private final CbsLookup cut = new CbsLookup(env, httpClient);

    @Test
    void lookupShouldReturnValidConfiguration() {
        // given
        givenConsulResponse(parseResource("/consul_cbs_service.json").getAsJsonArray());

        // when
        final InetSocketAddress result = cut.lookup().block();

        // then
        assertThat(result.getHostString()).isEqualTo("config-binding-service");
        assertThat(result.getPort()).isEqualTo(10000);
    }

    private void givenConsulResponse(JsonArray jsonArray) {
        given(env.getRequired("CONSUL_HOST")).willReturn("consul.local");
        given(env.getRequired("CONFIG_BINDING_SERVICE")).willReturn("cbs-service");
        given(httpClient.apply("http://consul.local:8500/v1/catalog/service/cbs-service"))
                .willReturn(jsonArray);
    }

    @Test
    void lookupShouldReturnErrorWhenEnvironmentIsInvalid() {
        // given
        given(env.getRequired(anyString())).willThrow(IllegalStateException.class);

        // when
        final Mono<InetSocketAddress> result = cut.lookup();

        // then
        StepVerifier.create(result).verifyError(IllegalStateException.class);
    }

    @Test
    void lookupShouldReturnEmptyResultWhenServiceArrayIsEmpty() {
        // given
        givenConsulResponse(new JsonArray());

        // when
        final Mono<InetSocketAddress> result = cut.lookup();

        // then
        StepVerifier.create(result).verifyComplete();
    }

    private JsonElement parseResource(String resource) {
        return new JsonParser().parse(new InputStreamReader(CbsLookupTest.class.getResourceAsStream(resource)));
    }

}