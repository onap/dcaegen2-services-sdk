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

package org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Object responsible for returning application metrics.
 *
 * @since 1.2.0
 */
public interface Metrics {

    /**
     * Return all gathered metrics.
     *
     * Returns a Publisher that will emit a single string containing all
     * metrics with current values and finish afterwards.
     *
     * @since 1.2.0
     */
    Mono<String> collect();

    /**
     * Returns all gathered metrics.
     *
     * Returns a Publisher that will emit string containing all metrics with current values in intervals.
     *
     * @param interval interval in which Publisher should return metrics
     * @since 1.2.0
     */
    Flux<String> collect(Duration interval);
}