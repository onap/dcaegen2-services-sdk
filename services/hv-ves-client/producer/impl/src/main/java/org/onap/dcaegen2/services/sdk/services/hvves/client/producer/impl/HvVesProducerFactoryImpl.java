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

import io.netty.channel.nio.NioEventLoopGroup;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducerFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ProducerOptions;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.encoders.EncodersFactory;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
public class HvVesProducerFactoryImpl extends HvVesProducerFactory {

    private final SslFactory sslFactory = new SslFactory();

    @Override
    protected @NotNull HvVesProducer createProducer(ProducerOptions options) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ProducerCore producerCore = new ProducerCore(new EncodersFactory());

        HvVesProducerImpl hvVesProducer = new HvVesProducerImpl(producerCore, workerGroup, new EventProcessor());
        hvVesProducer.initializeWithBootstrap(new HvVesClientBootstrap(options, sslFactory, hvVesProducer, workerGroup));
        return hvVesProducer;
    }

}
