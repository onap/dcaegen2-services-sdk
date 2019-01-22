package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders;

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.utils.VesEvents;
import org.onap.ves.VesEventOuterClass.VesEvent;

public class ProtobufEncoderTest {

    private final ProtobufEncoder protobufEncoder = new ProtobufEncoder(ByteBufAllocator.DEFAULT);

    @Test
    void todo() {
        // given
        final VesEvent message = VesEvents.defaultVesEvent();

        // when
        final ByteBuf encodedMessage = protobufEncoder.encode(message);

        // then
        assertThat(encodedMessage.readableBytes()).isGreaterThan(0);
    }
}
