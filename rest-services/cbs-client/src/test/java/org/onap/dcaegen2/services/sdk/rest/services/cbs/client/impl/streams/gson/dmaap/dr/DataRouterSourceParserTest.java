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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;
import io.vavr.control.Either;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParserError;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParsers;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.DataStreamUtils;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.ImmutableRawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.StreamType;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.DataRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.ImmutableDataRouterSource;

public class DataRouterSourceParserTest {

    private static final String SAMPLE_LOCATION = "mtc00";
    private static final String SAMPLE_DELIVERY_URL = "https://my-subscriber-app.dcae:8080/target-path";
    private static final String SAMPLE_USER = "some-user";
    private static final String SAMPLE_PASSWORD = "some-password";
    private static final String SAMPLE_SUBSCRIBER_ID = "789012";

    private final StreamFromGsonParser<DataRouterSource> streamParser = StreamFromGsonParsers.dataRouterSourceParser();

    @Test
    void fullConfiguration_shouldGenerateDataRouterSinkObject() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSourceFromResource("/streams/data_router_source_full.json");

        // when
        DataRouterSource result = streamParser.unsafeParse(input);

        // then

        final DataRouterSource fullConfigurationStream = ImmutableDataRouterSource.builder()
                .name(input.name())
                .location(SAMPLE_LOCATION)
                .deliveryUrl(SAMPLE_DELIVERY_URL)
                .username(SAMPLE_USER)
                .password(SAMPLE_PASSWORD)
                .subscriberId(SAMPLE_SUBSCRIBER_ID)
                .build();
        assertThat(result).isEqualTo(fullConfigurationStream);
    }

    @Test
    void minimalConfiguration_shouldGenerateDataRouterSinkObject() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils
                .readSourceFromResource("/streams/data_router_source_minimal.json");

        // when
        DataRouterSource result = streamParser.unsafeParse(input);

        // then
        final DataRouterSource minimalConfigurationStream = ImmutableDataRouterSource.builder()
                .name(input.name())
                .build();
        assertThat(result).isEqualTo(minimalConfigurationStream);
    }

    @Test
    void incorrectConfiguration_shouldParseToStreamParserError() throws IOException {
        // given
        RawDataStream<JsonObject> input = DataStreamUtils.readSourceFromResource("/streams/message_router_full.json");

        // when
        Either<StreamParserError, DataRouterSource> result = streamParser.parse(input);

        // then
        assertThat(result.getLeft()).isInstanceOf(StreamParserError.class);
        result.peekLeft(error -> {
                    assertThat(error.message()).contains("Invalid stream type");
                    assertThat(error.message()).contains("Expected '" + StreamType.DATA_ROUTER + "', but was '"
                            + StreamType.MESSAGE_ROUTER + "'");
                }
        );
    }

    @Test
    void emptyConfiguration_shouldBeParsedToStreamParserError() {
        // given
        JsonObject json = new JsonObject();
        final ImmutableRawDataStream<JsonObject> input = ImmutableRawDataStream.<JsonObject>builder()
                .name("empty")
                .type(StreamType.DATA_ROUTER)
                .descriptor(json)
                .direction(DataStreamDirection.SOURCE)
                .build();

        // when
        Either<StreamParserError, DataRouterSource> result = streamParser.parse(input);

        // then
        assertThat(result.getLeft()).isInstanceOf(StreamParserError.class);
    }
}
