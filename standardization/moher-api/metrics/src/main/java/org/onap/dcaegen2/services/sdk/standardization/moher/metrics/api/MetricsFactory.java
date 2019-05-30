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

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.impl.MetricsImpl;

/**
 * Factory for creating {@link Metrics} object.
 *
 * <p>Typical usage:</p>
 *
 * <pre>
 *  // create registry
 *  PrometheusMeterRegistry registry = MetricsFactory.createDefaultRegistry();
 *  // create metrics
 *  Metrics metrics = MetricsFactory.createMetrics(registry);
 * </pre>
 *
 * @since 1.2.0
 */
public class MetricsFactory {

    /**
     * Method for creating default Prometheus registry.
     * <p>
     * Client is expected to populate registry with custom metrics.
     * Exact procedure can be found at Micrometer project site.
     * </p>
     * <p>
     * It is recommended to search through available implementations of {@link MeterBinder}
     * as there exists a plenty of them for most common use cases.
     * </p>
     *
     * @since 1.2.0
     */
    public static PrometheusMeterRegistry createDefaultRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * Method for creating {@link Metrics} with configured Prometheus registry.
     * <p>
     * Returned object will add to registry few default JVM (memory and threads usage, garbage collection)
     * and system metrics (CPU usage). For exact list of metrics added, please refer to implementation.
     *
     * @param registry Prometheus registry to be used
     * @since 1.2.0
     */
    public static Metrics createMetrics(PrometheusMeterRegistry registry) {
        MetricsImpl metrics = new MetricsImpl(registry);
        metrics.configureDefaultMetrics();
        return metrics;
    }
}
