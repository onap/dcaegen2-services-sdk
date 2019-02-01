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

import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.ves.MeasDataCollectionOuterClass;
import org.onap.ves.VesEventOuterClass.CommonEventHeader;
import org.onap.ves.VesEventOuterClass.VesEvent;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
class HvVesProducerIT {

    private static final int INFO_ID = 17;
    private static final long VALUE = 5l;
    private static final int MEAS_TYPE = 3;
    private static final int PERIOD = 1000;
    private static final String OBJECT_INSTANCE_ID = "DH-1";

    private final SystemUnderTestWrapper sut = new SystemUnderTestWrapper();

    @BeforeEach
    void setUp() {
        sut.start();
    }

    @AfterEach
    void tearDown() {
        sut.stop();
    }

    @Test
    void singleMessageTest() throws Exception {
        // given

        final VesEvent sampleEvent = createSimpleVesEvent();
        final Flux<VesEvent> input = Flux.just(sampleEvent);

        // when
        final ByteBuf receivedData = sut.blockingSend(input);

        // then

        // check wire protocol
        byte[] wireProtocolHeader = new byte[6];
        receivedData.readBytes(wireProtocolHeader);
        short type = receivedData.readShort();
        assertThat(type).isEqualTo((short)0x0001);

        // check common header
        int payloadSize = receivedData.readInt();
        byte[] buffer = new byte[payloadSize];
        receivedData.readBytes(buffer);
        VesEvent result = VesEvent.parseFrom(buffer);
        assertThat(result.getCommonEventHeader().getDomain()).isEqualTo("RTPM");

        // check payload
        MeasDataCollectionOuterClass.MeasDataCollection content =
                MeasDataCollectionOuterClass.MeasDataCollection.parseFrom(result.getEventFields());
        assertThat(content.getGranularityPeriod()).isEqualTo(PERIOD);
        assertThat(content.getMeasObjInstIdListList()).containsExactly(OBJECT_INSTANCE_ID);
        assertThat(content.getMeasObjInstIdListList().size()).isEqualTo(1);
        MeasDataCollectionOuterClass.MeasInfo info = content.getMeasInfo(0);
        assertThat(info.getIMeasInfoId()).isEqualTo(INFO_ID);
        assertThat(info.getIMeasTypes().getIMeasTypeList()).containsExactly(MEAS_TYPE);
        assertThat(info.getMeasValuesList().size()).isEqualTo(1);
        assertThat(info.getMeasValues(0).getMeasResultsList().size()).isEqualTo(1);
        assertThat(info.getMeasValues(0).getMeasResults(0).getIValue()).isEqualTo(VALUE);
    }

    private VesEvent createSimpleVesEvent() {
        final MeasDataCollectionOuterClass.MeasDataCollection content = MeasDataCollectionOuterClass.MeasDataCollection.newBuilder()
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
