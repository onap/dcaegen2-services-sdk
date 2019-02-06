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

import io.vavr.control.Try;
import org.junit.jupiter.api.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.ByteBuffer;

public class WireFrameEncoderTest {
    private static final byte MARKER_BYTE = (byte) 0xAA;
    private static final byte SUPPORTED_VERSION_MAJOR = (byte) 0x01;
    private static final byte SUPPORTED_VERSION_MINOR = (byte) 0x00;
    private static final byte SAMPLE_VERSION_MAJOR = (byte) 0x02;
    private static final byte SAMPLE_VERSION_MINOR = (byte) 0x01;
    private static final int RESERVED_BYTES_COUNT = 3;
    private static final int HEADER_SIZE = 1 * Byte.BYTES +         // marker
            2 * Byte.BYTES +                                        // single byte fields (versions)
            RESERVED_BYTES_COUNT * java.lang.Byte.BYTES +           // reserved bytes
            1 * Short.BYTES +                                       // paylaod type
            1 * Integer.BYTES;                                      // payload length

    private final WireFrameVersion wireFrameVersion = new WireFrameVersion(SUPPORTED_VERSION_MAJOR,
            SUPPORTED_VERSION_MINOR);
    private final WireFrameEncoder wireFrameEncoder = new WireFrameEncoder(ByteBufAllocator.DEFAULT, wireFrameVersion);

    @Test
    void encode_givenNullPayload_shouldThrowEncodingException() {
        final ByteBuffer buffer = null;

        Try<ByteBuf> encodedBuffer = wireFrameEncoder.encode(buffer);

        assertThat(encodedBuffer.isFailure()).isTrue();
        assertThat(encodedBuffer.getCause()).isInstanceOf(WTPEncodingException.class);
    }

    @Test
    void encode_givenEmptyPayload_shouldThrowEncodingException() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(0);

        Try<ByteBuf> encodedBuffer = wireFrameEncoder.encode(buffer);

        assertThat(encodedBuffer.isFailure()).isTrue();
        assertThat(encodedBuffer.getCause()).isInstanceOf(WTPEncodingException.class);
    }

    @Test
    void encode_givenSomePayloadBytes_shouldCreateValidGPBFrameWithPayloadAtTheEnd() {
        final byte[] payloadBytes = new byte[]{0x1A, 0x2B, 0x3C};
        final int bufferSize = payloadBytes.length;
        final ByteBuffer buffer = ByteBuffer.wrap(payloadBytes);

        final Try<ByteBuf> encodedBuffer = wireFrameEncoder.encode(buffer);

        assertThat(encodedBuffer.isSuccess()).isTrue();
        final ByteBuf actualEncodedBuffer = encodedBuffer.get();
        assertBufferSizeIs(actualEncodedBuffer, HEADER_SIZE + bufferSize);
        assertValidHeaderBeggining(actualEncodedBuffer);
        skipReservedBytes(actualEncodedBuffer);
        assertNextBytesAreInOrder(actualEncodedBuffer, (byte) 0x00, (byte) 0x01);
        assertNextBytesAreInOrder(actualEncodedBuffer, intToBytes(bufferSize));
        assertNextBytesAreInOrder(actualEncodedBuffer, payloadBytes);
        assertAllBytesVerified(actualEncodedBuffer);
    }

    @Test
    void encode_givesSomePayloadBytes_shouldCreateValidGPBFrameWithSpecifiedWTPVersion(){
        // given
        WireFrameVersion wireFrameVersion = new WireFrameVersion(SAMPLE_VERSION_MAJOR, SAMPLE_VERSION_MINOR);
        final WireFrameEncoder encoder = new WireFrameEncoder(ByteBufAllocator.DEFAULT, wireFrameVersion);
        final byte[] payloadBytes = new byte[]{0x1A, 0x2B, 0x3C};
        final ByteBuffer buffer = ByteBuffer.wrap(payloadBytes);

        // when
        final Try<ByteBuf> encodedBuffer = encoder.encode(buffer);

        // then
        assertThat(encodedBuffer.isSuccess()).isTrue();
        final ByteBuf versionBuffer = encodedBuffer.get();
        assertValidHeaderBeggining(versionBuffer, SAMPLE_VERSION_MAJOR, SAMPLE_VERSION_MINOR);
    }

    private void assertNextBytesAreInOrder(ByteBuf encodedBuffer, byte... bytes) {
        for (int i = 0; i < bytes.length; i++) {
            assertThat(encodedBuffer.readByte())
                    .describedAs("byte in " + (i + 1) + " assertion")
                    .isEqualTo(bytes[i]);
        }
    }

    private void assertValidHeaderBeggining(ByteBuf encodedBuffer) {
        assertValidHeaderBeggining(encodedBuffer, SUPPORTED_VERSION_MAJOR, SUPPORTED_VERSION_MINOR);
    }

    private void assertValidHeaderBeggining(ByteBuf encodedBuffer, byte majorWTPVersion, byte minorWTPVersion) {
        assertNextBytesAreInOrder(encodedBuffer,
                MARKER_BYTE,
                majorWTPVersion,
                minorWTPVersion);
    }

    private void assertBufferSizeIs(ByteBuf encodedBuffer, int headerSize) {
        assertThat(encodedBuffer.readableBytes()).describedAs("buffer's readable bytes").isEqualTo(headerSize);
    }

    private void skipReservedBytes(ByteBuf encodedBuffer) {
        encodedBuffer.readBytes(RESERVED_BYTES_COUNT);
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
