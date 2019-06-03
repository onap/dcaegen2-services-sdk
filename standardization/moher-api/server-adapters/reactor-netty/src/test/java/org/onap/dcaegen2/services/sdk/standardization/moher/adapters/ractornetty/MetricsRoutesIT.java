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

package org.onap.dcaegen2.services.sdk.standardization.moher.adapters.ractornetty;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClientFactory;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api.Metrics;
import org.onap.dcaegen2.services.sdk.standardization.moher.metrics.api.MetricsFactory;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

class MetricsRoutesIT {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private final Metrics metrics = MetricsFactory.createMetrics(MetricsFactory.createDefaultRegistry());
    private final MetricsRoutes sut = new MetricsRoutes(metrics);
    private final RxHttpClient rxHttpClient = RxHttpClientFactory.create();
    private DisposableServer server;

    @BeforeEach
    void setUp() {
        server = HttpServer.create().route(sut).bindNow();
    }

    @AfterEach
    void tearDown() {
        server.disposeNow(TIMEOUT);
    }

    @Test
    void prometheusMetrics() {
        // given
        final String url = String.format("http://%s:%d/metrics", server.host(), server.port());

        // when
        final HttpResponse response = rxHttpClient
                .call(ImmutableHttpRequest.builder().method(HttpMethod.GET).url(url).build())
                .block(TIMEOUT);

        // then
        assertThat(response.successful()).describedAs("response should be successfull").isTrue();
        assertThat(response.bodyAsString()).describedAs("response body").contains("system_cpu", "jvm_classes");
    }
}