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
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.utils.VesEvents.defaultVesEvent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.vavr.control.Try;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.PayloadType;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.EncodersFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.ProtobufEncoder;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.WireFrameEncoder;
import org.onap.ves.VesEventOuterClass.VesEvent;
import reactor.core.publisher.Flux;

/**
 * @author <a href="mailto:jakub.dudycz@nokia.com">Jakub Dudycz</a>
 */
class ProducerCoreTest {

    private ProducerCore producerCore;
    private EncodersFactory encodersFactoryMock;
    private final ProtobufEncoder protobufEncoder = new ProtobufEncoder();

    @BeforeEach
    void setUp() {
        encodersFactoryMock = mock(EncodersFactory.class);
        producerCore = new ProducerCore(encodersFactoryMock);
    }

    @Test
    void encode_should_encode_raw_message_stream_to_wire_frame() {
        final WireFrameEncoder wireFrameEncoder = mock(WireFrameEncoder.class);
        final ByteBuffer payload = ByteBuffer.wrap(new byte[3]);
        final Try<ByteBuf> wireFrameBuffer = Try.success(Unpooled.copiedBuffer(new byte[5]));

        when(wireFrameEncoder.encode(payload, PayloadType.UNDEFINED)).thenReturn(wireFrameBuffer);
        when(encodersFactoryMock.createWireFrameEncoder(ByteBufAllocator.DEFAULT)).thenReturn(wireFrameEncoder);

        // given
        final int messageStreamSize = 2;
        final Flux<ByteBuffer> rawMessageStream = Flux.just(payload).repeat(messageStreamSize - 1);

        // when
        final ByteBuf lastMessage = producerCore
                .encode(rawMessageStream, PayloadType.UNDEFINED, ByteBufAllocator.DEFAULT)
                .blockLast();

        // then
        verify(encodersFactoryMock).createWireFrameEncoder(ByteBufAllocator.DEFAULT);
        verify(wireFrameEncoder, times(messageStreamSize)).encode(payload, PayloadType.UNDEFINED);

        assertThat(lastMessage).isNotNull();
        assertThat(lastMessage).isEqualTo(wireFrameBuffer.get());
    }

    @Test
    void encode_should_encode_ves_event_stream_to_wire_frame() {
        final WireFrameEncoder wireFrameEncoder = mock(WireFrameEncoder.class);
        final ProtobufEncoder protobufEncoder = mock(ProtobufEncoder.class);

        final VesEvent vesEvent = defaultVesEvent();
        final ByteBuffer protoBuffer = ByteBuffer.wrap(new byte[3]);
        final Try<ByteBuf> wireFrameBuffer = Try.success(Unpooled.copiedBuffer(new byte[5]));

        when(protobufEncoder.encode(vesEvent)).thenReturn(protoBuffer);
        when(wireFrameEncoder.encode(protoBuffer, PayloadType.PROTOBUF)).thenReturn(wireFrameBuffer);
        when(encodersFactoryMock.createProtobufEncoder()).thenReturn(protobufEncoder);
        when(encodersFactoryMock.createWireFrameEncoder(ByteBufAllocator.DEFAULT)).thenReturn(wireFrameEncoder);

        // given
        final int messageStreamSize = 2;
        final Flux<VesEvent> messages = Flux.just(vesEvent).repeat(messageStreamSize - 1);

        // when
        final ByteBuf lastMessage = producerCore.encode(messages, ByteBufAllocator.DEFAULT).blockLast();

        // then
        verify(encodersFactoryMock).createProtobufEncoder();
        verify(protobufEncoder, times(messageStreamSize)).encode(vesEvent);

        assertThat(lastMessage).isNotNull();
        assertThat(lastMessage).isEqualTo(wireFrameBuffer.get());
    }
}
