package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders;

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;

public class EncodersFactoryTest {

    private final EncodersFactory encodersFactory = new EncodersFactory();

    @Test
    public void todo() {
        // when
        final ProtobufEncoder protobufEncoder = encodersFactory.createProtobufEncoder(ByteBufAllocator.DEFAULT);
        final WireFrameEncoder wireFrameEncoder = encodersFactory.createWireFrameEncoder(ByteBufAllocator.DEFAULT);

        // then
        assertThat(protobufEncoder).isNotNull();
        assertThat(wireFrameEncoder).isNotNull();
    }
}
