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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.EncodersFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.ProtobufEncoder;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.WireFrameEncoder;
import org.onap.ves.VesEventOuterClass.VesEvent;
import reactor.core.publisher.Flux;

public class ProducerCoreTest {

    private ProducerCore producerCore;
    private EncodersFactory encodersFactoryMock;

    @BeforeEach
    public void setUp() {
        encodersFactoryMock = mock(EncodersFactory.class);
        producerCore = new ProducerCore(encodersFactoryMock);
    }

    @Test
    public void encode_should_encode_message_stream_to_wire_frame() {
        final WireFrameEncoder wireFrameEncoder = mock(WireFrameEncoder.class);
        final ProtobufEncoder protobufEncoder = mock(ProtobufEncoder.class);
        final ByteBuf protoBuffer = Unpooled.copiedBuffer(new byte[3]);
        final ByteBuf wireFrameBuffer = Unpooled.copiedBuffer(new byte[5]);

        when(protobufEncoder.encode(any(VesEvent.class))).thenReturn(protoBuffer);
        when(wireFrameEncoder.encode(protoBuffer)).thenReturn(wireFrameBuffer);
        when(encodersFactoryMock.createProtobufEncoder(ByteBufAllocator.DEFAULT)).thenReturn(protobufEncoder);
        when(encodersFactoryMock.createWireFrameEncoder(ByteBufAllocator.DEFAULT)).thenReturn(wireFrameEncoder);

        // given
        final int messageStreamSize = 2;
        final Flux<VesEvent> messages = Flux.just(defaultVesEvent()).repeat(messageStreamSize - 1);

        // when
        final ByteBuf lastMessage = producerCore.encode(messages, ByteBufAllocator.DEFAULT).blockLast();

        // then
        verify(encodersFactoryMock).createProtobufEncoder(ByteBufAllocator.DEFAULT);
        verify(encodersFactoryMock).createWireFrameEncoder(ByteBufAllocator.DEFAULT);
        verify(protobufEncoder, times(messageStreamSize)).encode(any(VesEvent.class));
        verify(wireFrameEncoder, times(messageStreamSize)).encode(protoBuffer);

        assertThat(lastMessage).isNotNull();
        assertThat(lastMessage).isEqualTo(wireFrameBuffer);
    }
}
