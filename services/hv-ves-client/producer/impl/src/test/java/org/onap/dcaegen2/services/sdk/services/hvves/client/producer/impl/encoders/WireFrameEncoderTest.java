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

import java.nio.ByteBuffer;

public class WireFrameEncoderTest {
    private static final byte MARKER_BYTE = (byte) 0xAA;
    private static final byte SUPPORTED_VERSION_MAJOR = (byte) 0x01;
    private static final byte SUPPORTED_VERSION_MINOR = (byte) 0x00;
    private static final int RESERVED_BYTES_COUNT = 3;
    private static final byte[] UNDEFINED_PAYLOAD_TYPE_BYTES = new byte[]{0x00, 0x00};
    private static final byte[] GPB_PAYLOAD_TYPE_BYTES = new byte[]{0x00, 0x01};
    private static final int HEADER_SIZE = 1 * Byte.BYTES +         // marker
            2 * Byte.BYTES +                                        // single byte fields (versions)
            RESERVED_BYTES_COUNT * java.lang.Byte.BYTES +           // reserved bytes
            1 * Short.BYTES +                                       // paylaod type
            1 * Integer.BYTES;                                      // payload length

    private final WireFrameEncoder wireFrameEncoder = new WireFrameEncoder(ByteBufAllocator.DEFAULT);

    @Test
    void encode_givenNullPayload_shouldReturnUndefinedMessageHeader() {
        final ByteBuf buffer = null;

        final ByteBuf encodedBuffer = wireFrameEncoder.encode(buffer);

        assertBufferSizeIs(encodedBuffer, HEADER_SIZE);
        assertValidHeaderBeggining(encodedBuffer);
        skipReservedBytes(encodedBuffer);
        assertNextBytesAreInOrder(encodedBuffer, UNDEFINED_PAYLOAD_TYPE_BYTES);
        assertNextBytesAreInOrder(encodedBuffer, intToBytes(0));
        assertAllBytesVerified(encodedBuffer);
    }

    @Test
    void encode_givenEmptyPayload_shouldCreateValidGPBFrame() {
        final ByteBuf buffer = Unpooled.buffer(0);

        final ByteBuf encodedBuffer = wireFrameEncoder.encode(buffer);

        assertBufferSizeIs(encodedBuffer, HEADER_SIZE);
        assertValidHeaderBeggining(encodedBuffer);
        skipReservedBytes(encodedBuffer);
        assertNextBytesAreInOrder(encodedBuffer, GPB_PAYLOAD_TYPE_BYTES);
        assertNextBytesAreInOrder(encodedBuffer, intToBytes(0));
        assertAllBytesVerified(encodedBuffer);
    }

    @Test
    void encode_givenSomePayloadBytes_shouldCreateValidGPBFrameWithPayloadAtTheEnd() {
        final byte[] payloadBytes = new byte[]{0x1A, 0x2B, 0x3C};
        final int bufferSize = payloadBytes.length;
        final ByteBuf buffer = Unpooled.buffer(bufferSize);
        buffer.writeBytes(payloadBytes);

        final ByteBuf encodedBuffer = wireFrameEncoder.encode(buffer);

        assertBufferSizeIs(encodedBuffer, HEADER_SIZE + bufferSize);
        assertValidHeaderBeggining(encodedBuffer);
        skipReservedBytes(encodedBuffer);
        assertNextBytesAreInOrder(encodedBuffer, GPB_PAYLOAD_TYPE_BYTES);
        assertNextBytesAreInOrder(encodedBuffer, intToBytes(bufferSize));
        assertNextBytesAreInOrder(encodedBuffer, payloadBytes);
        assertAllBytesVerified(encodedBuffer);
    }

    private void assertNextBytesAreInOrder(ByteBuf encodedBuffer, byte... bytes) {
        for (int i = 0; i < bytes.length; i++) {
            assertThat(encodedBuffer.readByte())
                    .describedAs("byte in " + i + " assertion")
                    .isEqualTo(bytes[i]);
        }
    }

    private void assertValidHeaderBeggining(ByteBuf encodedBuffer) {
        assertNextBytesAreInOrder(encodedBuffer,
                MARKER_BYTE,
                SUPPORTED_VERSION_MAJOR,
                SUPPORTED_VERSION_MINOR);
    }

    private void assertBufferSizeIs(ByteBuf encodedBuffer, int headerSize) {
        assertThat(encodedBuffer.readableBytes()).describedAs("buffer's readable bytes").isEqualTo(headerSize);
    }

    private void skipReservedBytes(ByteBuf encodedBuffer) {
        encodedBuffer.readBytes(3);
    }

    private void assertAllBytesVerified(ByteBuf encodedBuffer) {
        assertThat(encodedBuffer.readableBytes())
                .describedAs("all bytes should've been asserted")
                .isEqualTo(0);
    }

    private byte[] intToBytes(int integer) {
        return ByteBuffer.allocate(4).putInt(integer).array();
    }
}
