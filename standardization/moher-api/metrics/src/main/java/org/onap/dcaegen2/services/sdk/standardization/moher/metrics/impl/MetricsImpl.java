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

package org.onap.dcaegen2.services.sdk.standardization.moher.metrics.impl;


import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api.Metrics;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class MetricsImpl implements Metrics {
    private final PrometheusMeterRegistry registry;

    public MetricsImpl(PrometheusMeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Mono<String> collect() {
        return Mono.just(registry.scrape());
    }

    @Override
    public Flux<String> collect(Duration interval) {
        return Flux.interval(interval)
                .map((l) -> registry.scrape());
    }
}
