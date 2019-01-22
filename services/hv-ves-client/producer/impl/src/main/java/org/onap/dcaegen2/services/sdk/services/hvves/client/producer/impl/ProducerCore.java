package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.EncodersFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.ProtobufEncoder;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.WireFrameEncoder;
import org.onap.ves.VesEventOuterClass.VesEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;


/**
 * @author <a href="mailto:jakub.dudycz@nokia.com">Jakub Dudycz</a>
 */
public class ProducerCore {

    private final EncodersFactory encodersFactory;

    public ProducerCore(EncodersFactory encodersFactory) {
        this.encodersFactory = encodersFactory;
    }

    public Flux<ByteBuf> encode(Publisher<VesEvent> messages, ByteBufAllocator allocator) {
        final WireFrameEncoder wireFrameEncoder = encodersFactory.createWireFrameEncoder(allocator);
        final ProtobufEncoder protobufEncoder = encodersFactory.createProtobufEncoder(allocator);
        return Flux.from(messages)
            .map(protobufEncoder::encode)
            .map(wireFrameEncoder::encode);
    }
}
