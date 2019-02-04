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
import io.vavr.collection.HashSet;
import io.vavr.control.Try;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducerFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ImmutableProducerOptions;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ImmutableProducerOptions.Builder;
import org.onap.ves.VesEventOuterClass.VesEvent;
import reactor.core.publisher.Flux;

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

    public void startSecure() {
        start(ImmutableProducerOptions.builder()
                .securityKeys(ImmutableSecurityKeys.builder()
                        .keyStore(ImmutableSecurityKeysStore.of(resource("/client.p12").get()))
                        .keyStorePassword(Passwords.fromResource("/client.pass").get())
                        .trustStore(ImmutableSecurityKeysStore.of(resource("/trust.p12").get()))
                        .trustStorePassword(Passwords.fromResource("/trust.pass").get())
                        .build()));
    }

    public void start() {
        start(createDefaultOptions());
    }

    public void start(ImmutableProducerOptions.Builder optionsBuilder) {
        InetSocketAddress collectorAddress = collector.start();
        cut = HvVesProducerFactory.create(
                optionsBuilder.collectorAddresses(HashSet.of(collectorAddress)).build());
    }

    public void stop() {
        collector.stop();
    }

    public ByteBuf blockingSend(Flux<VesEvent> events) {
        events.transform(cut::send).subscribe();
        collector.blockUntilFirstClientIsHandled(timeout);
        return collector.dataFromFirstClient();
    }

    private Builder createDefaultOptions() {
        return ImmutableProducerOptions.builder();
    }

    private Try<Path> resource(String resource) {
        return Try.of(() -> Paths.get(Passwords.class.getResource(resource).toURI()));
    }

}
