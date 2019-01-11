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
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.ct;

import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducerFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.ImmutableProducerOptions;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.ImmutableProducerOptions.Builder;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.domain.VesEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
public class SystemUnderTestWrapper {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private final DummyCollector collector = new DummyCollector();
    private HvVesProducer cut;
    private final Duration timeout;

    public SystemUnderTestWrapper(Duration timeout) {
        this.timeout = timeout;
    }

    public SystemUnderTestWrapper() {
        this(DEFAULT_TIMEOUT);
    }

    public void start() {
        start(createDefaultOptions());
    }

    public void start(ImmutableProducerOptions.Builder optionsBuilder) {
        InetSocketAddress collectorAddress = collector.start();
        cut = HvVesProducerFactory.create(
                optionsBuilder.addCollectorAddress(collectorAddress).build());
    }

    public void stop() {
        collector.stop();
    }

    public ByteBuf blockingSend(Flux<VesEvent> events) {
        events.transform(cut::send).subscribe();


        Mono.from(cut.send(events)).block();
        collector.blockUntilFirstClientIsHandled(timeout);
        return collector.dataFromFirstClient();
    }

    private Builder createDefaultOptions() {
        return ImmutableProducerOptions.builder();
    }

}
