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

import java.util.function.Consumer;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api.Metrics;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

public class MetricsRoutes implements Consumer<HttpServerRoutes> {

    private final Metrics metrics;

    public MetricsRoutes(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void accept(HttpServerRoutes routes) {
        routes.get("/metrics", this::prometheusMetrics);
    }

    private Publisher<Void> prometheusMetrics(
            HttpServerRequest httpServerRequest,
            HttpServerResponse httpServerResponse) {
        return httpServerResponse.sendString(metrics.collect());
    }

}
