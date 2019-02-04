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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.vavr.control.Try;
import java.nio.ByteBuffer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.PayloadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WireFrameEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(WireFrameEncoder.class);
    private static final short MARKER_BYTE = 0xAA;
    private static final short SUPPORTED_VERSION_MAJOR = 0x01;
    private static final short SUPPORTED_VERSION_MINOR = 0x00;
    private static final int RESERVED_BYTES_COUNT = 3;
    private static final int HEADER_SIZE = 1 * Byte.BYTES +         // marker
            2 * Byte.BYTES +                                        // single byte fields (versions)
            RESERVED_BYTES_COUNT * Byte.BYTES +                     // reserved bytes
            1 * Short.BYTES +                                       // paylaod type
            1 * Integer.BYTES;                                      // payload length

    private final ByteBufAllocator allocator;

    public WireFrameEncoder(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }


    public Try<ByteBuf> encode(ByteBuffer payload, PayloadType payloadType) {
        return Try.of(() -> encodeMessageAs(payload, payloadType))
                .onFailure(ex -> LOGGER.warn("Failed to encode payload", ex));
    }

    private ByteBuf encodeMessageAs(ByteBuffer payload, PayloadType payloadType) throws WTPEncodingException {
        if (payload == null) {
            throw new WTPEncodingException("Payload is null");
        }

        final int payloadSize = payload.remaining();
        if (payloadSize == 0) {
            throw new WTPEncodingException("Payload is empty");
        }

        final ByteBuf encodedMessage = allocator.buffer(HEADER_SIZE + payloadSize);
        writeBasicWTPFrameHeaderBeginning(encodedMessage);
        writePayloadMessageHeaderEnding(encodedMessage, payloadType, payload, payloadSize);
        return encodedMessage;
    }

    private void writeBasicWTPFrameHeaderBeginning(ByteBuf encodedMessage) {
        encodedMessage.writeByte(MARKER_BYTE);
        encodedMessage.writeByte(SUPPORTED_VERSION_MAJOR);
        encodedMessage.writeByte(SUPPORTED_VERSION_MINOR);
        encodedMessage.writeZero(RESERVED_BYTES_COUNT);
    }

    private void writePayloadMessageHeaderEnding(ByteBuf encodedMessage,
            PayloadType payloadType,
            ByteBuffer payload,
            int payloadSize) {
        encodedMessage.writeBytes(payloadType.getPayloadTypeBytes());
        encodedMessage.writeInt(payloadSize);
        encodedMessage.writeBytes(payload);
    }
}


class WTPEncodingException extends RuntimeException {

    WTPEncodingException(String message) {
        super(message);
    }
}