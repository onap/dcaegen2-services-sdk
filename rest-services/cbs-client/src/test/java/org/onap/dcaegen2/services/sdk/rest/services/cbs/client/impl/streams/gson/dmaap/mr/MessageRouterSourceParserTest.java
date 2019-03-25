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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap.mr;

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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.MessageRouterSink;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.MessageRouterSource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.StreamsConstants.DATA_ROUTER_TYPE;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.StreamsConstants.MESSAGE_ROUTER_TYPE;

/**
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 */

public class MessageRouterSourceParserTest {

    private static final String SAMPLE_AAF_USERNAME = "some-user";
    private static final String SAMPLE_AAF_PASSWORD = "some-password";
    private static final String SAMPLE_LOCATION = "mtc00";
    private static final String SAMPLE_CLIENT_ROLE = "com.dcae.member";
    private static final String SAMPLE_CLIENT_ID = "1500462518108";
    private static final String SAMPLE_TOPIC_URL = "https://we-are-message-router.us:3905/events/some-topic";

    private final StreamFromGsonParser<MessageRouterSource> streamParser = StreamFromGsonParsers.messageRouterSourceParser();

    @Test
    void fullConfiguration_shouldGenerateDataRouterSourceObject() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSourceFromResource("/streams/message_router_full.json");

        // when
        MessageRouterSource result = streamParser.unsafeParse(input);

        // then
        assertThat(result.aafCredentials().username()).isEqualTo(SAMPLE_AAF_USERNAME);
        assertThat(result.aafCredentials().password()).isEqualTo(SAMPLE_AAF_PASSWORD);
        assertThat(result.location()).isEqualTo(SAMPLE_LOCATION);
        assertThat(result.clientRole()).isEqualTo(SAMPLE_CLIENT_ROLE);
        assertThat(result.clientId()).isEqualTo(SAMPLE_CLIENT_ID);
        assertThat(result.topicUrl()).isEqualTo(SAMPLE_TOPIC_URL);
    }

    @Test
    void minimalConfiguration_shouldGenerateDataRouterSourceObject() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSourceFromResource("/streams/message_router_minimal.json");

        // when
        MessageRouterSource result = streamParser.unsafeParse(input);

        // then
        assertThat(result.topicUrl()).isEqualTo(SAMPLE_TOPIC_URL);
        assertThat(result.aafCredentials()).isNull();
        assertThat(result.clientId()).isNull();
    }

    @Test
    void incorrectConfiguration_shouldParseToStreamParserError() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSourceFromResource("/streams/data_router_sink_full.json");

        // when
        Either<StreamParserError, MessageRouterSource> result = streamParser.parse(input);

        // then
        assertThat(result.getLeft()).isInstanceOf(StreamParserError.class);
        result.peekLeft(error -> {
                    assertThat(error.message()).contains("Invalid stream type");
                    assertThat(error.message()).contains("Expected '" + MESSAGE_ROUTER_TYPE + "', but was '"
                            + DATA_ROUTER_TYPE + "'");
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
                .direction(DataStreamDirection.SOURCE)
                .build();
        // when
        Either<StreamParserError, MessageRouterSource> result = streamParser.parse(input);
        // then
        assertThat(result.getLeft()).isInstanceOf(StreamParserError.class);
    }


}