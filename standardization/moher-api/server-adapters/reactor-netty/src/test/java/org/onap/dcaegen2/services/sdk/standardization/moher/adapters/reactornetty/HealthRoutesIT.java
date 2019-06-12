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

package org.onap.dcaegen2.services.sdk.standardization.moher.adapters.reactornetty;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClientFactory;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.AliveMessage;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.GsonAdaptersHealth;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.Health;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.HealthProvider;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

class HealthRoutesIT {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private final AtomicReference<Health> currentHealth = new AtomicReference<>();
    private final HealthRoutes sut = HealthRoutes.create(HealthProvider.fromSupplier(currentHealth::get));
    private final RxHttpClient rxHttpClient = RxHttpClientFactory.create();
    private final Gson gsonForDeserialization = new GsonBuilder()
            .registerTypeAdapterFactory(new GsonAdaptersHealth())
            .create();
    private DisposableServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        server = HttpServer.create().route(sut).bindNow();
        baseUrl = String.format("http://%s:%d", server.host(), server.port());
    }

    @AfterEach
    void tearDown() {
        server.disposeNow(TIMEOUT);
    }

    @Test
    void readinessProbeShouldReturnOkWhenHealthy() {
        // given
        final Health expectedHealth = Health.createHealthy("Ready to go");
        currentHealth.set(expectedHealth);
        final String url = baseUrl + "/health/ready";

        // when
        final HttpResponse response = rxHttpClient
                .call(ImmutableHttpRequest.builder().method(HttpMethod.GET).url(url).build())
                .block(TIMEOUT);

        // then
        assertThat(response.successful()).describedAs("response should be successful").isTrue();
        final Health actualHealth = response.bodyAsJson(StandardCharsets.UTF_8, gsonForDeserialization, Health.class);
        assertThat(actualHealth).describedAs("response body").isEqualTo(expectedHealth);
    }

    @Test
    void readinessProbeShouldReturnUnavailableWhenNotHealthy() {
        // given
        final Health expectedHealth = Health.createUnhealthy("Waiting for CBS update");
        currentHealth.set(expectedHealth);
        final String url = baseUrl + "/health/ready";

        // when
        final HttpResponse response = rxHttpClient
                .call(ImmutableHttpRequest.builder().method(HttpMethod.GET).url(url).build())
                .block(TIMEOUT);

        // then
        assertThat(response.statusCode()).describedAs("response status code")
                .isGreaterThanOrEqualTo(500)
                .isLessThan(600);
        assertThat(response.successful()).describedAs("response should not be successful").isFalse();
        final Health actualHealth = response.bodyAsJson(StandardCharsets.UTF_8, gsonForDeserialization, Health.class);
        assertThat(actualHealth).describedAs("response body").isEqualTo(expectedHealth);
    }

    @Test
    void livenessProbeShouldAlwaysReturnOk() {
        // given
        final String url = baseUrl + "/health/alive";

        // when
        final HttpResponse response = rxHttpClient
                .call(ImmutableHttpRequest.builder().method(HttpMethod.GET).url(url).build())
                .block(TIMEOUT);

        // then
        assertThat(response.successful()).describedAs("response should be successful").isTrue();
        assertThat(response.bodyAsString()).describedAs("response body").isEqualTo(AliveMessage.ALIVE_MESSAGE_JSON);
    }

}