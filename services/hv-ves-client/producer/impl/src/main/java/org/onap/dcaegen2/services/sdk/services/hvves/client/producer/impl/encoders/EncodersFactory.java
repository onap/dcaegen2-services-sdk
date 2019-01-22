package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders;

import io.netty.buffer.ByteBufAllocator;

public class EncodersFactory {

    public ProtobufEncoder createProtobufEncoder(ByteBufAllocator allocator) {
        return new ProtobufEncoder(allocator);
    }

    public WireFrameEncoder createWireFrameEncoder(ByteBufAllocator allocator) {
        return new WireFrameEncoder(allocator);
    }
}
