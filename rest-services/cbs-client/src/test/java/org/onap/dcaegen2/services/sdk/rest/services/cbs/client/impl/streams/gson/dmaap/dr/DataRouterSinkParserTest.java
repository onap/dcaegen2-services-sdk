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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap.dr;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParserError;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParsers;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.DataStreamUtils;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.ImmutableRawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.DataRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.ImmutableDataRouterSink;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.StreamsConstants.DATA_ROUTER_TYPE;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.StreamsConstants.MESSAGE_ROUTER_TYPE;


class DataRouterSinkParserTest {

    private static final String SAMPLE_LOCATION = "mtc00";
    private static final String SAMPLE_PUBLISH_URL = "https://we-are-data-router.us/feed/xyz";
    private static final String SAMPLE_LOG_URL = "https://we-are-data-router.us/feed/xyz/logs";
    private static final String SAMPLE_USER = "some-user";
    private static final String SAMPLE_PASSWORD = "some-password";
    private static final String SAMPLE_PUBLISHER_ID = "123456";

    private final StreamFromGsonParser<DataRouterSink> streamParser = StreamFromGsonParsers.dataRouterSinkParser();

    @Test
    void fullConfiguration_shouldGenerateDataRouterSinkObject() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSinkFromResource("/streams/data_router_sink_full.json");

        // when
        DataRouterSink result = streamParser.unsafeParse(input);

        // then
        final DataRouterSink fullConfigurationStream = ImmutableDataRouterSink.builder()
                .name(input.name())
                .location(SAMPLE_LOCATION)
                .publishUrl(SAMPLE_PUBLISH_URL)
                .logUrl(SAMPLE_LOG_URL)
                .username(SAMPLE_USER)
                .password(SAMPLE_PASSWORD)
                .publisherId(SAMPLE_PUBLISHER_ID)
                .build();
        assertThat(result).isEqualTo(fullConfigurationStream);
    }

    @Test
    void minimalConfiguration_shouldGenerateDataRouterSinkObject() throws IOException {
        //given
        RawDataStream<JsonObject> input = DataStreamUtils
                .readSinkFromResource("/streams/data_router_sink_minimal.json");

        // when
        DataRouterSink result = streamParser.unsafeParse(input);

        // then
        final DataRouterSink minimalConfigurationStream = ImmutableDataRouterSink.builder()
                .name(input.name())
                .publishUrl(SAMPLE_PUBLISH_URL)
                .build();
        assertThat(result).isEqualTo(minimalConfigurationStream);
    }

    @Test
    void incorrectConfiguration_shouldParseToStreamParserError() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSinkFromResource("/streams/message_router_full.json");

        // when
        Either<StreamParserError, DataRouterSink> result = streamParser.parse(input);

        // then
        assertThat(result.getLeft()).isInstanceOf(StreamParserError.class);
        result.peekLeft(error -> {
                    assertThat(error.message()).contains("Invalid stream type");
                    assertThat(error.message()).contains("Expected '" + DATA_ROUTER_TYPE + "', but was '"
                            + MESSAGE_ROUTER_TYPE + "'");
                }
        );
    }

    @Test
    void emptyConfiguration_shouldParseToStreamParserError() {
        // given
        JsonObject json = new JsonObject();
        final ImmutableRawDataStream<JsonObject> input = ImmutableRawDataStream.<JsonObject>builder()
                .name("empty")
                .type("data_router")
                .descriptor(json)
                .direction(DataStreamDirection.SINK)
                .build();

        // when
        Either<StreamParserError, DataRouterSink> result = streamParser.parse(input);

        // then
        assertThat(result.getLeft()).isInstanceOf(StreamParserError.class);
    }

}