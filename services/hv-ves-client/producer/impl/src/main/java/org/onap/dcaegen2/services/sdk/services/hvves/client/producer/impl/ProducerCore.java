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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.vavr.control.Try;
import java.nio.ByteBuffer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.PayloadType;
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
        final ProtobufEncoder protobufEncoder = encodersFactory.createProtobufEncoder();
        return Flux.from(messages)
                .map(protobufEncoder::encode)
                .transform(payload -> encode(payload, PayloadType.PROTOBUF, allocator));
    }

    public Flux<ByteBuf> encode(Publisher<ByteBuffer> messages, PayloadType payloadType, ByteBufAllocator allocator) {
        final WireFrameEncoder wireFrameEncoder = encodersFactory.createWireFrameEncoder(allocator);
        return Flux.from(messages)
                .map(payload -> wireFrameEncoder.encode(payload, payloadType))
                .map(Try::get);
    }
}
