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
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders;

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

public class WireFrameEncoderTest {

    private final WireFrameEncoder wireFrameEncoder = new WireFrameEncoder(ByteBufAllocator.DEFAULT);

    @Test
    void todo() {
        // given
        final ByteBuf buffer = Unpooled.buffer(0);

        // when
        final ByteBuf encodedBuffer = wireFrameEncoder.encode(buffer);

        // then
        assertThat(encodedBuffer.readableBytes()).isGreaterThan(0);
    }

}
