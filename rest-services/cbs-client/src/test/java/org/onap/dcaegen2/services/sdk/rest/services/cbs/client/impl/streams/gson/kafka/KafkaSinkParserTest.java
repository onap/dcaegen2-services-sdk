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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;
import io.vavr.control.Either;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParserError;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParsers;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.DataStreamUtils;
import org.onap.dcaegen2.services.sdk.model.streams.ImmutableAafCredentials;
import org.onap.dcaegen2.services.sdk.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.KafkaSink;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
class KafkaSinkParserTest {

    private final StreamFromGsonParser<KafkaSink> cut = StreamFromGsonParsers.kafkaSinkParser();

    @Test
    void precondition_assureInstanceOf() {
        assertThat(cut).isInstanceOf(KafkaSinkParser.class);
    }

    @Test
    void shouldParseMinimalKafkaSinkDefinition() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSinkFromResource("/streams/kafka_sink_minimal.json");

        // when
        final KafkaSink result = cut.unsafeParse(input);

        // then
        assertThat(result.aafCredentials()).isNull();
        assertThat(result.bootstrapServers()).isEqualTo("dmaap-mr-kafka-0:6060,dmaap-mr-kafka-1:6060");
        assertThat(result.topicName()).isEqualTo("HVVES_PERF3GPP");
        assertThat(result.clientId()).isNull();
        assertThat(result.clientRole()).isNull();
    }

    @Test
    void shouldParseFullKafkaSinkDefinition() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSinkFromResource("/streams/kafka_sink.json");

        // when
        final KafkaSink result = cut.unsafeParse(input);

        // then
        final ImmutableAafCredentials expectedCredentials = ImmutableAafCredentials.builder()
                .username("the user")
                .password("the passwd")
                .build();
        assertThat(result.aafCredentials()).isEqualTo(expectedCredentials);
        assertThat(result.bootstrapServers()).isEqualTo("dmaap-mr-kafka-0:6060,dmaap-mr-kafka-1:6060");
        assertThat(result.topicName()).isEqualTo("HVVES_PERF3GPP");
        assertThat(result.clientId()).isEqualTo("1500462518108");
        assertThat(result.clientRole()).isEqualTo("com.dcae.member");
    }

    @Test
    void shouldReturnErrorWhenStructureIsWrong() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSinkFromResource("/streams/kafka_sink_missing_child.json");

        // when
        final Either<StreamParserError, KafkaSink> result = cut.parse(input);

        // then
        assertThat(result.isRight()).describedAs("should not be right").isFalse();
        result.peekLeft(error -> {
            assertThat(error.message()).contains("kafka_info");
        });
    }

    @Test
    void shouldReturnErrorWhenTypeIsWrong() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSinkFromResource("/streams/kafka_invalid_type.json");

        // when
        final Either<StreamParserError, KafkaSink> result = cut.parse(input);

        // then
        assertThat(result.isRight()).describedAs("should not be right").isFalse();
        result.peekLeft(error -> {
            assertThat(error.message()).containsIgnoringCase("invalid stream type");
            assertThat(error.message()).containsIgnoringCase("kafka");
            assertThat(error.message()).containsIgnoringCase("message_router");
        });
    }

    @Test
    void shouldReturnErrorWhenDirectionIsWrong() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSourceFromResource("/streams/kafka_sink.json");

        // when
        final Either<StreamParserError, KafkaSink> result = cut.parse(input);

        // then
        assertThat(result.isRight()).describedAs("should not be right").isFalse();
        result.peekLeft(error -> {
            assertThat(error.message()).containsIgnoringCase("invalid stream direction");
        });
    }
}