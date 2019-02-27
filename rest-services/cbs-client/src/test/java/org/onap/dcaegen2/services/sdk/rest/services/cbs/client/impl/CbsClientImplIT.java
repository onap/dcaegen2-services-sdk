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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.DummyHttpServer.sendResource;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.DummyHttpServer.sendString;

import com.google.gson.JsonObject;
import io.vavr.collection.Stream;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.ImmutableEnvProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsClientImplIT {

    private static final String CONSUL_RESPONSE = "[\n"
            + "    {\n"
            + "        \"ServiceAddress\": \"HOST\",\n"
            + "        \"ServiceName\": \"the_cbs\",\n"
            + "        \"ServicePort\": PORT\n"
            + "    }\n"
            + "]\n";
    private static final String SAMPLE_CONFIG = "/sample_config.json";
    private static final String SAMPLE_CONFIG_KEY = "keystore.path";
    private static final String EXPECTED_CONFIG_VALUE = "/var/run/security/keystore.p12";
    private static DummyHttpServer server;

    @BeforeAll
    static void setUp() {
        server = DummyHttpServer.start(routes ->
                routes.get("/v1/catalog/service/the_cbs", (req, resp) -> sendString(resp, lazyConsulResponse()))
                        .get("/service_component/dcae-component", (req, resp) -> sendResource(resp, SAMPLE_CONFIG)));
    }

    @AfterAll
    static void tearDown() {
        server.close();
    }

    @Test
    void testCbsClientWithSingleCall() {
        // given
        final EnvProperties env = ImmutableEnvProperties.builder()
                .appName("dcae-component")
                .cbsName("the_cbs")
                .consulHost(server.host())
                .consulPort(server.port())
                .build();
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(env);
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final Mono<JsonObject> result = sut.flatMap(cbsClient -> cbsClient.get(diagnosticContext));

        // then
        StepVerifier.create(result.map(obj -> obj.get(SAMPLE_CONFIG_KEY).getAsString()))
                .expectNext(EXPECTED_CONFIG_VALUE)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCbsClientWithPeriodicCall() {
        // given
        final EnvProperties env = ImmutableEnvProperties.builder()
                .appName("dcae-component")
                .cbsName("the_cbs")
                .consulHost(server.host())
                .consulPort(server.port())
                .build();
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(env);
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final Flux<JsonObject> result = sut.flatMapMany(cbsClient -> cbsClient.get(diagnosticContext, Duration.ZERO, Duration.ofMillis(10)));

        // then
        final int itemsToTake = 5;
        StepVerifier.create(result.take(itemsToTake).map(obj -> obj.get(SAMPLE_CONFIG_KEY).getAsString()))
                .expectNextSequence(Stream.of(EXPECTED_CONFIG_VALUE).cycle(itemsToTake))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    private static Mono<String> lazyConsulResponse() {
        return Mono.just(CONSUL_RESPONSE)
                .map(CbsClientImplIT::processConsulResponseTemplate);
    }

    private static String processConsulResponseTemplate(String resp) {
        return resp.replaceAll("HOST", server.host())
                .replaceAll("PORT", Integer.toString(server.port()));
    }
}
