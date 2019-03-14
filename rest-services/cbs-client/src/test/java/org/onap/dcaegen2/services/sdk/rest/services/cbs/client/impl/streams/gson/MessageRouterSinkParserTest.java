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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParserError;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParsers;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.MessageRouterSink;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 */

public class MessageRouterSinkParserTest {

    private static final String SAMPLE_AAF_USERNAME = "some-user";
    private static final String SAMPLE_AAF_PASSWORD = "some-password";
    private static final String SAMPLE_LOCATION = "mtc00";
    private static final String SAMPLE_CLIENT_ROLE = "com.dcae.member";
    private static final String SAMPLE_CLIENT_ID = "1500462518108";
    private static final String SAMPLE_TOPIC_URL = "https://we-are-message-router.us:3905/events/some-topic";

    private static final Gson gson = new Gson();

    private final StreamFromGsonParser<MessageRouterSink> streamParser = StreamFromGsonParsers.messageRouterSinkParser();

    @Test
    void fullConfiguration_shouldGenerateDataRouterSinkObject() throws IOException {
        // given
        JsonObject input = GsonUtils.readObjectFromResource("/streams/message_router_full.json");
        // when
        MessageRouterSink result = streamParser.unsafeParse(input);
        // then
        assertThat(result).isInstanceOf(MessageRouterSink.class);
        assertThat(result.aafCredentials().username()).isEqualTo(SAMPLE_AAF_USERNAME);
        assertThat(result.aafCredentials().password()).isEqualTo(SAMPLE_AAF_PASSWORD);
        assertThat(result.location()).isEqualTo(SAMPLE_LOCATION);
        assertThat(result.clientRole()).isEqualTo(SAMPLE_CLIENT_ROLE);
        assertThat(result.clientId()).isEqualTo(SAMPLE_CLIENT_ID);
        assertThat(result.topicUrl()).isEqualTo(SAMPLE_TOPIC_URL);
    }

    @Test
    void minimalConfiguration_shouldGenerateDataRouterSinkObject() throws IOException {
        // given
        JsonObject input = GsonUtils.readObjectFromResource("/streams/message_router_minimal.json");

        // when
        MessageRouterSink result = streamParser.unsafeParse(input);
        // then
        assertThat(result).isInstanceOf(MessageRouterSink.class);
        assertThat(result.topicUrl()).isEqualTo(SAMPLE_TOPIC_URL);
        assertThat(result.aafCredentials().username()).isNull();
        assertThat(result.aafCredentials().password()).isNull();
        assertThat(result.clientId()).isNull();
    }

    @Test
    void emptyConfiguration_shouldParseToStreamParserError() {
        // given
        final JsonObject emptyJsonObject = gson.fromJson("{}", JsonObject.class);
        // when
        Either<StreamParserError, MessageRouterSink> result = streamParser.parse(emptyJsonObject);
        // then
        assertThat(result.getLeft()).isInstanceOf(StreamParserError.class);
    }


}