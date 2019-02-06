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

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducer;
import org.onap.ves.VesEventOuterClass.VesEvent;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;


/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
public class HvVesProducerImpl implements HvVesProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HvVesProducerImpl.class);

    private final ProducerCore producerCore;
    private final EventLoopGroup workerGroup;
    private final EventProcessor processor;
    private HvVesClientBootstrap hvVesClientBootstrap;

    HvVesProducerImpl(ProducerCore producerCore, NioEventLoopGroup workerGroup, EventProcessor processor) {
        this.producerCore = producerCore;
        this.workerGroup = workerGroup;
        this.processor = processor;
    }

    void initializeWithBootstrap(HvVesClientBootstrap hvVesClientBootstrap) {
        this.hvVesClientBootstrap = hvVesClientBootstrap;
    }

    void shutdownClient() {
        workerGroup.shutdownGracefully();
    }

    @Override
    public Publisher<Void> send(Publisher<VesEvent> messages) {
        Channel channel = hvVesClientBootstrap.connect().channel();

        processor.setChannel(channel);
        producerCore.encode(messages, channel.alloc()).doOnNext(processor::addEvent).blockLast();

        return Mono.create((sink) -> {
            processor.startProcessingAndDoOnComplete(() -> {
                LOGGER.info("Finished handling messages");
                channel.close().addListener((future -> sink.success()));
            });
        });
    }

    public void reconnect() {
        LOGGER.info("Attempting to reconnect");

        Channel channel = hvVesClientBootstrap.connect().channel();

        processor.setChannel(channel);
    }

}
