/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2022 Nokia. All rights reserved.
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

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api.Metrics;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api.MetricsFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class MetricsControllerTest {

    private final PrometheusMeterRegistry defaultRegistry = MetricsFactory.createDefaultRegistry();
    private final Metrics metrics = MetricsFactory.createMetrics(defaultRegistry);
    private final MetricsController sut = new MetricsController(metrics);
    private final WebTestClient client = WebTestClient.bindToController(sut).build();

    @Test
    void prometheusMetrics() {
        client.get().uri("/metrics").accept(MediaType.ALL).exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("system_cpu").contains("jvm_classes"));
    }
}
