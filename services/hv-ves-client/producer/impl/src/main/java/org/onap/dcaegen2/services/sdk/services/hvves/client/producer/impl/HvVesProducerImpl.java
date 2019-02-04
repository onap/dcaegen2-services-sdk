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

import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.PayloadType;
import org.onap.ves.VesEventOuterClass.VesEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;


/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
public class HvVesProducerImpl implements HvVesProducer {

    private final TcpClient tcpClient;
    private final ProducerCore producerCore;


    HvVesProducerImpl(TcpClient tcpClient, ProducerCore producerCore) {
        this.tcpClient = tcpClient;
        this.producerCore = producerCore;
    }

    @Override
    public @NotNull Mono<Void> send(Publisher<VesEvent> messages) {
        return tcpClient
                .handle((in, out) -> handle(messages, out))
                .connect()
                .then();
    }

    private Publisher<Void> handle(Publisher<VesEvent> messages, NettyOutbound outbound) {
        return outbound
                .send(producerCore.encode(messages, outbound.alloc()))
                .then();
    }

    @Override
    public @NotNull Publisher<Void> send(Publisher<ByteBuffer> messages, PayloadType payloadType) {
        return tcpClient
                .handle((in, out) -> handle(messages, payloadType, out))
                .connect()
                .then();
    }

    private Publisher<Void> handle(Publisher<ByteBuffer> messages, PayloadType payloadType,
            NettyOutbound outbound) {
        return outbound
                .send(producerCore.encode(messages, payloadType, outbound.alloc()))
                .then();
    }
}
