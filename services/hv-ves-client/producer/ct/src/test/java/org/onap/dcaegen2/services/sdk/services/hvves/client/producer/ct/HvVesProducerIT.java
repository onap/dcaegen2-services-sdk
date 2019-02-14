/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.ct;

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.PayloadType;
import org.onap.ves.MeasDataCollectionOuterClass;
import org.onap.ves.VesEventOuterClass.CommonEventHeader;
import org.onap.ves.VesEventOuterClass.VesEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
class HvVesProducerIT {

    private static final int INFO_ID = 17;
    private static final long VALUE = 5l;
    private static final int MEAS_TYPE = 3;
    private static final int PERIOD = 1000;
    private static final String OBJECT_INSTANCE_ID = "DH-1";

    private final SystemUnderTestWrapper sut = new SystemUnderTestWrapper(Duration.ofSeconds(10));

    @AfterEach
    void tearDown() {
        sut.stop();
    }

    @Test
    void singleMessageTest_notSecure() throws Exception {
        // given
        final VesEvent sampleEvent = createSimpleVesEvent();
        final Flux<VesEvent> input = Flux.just(sampleEvent);

        // when
        sut.start();
        final ByteBuf receivedData = sut.blockingSend(input);

        // then
        WireProtocolDecoder decoded = WireProtocolDecoder.decode(receivedData);
        assertThat(decoded.type).isEqualTo(PayloadType.PROTOBUF.getPayloadTypeBytes().getShort());
        assertThat(decoded.event).isEqualTo(sampleEvent);

    }

    @Test
    void singleMessageTest_withSecure() throws Exception {
        // given
        final VesEvent sampleEvent = createSimpleVesEvent();
        final Flux<VesEvent> input = Flux.just(sampleEvent);

        // when
        sut.startSecure();
        final ByteBuf receivedData = sut.blockingSend(input);

        // then
        WireProtocolDecoder decoded = WireProtocolDecoder.decode(receivedData);
        assertThat(decoded.type).isEqualTo(PayloadType.PROTOBUF.getPayloadTypeBytes().getShort());
        assertThat(decoded.event).isEqualTo(sampleEvent);

    }

    private VesEvent createSimpleVesEvent() {
        final MeasDataCollectionOuterClass.MeasDataCollection content = MeasDataCollectionOuterClass.MeasDataCollection
                .newBuilder()
                .addMeasInfo(MeasDataCollectionOuterClass.MeasInfo.newBuilder()
                        .addMeasValues(MeasDataCollectionOuterClass.MeasValue.newBuilder()
                                .addMeasResults(MeasDataCollectionOuterClass.MeasResult.newBuilder()
                                        .setIValue(VALUE).build())
                                .build())
                        .setIMeasInfoId(INFO_ID)
                        .setIMeasTypes(MeasDataCollectionOuterClass.MeasInfo.IMeasTypes.newBuilder()
                                .addIMeasType(MEAS_TYPE))
                        .build())
                .setGranularityPeriod(PERIOD)
                .addMeasObjInstIdList(OBJECT_INSTANCE_ID)
                .build();
        return VesEvent.newBuilder()
                .setCommonEventHeader(CommonEventHeader.newBuilder()
                        .setDomain("RTPM")
                        .build())
                .setEventFields(content.toByteString())
                .build();
    }
}
