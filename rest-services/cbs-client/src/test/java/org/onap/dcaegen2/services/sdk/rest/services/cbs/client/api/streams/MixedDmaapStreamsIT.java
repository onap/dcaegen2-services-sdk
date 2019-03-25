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
import io.vavr.collection.List;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils;
import org.onap.dcaegen2.services.sdk.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.model.streams.StreamType;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.DataRouterSink;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.DataRouterSource;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.MessageRouterSource;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
class MixedDmaapStreamsIT {

    final JsonObject json = GsonUtils.readObjectFromResource("/streams/integration_mixed_dmaap.json");
    final List<RawDataStream<JsonObject>> sources = DataStreams.namedSources(json).toList();
    final List<RawDataStream<JsonObject>> sinks = DataStreams.namedSinks(json).toList();

    MixedDmaapStreamsIT() throws IOException {
    }

    @Test
    void thereShouldBeSomeSinksDefined() {
        assertThat(sinks).isNotEmpty();
        assertThat(sinks).hasSize(3);
    }

    @Test
    void thereShouldBeSomeSourcesDefined() {
        assertThat(sources).isNotEmpty();
        assertThat(sources).hasSize(3);
    }

    @Test
    void allStreamsShouldBeOfProperType() {
        assertThat(sources.map(RawDataStream::type).distinct()).containsExactly(StreamType.DATA_ROUTER, StreamType.MESSAGE_ROUTER);
        assertThat(sinks.map(RawDataStream::type).distinct()).containsExactly(StreamType.DATA_ROUTER);
    }

    @Test
    void sinksShouldHaveProperDirection() {
        assertThat(sinks.map(RawDataStream::direction).distinct())
                .containsExactly(DataStreamDirection.SINK);
    }

    @Test
    void sourcesShouldHaveProperDirection() {
        assertThat(sources.map(RawDataStream::direction).distinct())
                .containsExactly(DataStreamDirection.SOURCE);
    }

    @Test
    void verifyDcaeGuestOsSource() {
        // given
        final String streamName = "DCAE_GUEST_OS";
        final RawDataStream<JsonObject> source = sources.find(streamWithName(streamName)).get();

        // when
        final DataRouterSource parsedSource = StreamFromGsonParsers.dataRouterSourceParser().unsafeParse(source);

        // then
        assertThat(parsedSource.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSource.location()).describedAs("location").isEqualTo("mtn23");
        assertThat(parsedSource.username()).describedAs("user name").isEqualTo("xyz");
        assertThat(parsedSource.password()).describedAs("password").isEqualTo("abc");
        assertThat(parsedSource.deliveryUrl()).describedAs("delivery url")
                .isEqualTo("https://dr.global:8666/DCAE_SAM_GUEST_OS");
        assertThat(parsedSource.subscriberId()).describedAs("subscriber id").isEqualTo("811");
    }

    @Test
    void verifyDcaeRawDataSource() {
        // given
        final String streamName = "DCAE_RAW_DATA";
        final RawDataStream<JsonObject> source = sources.find(streamWithName(streamName)).get();

        // when
        final DataRouterSource parsedSource = StreamFromGsonParsers.dataRouterSourceParser().unsafeParse(source);

        // then
        assertThat(parsedSource.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSource.location()).describedAs("location").isEqualTo("mtn23");
        assertThat(parsedSource.username()).describedAs("user name").isEqualTo("abc");
        assertThat(parsedSource.password()).describedAs("password").isEqualTo("xyz");
        assertThat(parsedSource.deliveryUrl()).describedAs("delivery url")
                .isEqualTo("https://dr.global:8666/DCAE_CEILOMETER_RAW_DATA");
        assertThat(parsedSource.subscriberId()).describedAs("subscriber id").isEqualTo("812");
    }

    @Test
    void verifySecMeasurementOutputSource() {
        // given
        final String streamName = "sec-measurement-output";
        final RawDataStream<JsonObject> source = sources.find(streamWithName(streamName))
                .get();

        // when
        final MessageRouterSource parsedSource = StreamFromGsonParsers.messageRouterSourceParser().unsafeParse(source);

        // then
        assertThat(parsedSource.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSource.aafCredentials()).describedAs("aaf credentials").isNotNull();
        assertThat(parsedSource.aafCredentials().username()).describedAs("aaf user name").isEqualTo("aaf_username");
        assertThat(parsedSource.aafCredentials().password()).describedAs("aaf password").isEqualTo("aaf_password");
        assertThat(parsedSource.location()).describedAs("location").isEqualTo("mtn23");
        assertThat(parsedSource.clientId()).describedAs("client id").isEqualTo("1111");
        assertThat(parsedSource.clientRole()).describedAs("client role").isEqualTo("com.att.dcae.member");
        assertThat(parsedSource.topicUrl()).describedAs("topic url")
                .isEqualTo("https://mr.hostname:3905/events/com.att.dcae.dmaap.SEC-MEASUREMENT-OUTPUT-v1");
    }

    @Test
    void verifyDcaeVoipPmDataSink() {
        // given
        final String streamName = "DCAE_VOIP_PM_DATA";
        final RawDataStream<JsonObject> sink = sinks.find(streamWithName(streamName)).get();

        // when
        final DataRouterSink parsedSink = StreamFromGsonParsers.dataRouterSinkParser().unsafeParse(sink);

        // then
        assertThat(parsedSink.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSink.location()).describedAs("location").isEqualTo("mtn23");
        assertThat(parsedSink.username()).describedAs("user name").isEqualTo("abc");
        assertThat(parsedSink.password()).describedAs("password").isEqualTo("xyz");
        assertThat(parsedSink.logUrl()).describedAs("log url")
                .isEqualTo("https://dcae-drps/feedlog/206");
        assertThat(parsedSink.publishUrl()).describedAs("publish url")
                .isEqualTo("https://dcae-drps/publish/206");
        assertThat(parsedSink.publisherId()).describedAs("publisher id").isEqualTo("206.518hu");
    }

    @Test
    void verifyDcaeGuestOsOSink() {
        // given
        final String streamName = "DCAE_GUEST_OS_O";
        final RawDataStream<JsonObject> sink = sinks.find(streamWithName(streamName)).get();

        // when
        final DataRouterSink parsedSink = StreamFromGsonParsers.dataRouterSinkParser().unsafeParse(sink);

        // then
        assertThat(parsedSink.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSink.location()).describedAs("location").isEqualTo("mtn23");
        assertThat(parsedSink.username()).describedAs("user name").isEqualTo("axyz");
        assertThat(parsedSink.password()).describedAs("password").isEqualTo("abc");
        assertThat(parsedSink.logUrl()).describedAs("log url")
                .isEqualTo("https://dcae-drps/feedlog/203");
        assertThat(parsedSink.publishUrl()).describedAs("publish url")
                .isEqualTo("https://dcae-drps/publish/203");
        assertThat(parsedSink.publisherId()).describedAs("publisher id").isEqualTo("203.2od8s");
    }


    @Test
    void verifyDcaePmDataSink() {
        // given
        final String streamName = "DCAE_PM_DATA";
        final RawDataStream<JsonObject> sink = sinks.find(streamWithName(streamName)).get();

        // when
        final DataRouterSink parsedSink = StreamFromGsonParsers.dataRouterSinkParser().unsafeParse(sink);

        // then
        assertThat(parsedSink.name()).describedAs("name").isEqualTo(streamName);
        assertThat(parsedSink.location()).describedAs("location").isEqualTo("mtn23bdce2");
        assertThat(parsedSink.username()).describedAs("user name").isEqualTo("xyz");
        assertThat(parsedSink.password()).describedAs("password").isEqualTo("abc");
        assertThat(parsedSink.logUrl()).describedAs("log url")
                .isEqualTo("https://dcae-drps/feedlog/493");
        assertThat(parsedSink.publishUrl()).describedAs("publish url")
                .isEqualTo("https://dcae-drps/publish/493");
        assertThat(parsedSink.publisherId()).describedAs("publisher id").isEqualTo("493.eacqs");
    }

}