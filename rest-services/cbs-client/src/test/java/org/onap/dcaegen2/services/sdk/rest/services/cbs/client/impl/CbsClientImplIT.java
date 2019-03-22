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

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendResource;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer.sendString;

import com.google.gson.JsonObject;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.test.DummyHttpServer;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.DataStreams;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParsers;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableEnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.KafkaSink;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.KafkaSource;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
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
    private static EnvProperties sampleEnvironment;
    private static DummyHttpServer server;

    @BeforeAll
    static void setUp() {
        server = DummyHttpServer.start(routes ->
                routes.get("/v1/catalog/service/the_cbs", (req, resp) -> sendString(resp, lazyConsulResponse()))
                        .get("/service_component/dcae-component", (req, resp) -> sendResource(resp, SAMPLE_CONFIG)));
        sampleEnvironment = ImmutableEnvProperties.builder()
                .appName("dcae-component")
                .cbsName("the_cbs")
                .consulHost(server.host())
                .consulPort(server.port())
                .build();
    }

    @AfterAll
    static void tearDown() {
        server.close();
    }

    @Test
    void testCbsClientWithSingleCall() {
        // given
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleEnvironment);
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final Mono<JsonObject> result = sut.flatMap(cbsClient -> cbsClient.get(diagnosticContext));

        // then
        StepVerifier.create(result.map(this::sampleConfigValue))
                .expectNext(EXPECTED_CONFIG_VALUE)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCbsClientWithPeriodicCall() {
        // given
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleEnvironment);
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final Flux<JsonObject> result = sut
                .flatMapMany(cbsClient -> cbsClient.get(diagnosticContext, Duration.ZERO, Duration.ofMillis(10)));

        // then
        final int itemsToTake = 5;
        StepVerifier.create(result.take(itemsToTake).map(this::sampleConfigValue))
                .expectNextSequence(Stream.of(EXPECTED_CONFIG_VALUE).cycle(itemsToTake))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCbsClientWithUpdatesCall() {
        // given
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleEnvironment);
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();
        final Duration period = Duration.ofMillis(10);

        // when
        final Flux<JsonObject> result = sut
                .flatMapMany(cbsClient -> cbsClient.updates(diagnosticContext, Duration.ZERO, period));

        // then
        final Duration timeToCollectItemsFor = period.multipliedBy(50);
        StepVerifier.create(result.take(timeToCollectItemsFor).map(this::sampleConfigValue))
                .expectNext(EXPECTED_CONFIG_VALUE)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCbsClientWithStreamsParsing() {
        // given
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleEnvironment);
        final StreamFromGsonParser<KafkaSink> kafkaSinkParser = StreamFromGsonParsers.kafkaSinkParser();
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final Mono<KafkaSink> result = sut.flatMap(cbsClient -> cbsClient.get(diagnosticContext))
                .map(json ->
                        DataStreams.namedSinks(json).map(kafkaSinkParser::unsafeParse).head()
                );

        // then
        StepVerifier.create(result)
                .consumeNextWith(kafkaSink -> {
                    assertThat(kafkaSink.name()).isEqualTo("perf3gpp");
                    assertThat(kafkaSink.bootstrapServers()).isEqualTo("dmaap-mr-kafka:6060");
                    assertThat(kafkaSink.topicName()).isEqualTo("HVVES_PERF3GPP");
                })
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCbsClientWithStreamsParsingUsingSwitch() {
        // given
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleEnvironment);
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();
        // TODO: Use these parsers below
        final StreamFromGsonParser<KafkaSink> kafkaSinkParser = StreamFromGsonParsers.kafkaSinkParser();
        final StreamFromGsonParser<MessageRouterSink> mrSinkParser = StreamFromGsonParsers.messageRouterSinkParser();

        // when
        final Mono<Void> result = sut.flatMap(cbsClient -> cbsClient.get(diagnosticContext))
                .map(json -> {
                    final Map<String, Stream<RawDataStream<JsonObject>>> sinks = DataStreams.namedSinks(json)
                            .groupBy(RawDataStream::type);

                    final Stream<KafkaSink> allKafkaSinks = sinks.getOrElse("kafka", Stream.empty())
                            .map(kafkaSinkParser::unsafeParse);
                    final Stream<MessageRouterSink> allMrSinks = sinks.getOrElse("message_router", Stream.empty())
                            .map(mrSinkParser::unsafeParse);

                    assertThat(allKafkaSinks.size())
                            .describedAs("Number of kafka sinks")
                            .isEqualTo(2);
                    assertThat(allMrSinks.size())
                            .describedAs("Number of DMAAP-MR sinks")
                            .isEqualTo(1);

                    return true;
                })
                .then();

        // then
        StepVerifier.create(result)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCbsClientWithStreamsParsingWhenUsingInvalidParser() {
        // given
        final Mono<CbsClient> sut = CbsClientFactory.createCbsClient(sampleEnvironment);
        final StreamFromGsonParser<KafkaSource> kafkaSourceParser = StreamFromGsonParsers.kafkaSourceParser();
        final RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final Mono<KafkaSource> result = sut.flatMap(cbsClient -> cbsClient.get(diagnosticContext))
                .map(json ->
                        DataStreams.namedSources(json).map(kafkaSourceParser::unsafeParse).head()
                );

        // then
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex).hasMessageContaining("Invalid stream type");
                    assertThat(ex).hasMessageContaining("message_router");
                    assertThat(ex).hasMessageContaining("kafka");
                })
                .verify(Duration.ofSeconds(5));
    }

    private String sampleConfigValue(JsonObject obj) {
        return obj.get(SAMPLE_CONFIG_KEY).getAsString();
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
