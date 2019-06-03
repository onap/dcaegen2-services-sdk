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

package org.onap.dcaegen2.services.sdk.standardization.moher.adapters.springwebflux;


import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.AliveMessage;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.GsonAdaptersHealth;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.Health;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.HealthProvider;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.ImmutableHealth;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

class HealthControllerIT {

    private final AtomicReference<Health> currentHealth = new AtomicReference<>();
    private final HealthProvider healthProvider = () -> Mono.fromCallable(currentHealth::get);
    private final HealthController sut = HealthController.create(healthProvider);
    private final WebTestClient client = WebTestClient.bindToController(sut).build();
    private final Gson gsonForDeserialization = new GsonBuilder().registerTypeAdapterFactory(new GsonAdaptersHealth())
            .create();

    @Test
    void readinessProbeShouldReturnOkWhenHealthy() {
        final Health expectedHealth = Health.createHealthy("Ready to go");
        currentHealth.set(expectedHealth);

        client.get().uri("/health/ready").accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(body -> {
            final Health actualHealth = gsonForDeserialization.fromJson(body, Health.class);
            assertThat(actualHealth).isEqualTo(expectedHealth);
        });
    }

    @Test
    void readinessProbeShouldReturnUnavailableWhenNotHealthy() {
        final Health expectedHealth = Health.createUnhealthy("Waiting for CBS update");
        currentHealth.set(expectedHealth);

        client.get().uri("/health/ready").accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class).value(body -> {
            final Health actualHealth = gsonForDeserialization.fromJson(body, Health.class);
            assertThat(actualHealth).isEqualTo(expectedHealth);
        });
    }

    @Test
    void livenessProbeShouldAlwaysReturnOk() {
        client.get().uri("/health/alive").accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(body -> {
            assertThat(body).isEqualTo(AliveMessage.ALIVE_MESSAGE_JSON);
        });
    }

}