/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.JsonObject;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.KafkaSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.KafkaSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions.HttpException;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.EnvironmentParsingException;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParsingException;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.DataStreams;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParsers;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableCbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.model.streams.StreamType.KAFKA;
import static org.onap.dcaegen2.services.sdk.model.streams.StreamType.MESSAGE_ROUTER;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendResource;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamPredicates.streamOfType;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsClientImplIT {

    private static final String SAMPLE_CONFIG = "/sample_service_config.json";
    private static final String SAMPLE_ALL = "/sample_all.json";
    private static final String SAMPLE_KEY = "/sample_key.json";
    private static final String SAMPLE_CONFIG_KEY = "keystore.path";
    private static final String EXPECTED_CONFIG_VALUE_FROM_CBS = "/var/run/security/keystore.p12";
    private static final String CONFIG_MAP_FILE_PATH = "src/test/resources/application_config.yaml";
    private static CbsClientConfiguration sampleConfigurationCbsSource;
    private static CbsClientConfiguration sampleConfigurationFileSource;
    private static DummyHttpServer server;


    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables();

    @BeforeAll
    static void setUp() {
        server = DummyHttpServer.start(routes ->
                routes.get("/service_component/dcae-component", (req, resp) -> sendResource(resp, SAMPLE_CONFIG))
                        .get("/service_component_all/dcae-component", (req, resp) -> sendResource(resp, SAMPLE_ALL))
                        .get("/sampleKey/dcae-component", (req, resp) -> sendResource(resp, SAMPLE_KEY))
        );
        ImmutableCbsClientConfiguration.Builder configBuilder = getConfigBuilder();
        sampleConfigurationCbsSource = configBuilder.build();
        sampleConfigurationFileSource = configBuilder.configMapFilePath(CONFIG_MAP_FILE_PATH).build();
    }

    @AfterAll
    static void tearDown() {
        server.close();
    }

    @Test
    void testCbsClientWithUpdatesCall() {
        // given
        envs.set("AAF_USER", "admin");
        envs.set("AAF_PASSWORD", "admin_secret");
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleConfigurationCbsSource);
        final CbsRequest request = CbsRequests.getConfiguration(RequestDiagnosticContext.create());
        final Duration period = Duration.ofMillis(10);

        // when
        final Flux<JsonObject> result = sut
                .flatMapMany(cbsClient -> cbsClient.updates(request, Duration.ZERO, period));

        // then
        final Duration timeToCollectItemsFor = period.multipliedBy(50);
        StepVerifier.create(result.take(timeToCollectItemsFor).map(this::sampleConfigValue))
                .expectNext(EXPECTED_CONFIG_VALUE_FROM_CBS)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCbsClientWithConfigRetrievedFromFileMissingEnv() {
        // given
        envs.set("AAF_USER", "");
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleConfigurationFileSource);
        final CbsRequest request = CbsRequests.getConfiguration(RequestDiagnosticContext.create());

        // when
        final Mono<JsonObject> result = sut.flatMap(cbsClient -> cbsClient.get(request));

        // then
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(EnvironmentParsingException.class);
                    assertThat(ex).hasMessageContaining("Cannot read AAF_USER from environment.");
                })
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCbsClientWithConfigRetrievedFromFile() {
        // given
        envs.set("AAF_USER", "admin");
        envs.set("AAF_PASSWORD", "admin_secret");
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleConfigurationFileSource);
        final CbsRequest request = CbsRequests.getConfiguration(RequestDiagnosticContext.create());

        // when
        final Mono<JsonObject> result = sut.flatMap(cbsClient -> cbsClient.get(request));

        // then
        StepVerifier.create(result.map(this::sampleConfigValue))
                .expectNext(EXPECTED_CONFIG_VALUE_FROM_CBS)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }


    @NotNull
    private static ImmutableCbsClientConfiguration.Builder getConfigBuilder() {
        return ImmutableCbsClientConfiguration.builder()
                .appName("dcae-component");
    }

    private String sampleConfigValue(JsonObject obj) {
        return obj.get(SAMPLE_CONFIG_KEY).getAsString();
    }

}
