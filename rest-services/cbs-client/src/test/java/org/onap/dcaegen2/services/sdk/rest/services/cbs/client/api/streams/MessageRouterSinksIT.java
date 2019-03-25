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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamPredicates.streamWithName;

import com.google.gson.JsonObject;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.StreamType;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.MessageRouterSink;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
class MessageRouterSinksIT {

    final JsonObject json = GsonUtils.readObjectFromResource("/streams/integration_message_router.json");

    MessageRouterSinksIT() throws IOException {
    }

    @Test
    void thereShouldBeNoDataSources() {
        assertThat(DataStreams.namedSources(json)).isEmpty();
    }

    @Test
    void thereShouldBeSomeSinksDefined() {
        assertThat(DataStreams.namedSinks(json)).isNotEmpty();
        assertThat(DataStreams.namedSinks(json)).hasSize(4);
    }

    @Test
    void allSinksShouldBeOfMessageRouterType() {
        assertThat(DataStreams.namedSinks(json).map(RawDataStream::type).distinct())
                .containsExactly(StreamType.MESSAGE_ROUTER);
    }

    @Test
    void sinksShouldHaveProperDirection() {
        assertThat(DataStreams.namedSinks(json).map(RawDataStream::direction).distinct())
                .containsExactly(DataStreamDirection.SINK);
    }

    @Test
    void verifySecMeasurementSink() {
        // given
        final String streamName = "sec_measurement";
        final RawDataStream<JsonObject> sink = DataStreams.namedSinks(json).find(streamWithName(streamName))
                .get();

        // when
        final MessageRouterSink parsedSink = StreamFromGsonParsers.messageRouterSinkParser().unsafeParse(sink);

        // then
        assertThat(parsedSink.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSink.aafCredentials()).describedAs("aaf credentials").isNotNull();
        assertThat(parsedSink.aafCredentials().username()).describedAs("aaf user name").isEqualTo("aaf_username");
        assertThat(parsedSink.aafCredentials().password()).describedAs("aaf password").isEqualTo("aaf_password");
        assertThat(parsedSink.location()).describedAs("location").isEqualTo("mtl5");
        assertThat(parsedSink.clientId()).describedAs("client id").isEqualTo("111111");
        assertThat(parsedSink.clientRole()).describedAs("client role").isEqualTo("com.att.dcae.member");
        assertThat(parsedSink.topicUrl()).describedAs("topic url")
                .isEqualTo("https://mrlocal:3905/events/com.att.dcae.dmaap.FTL2.SEC-MEASUREMENT-OUTPUT");
    }

    @Test
    void verifySecFaultUnsecureSink() {
        // given
        final String streamName = "sec_fault_unsecure";
        final RawDataStream<JsonObject> sink = DataStreams.namedSinks(json).find(streamWithName(streamName))
                .get();

        // when
        final MessageRouterSink parsedSink = StreamFromGsonParsers.messageRouterSinkParser().unsafeParse(sink);

        // then
        assertThat(parsedSink.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSink.aafCredentials()).describedAs("aaf credentials").isNull();
        assertThat(parsedSink.location()).describedAs("location").isEqualTo("mtl5");
        assertThat(parsedSink.clientId()).describedAs("client id").isNull();
        assertThat(parsedSink.clientRole()).describedAs("client role").isNull();
        assertThat(parsedSink.topicUrl()).describedAs("topic url")
                .isEqualTo("http://ueb.global:3904/events/DCAE-SE-COLLECTOR-EVENTS-DEV");
    }

    @Test
    void verifySecMeasurementUnsecureSink() {
        // given
        final String streamName = "sec_measurement_unsecure";
        final RawDataStream<JsonObject> sink = DataStreams.namedSinks(json).find(streamWithName(streamName))
                .get();

        // when
        final MessageRouterSink parsedSink = StreamFromGsonParsers.messageRouterSinkParser().unsafeParse(sink);

        // then
        assertThat(parsedSink.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSink.aafCredentials()).describedAs("aaf credentials").isNull();
        assertThat(parsedSink.location()).describedAs("location").isEqualTo("mtl5");
        assertThat(parsedSink.clientId()).describedAs("client id").isNull();
        assertThat(parsedSink.clientRole()).describedAs("client role").isNull();
        assertThat(parsedSink.topicUrl()).describedAs("topic url")
                .isEqualTo("http://ueb.global:3904/events/DCAE-SE-COLLECTOR-EVENTS-DEV");
    }

    @Test
    void verifySecFaultSink() {
        // given
        final String streamName = "sec_fault";
        final RawDataStream<JsonObject> sink = DataStreams.namedSinks(json).find(streamWithName(streamName))
                .get();

        // when
        final MessageRouterSink parsedSink = StreamFromGsonParsers.messageRouterSinkParser().unsafeParse(sink);

        // then
        assertThat(parsedSink.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSink.aafCredentials()).describedAs("aaf credentials").isNotNull();
        assertThat(parsedSink.aafCredentials().username()).describedAs("aaf user name").isEqualTo("aaf_username");
        assertThat(parsedSink.aafCredentials().password()).describedAs("aaf password").isEqualTo("aaf_password");
        assertThat(parsedSink.location()).describedAs("location").isEqualTo("mtl5");
        assertThat(parsedSink.clientId()).describedAs("client id").isEqualTo("222222");
        assertThat(parsedSink.clientRole()).describedAs("client role").isEqualTo("com.att.dcae.member");
        assertThat(parsedSink.topicUrl()).describedAs("topic url")
                .isEqualTo("https://mrlocal:3905/events/com.att.dcae.dmaap.FTL2.SEC-FAULT-OUTPUT");
    }
}