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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.function.Consumer;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.AliveMessage;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.GsonAdaptersHealth;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.Health;
import org.onap.dcaegen2.services.sdk.standardization.moher.health.api.HealthProvider;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

public class HealthRoutes implements Consumer<HttpServerRoutes> {

    public static final String APPLICATION_JSON = "application/json";
    private final Gson gson;
    private final HealthProvider healthProvider;

    public HealthRoutes(Gson gson,
            HealthProvider healthProvider) {
        this.gson = gson;
        this.healthProvider = healthProvider;
    }

    public static HealthRoutes create(HealthProvider healthProvider) {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapterFactory(new GsonAdaptersHealth());
        return new HealthRoutes(gson.create(), healthProvider);
    }

    @Override
    public void accept(HttpServerRoutes routes) {
        routes.get("/health/ready", this::readinessCheck);
        routes.get("/health/alive", this::livenessCheck);
    }

    private Publisher<Void> readinessCheck(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return healthProvider.currentHealth()
                .flatMapMany(health ->
                        httpServerResponse.status(statusForHealth(health))
                                .header(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON)
                                .sendString(Mono.just(health).map(gson::toJson))
                );
    }

    private Publisher<Void> livenessCheck(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return httpServerResponse.status(HttpResponseStatus.OK)
                .header(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON)
                .sendString(Mono.just(AliveMessage.ALIVE_MESSAGE_JSON));
    }

    private HttpResponseStatus statusForHealth(Health health) {
        return health.healthy() ? HttpResponseStatus.OK : HttpResponseStatus.SERVICE_UNAVAILABLE;
    }
}
