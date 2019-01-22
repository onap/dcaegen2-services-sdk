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
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.ProtobufEncoder;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.WireFrameEncoder;
import org.onap.ves.VesEventOuterClass.VesEvent;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;


/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
public class HvVesProducerImpl implements HvVesProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HvVesProducerImpl.class);
    private final TcpClient tcpClient;

    HvVesProducerImpl(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    public @NotNull Mono<Void> send(Publisher<VesEvent> messages) {
        return tcpClient
            .handle((inbound, outbound) -> handle(outbound, messages))
            .connect().then();
    }

    private Publisher<Void> handle(NettyOutbound outbound, Publisher<VesEvent> messages) {
        // TODO remove comments after review
        // WireFrameEncoder has to be instantiated here, due to dependency on allocator
        // I dont think passing allocator as parameter to encode method would be a better idea
        // ProtobufEncoder could be declared as a class field, but it would break cohesion.
        // This approach shall affect the memory consumption in tiny extent

        final WireFrameEncoder wireFrameEncoder = new WireFrameEncoder(outbound.alloc());
        final ProtobufEncoder protobufEncoder = new ProtobufEncoder();
        final Flux<ByteBuf> encodedMessages = Flux.from(messages)
            .map(protobufEncoder::encode)
            .map(wireFrameEncoder::encode);

        return outbound.send(encodedMessages).then();
    }
}
