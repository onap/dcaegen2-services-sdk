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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ProducerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HvVesClientBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(HvVesClientBootstrap.class);
    private final Bootstrap actualBootstrap;

    HvVesClientBootstrap(ProducerOptions options, SslFactory sslFactory, HvVesProducerImpl producer, EventLoopGroup workerGroup) {
        this.actualBootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addFirst(new ReconnectionHandler(producer));
                    }
                })
                .remoteAddress(options.collectorAddresses().head());

        if (options.securityKeys() == null) {
            LOGGER.warn("Using insecure connection");
        } else {
            LOGGER.info("Using secure tunnel");
            final SslContext ctx = sslFactory.createSecureContext(options.securityKeys()).get();
//TODO: something along     this.actualBootstrap.secure(ssl -> ssl.sslContext(ctx));  ??
        }
    }

    ChannelFuture connect() {
        return actualBootstrap.connect();
    }
}
