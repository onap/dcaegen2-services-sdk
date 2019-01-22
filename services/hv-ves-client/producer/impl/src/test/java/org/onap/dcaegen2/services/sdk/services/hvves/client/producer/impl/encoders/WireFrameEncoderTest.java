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
