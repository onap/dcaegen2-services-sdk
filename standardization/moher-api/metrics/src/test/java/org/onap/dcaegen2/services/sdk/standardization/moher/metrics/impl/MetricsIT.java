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

import io.micrometer.core.instrument.Counter;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api.Metrics;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api.MetricsFactory;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsIT {

    private static final String COUNTER_NAME = "Duplicates_Amount";
    private static final String GAUGE_NAME = "gauge_Duplicates_Amount";
    private static final String TIMER_NAME = "Duplicates_Amount_timer";
    private static final String SUMMARY_NAME = "Duplicates_Amount_summary";
    private static final Duration INTERVAL = Duration.ofMillis(100);

    private PrometheusMeterRegistry defaultRegistry;
    private Metrics cut;

    @BeforeEach
    void setup() {
        defaultRegistry = MetricsFactory.createDefaultRegistry();
        cut = MetricsFactory.createMetrics(defaultRegistry);
    }

    @Test
    void metrics_givenDefaultRegistry_shouldReturnCreatedMeters() {

        Counter counter = defaultRegistry.counter(COUNTER_NAME);
        defaultRegistry.gauge(GAUGE_NAME, counter.count());
        defaultRegistry.timer(TIMER_NAME);
        defaultRegistry.summary(SUMMARY_NAME);

        StepVerifier.create(cut.collect())
                .expectNextMatches((collectedMetrics) ->
                        collectedMetrics.contains(COUNTER_NAME) &&
                                collectedMetrics.contains(GAUGE_NAME) &&
                                collectedMetrics.contains(TIMER_NAME) &&
                                collectedMetrics.contains(SUMMARY_NAME)
                )
                .verifyComplete();
    }

    @Test
    void metrics_givenDefaultRegistry_shouldReturnCreatedMetersInIntervals() {

        Counter counter = defaultRegistry.counter(COUNTER_NAME);

        StepVerifier.create(
                cut.collect(INTERVAL).take(2)
        )
                .consumeNextWith((collectedMetrics) -> {
                    assertMetricsContain(collectedMetrics, COUNTER_NAME);
                    counter.increment();
                })
                .thenAwait(INTERVAL)
                .expectNextMatches((collectedMetrics) ->
                        collectedMetrics.contains(COUNTER_NAME + "_total 1.0"))
                .verifyComplete();
    }

    @Test
    void metrics_shouldIncludeSomeDefaultMetrics() {
        StepVerifier.create(cut.collect())
                .consumeNextWith((collectedMetrics) -> {
                    assertMetricsContain(collectedMetrics, "jvm_threads");
                    assertMetricsContain(collectedMetrics, "jvm_memory");
                    assertMetricsContain(collectedMetrics, "jvm_classes");
                    assertMetricsContain(collectedMetrics, "jvm_gc");
                    assertMetricsContain(collectedMetrics, "system_cpu");
                })
                .verifyComplete();
    }

    private void assertMetricsContain(final String collectedMetrics, final String metricName) {
        assertThat(collectedMetrics.contains(metricName))
                .describedAs(String.format("Expected metric: %s", metricName))
                .isTrue();
    }
}
