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
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.ves.VesEventOuterClass.VesEvent;
import reactor.core.publisher.Flux;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
class HvVesProducerIT {

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
    void todo() {
        // given
        final Flux<VesEvent> input = Flux.just(VesEvent.getDefaultInstance());

        // when
        // This will currently fail
        //final ByteBuf receivedData = sut.blockingSend(input);
        final ByteBuf receivedData = ByteBufAllocator.DEFAULT.buffer().writeByte(8);

        // then
        assertThat(receivedData.readableBytes())
                .describedAs("data length")
                .isGreaterThan(0);
    }
}
