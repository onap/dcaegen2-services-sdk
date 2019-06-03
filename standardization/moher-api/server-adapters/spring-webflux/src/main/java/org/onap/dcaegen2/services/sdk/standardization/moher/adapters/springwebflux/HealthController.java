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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.AliveMessage;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.GsonAdaptersHealth;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.Health;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.HealthProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/health", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class HealthController {
    private final Gson gson;
    private final HealthProvider healthProvider;

    public static HealthController create(HealthProvider healthProvider) {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapterFactory(new GsonAdaptersHealth());
        return new HealthController(gson.create(), healthProvider);
    }

    public HealthController(Gson gson, HealthProvider healthProvider) {
        this.gson = gson;
        this.healthProvider = healthProvider;
    }

    @GetMapping("/ready")
    public Mono<ServerResponse> readinessCheck() {
        return healthProvider.currentHealth()
                .flatMap(health -> responseStatusForHealth(health)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(Mono.just(health).map(gson::toJson), String.class));
    }

    @GetMapping("/alive")
    public Mono<String> livenessCheck() {
        return Mono.just(AliveMessage.ALIVE_MESSAGE_JSON);
    }

    private ServerResponse.BodyBuilder responseStatusForHealth(Health health) {
        return health.healthy()
                ? ServerResponse.ok()
                : ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
