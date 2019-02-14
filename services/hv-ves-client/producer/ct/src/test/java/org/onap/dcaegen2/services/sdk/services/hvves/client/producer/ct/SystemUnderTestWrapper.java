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
import io.netty.handler.ssl.SslContext;
import io.vavr.collection.HashSet;
import io.vavr.control.Try;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;

import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducer;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.HvVesProducerFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ImmutableProducerOptions;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ImmutableProducerOptions.Builder;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ImmutableWireFrameVersion;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.WireFrameVersion;
import org.onap.ves.VesEventOuterClass.VesEvent;
import reactor.core.publisher.Flux;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
public class SystemUnderTestWrapper {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    private static final String TRUST_CERT_PATH = "/trust.p12";
    private static final String TRUST_PASSWORD_PATH = "/trust.pass";
    private static final String CLIENT_CERT_PATH = "/client.p12";
    private static final String CLIENT_PASSWORD_PATH = "/client.pass";
    private static final String SERVER_CERT_PATH = "/server.p12";
    private static final String SERVER_PASSWORD_PATH = "/server.pass";

    private DummyCollector collector;
    private HvVesProducer cut;
    private final Duration timeout;
    private final SslFactory sslFactory = new SslFactory();

    public SystemUnderTestWrapper(Duration timeout) {
        this.timeout = timeout;
    }

    public SystemUnderTestWrapper() {
        this(DEFAULT_TIMEOUT);
    }

    public void startSecure() {
        collector = createCollectorWithEnabledSSL();

        final ImmutableSecurityKeys producerSecurityKeys = ImmutableSecurityKeys.builder()
                .keyStore(ImmutableSecurityKeysStore.of(resource(CLIENT_CERT_PATH).get()))
                .keyStorePassword(Passwords.fromResource(CLIENT_PASSWORD_PATH))
                .trustStore(ImmutableSecurityKeysStore.of(resource(TRUST_CERT_PATH).get()))
                .trustStorePassword(Passwords.fromResource(TRUST_PASSWORD_PATH))
                .build();
        start(ImmutableProducerOptions.builder().securityKeys(producerSecurityKeys));
    }

    public void start() {
        collector = new DummyCollector(Optional.empty());
        start(createDefaultOptions());
    }

    public void start(ImmutableProducerOptions.Builder optionsBuilder) {
        InetSocketAddress collectorAddress = collector.start();
        WireFrameVersion WTPVersion = ImmutableWireFrameVersion.builder().build();
        cut = HvVesProducerFactory.create(
                optionsBuilder.collectorAddresses(HashSet.of(collectorAddress))
                        .wireFrameVersion(WTPVersion).build());
    }

    public void stop() {
        collector.stop();
    }

    public ByteBuf blockingSend(Flux<VesEvent> events) {
        events.transform(cut::send).subscribe();
        collector.blockUntilFirstClientIsHandled(timeout);
        return collector.dataFromFirstClient();
    }

    private DummyCollector createCollectorWithEnabledSSL() {
        final ImmutableSecurityKeys collectorSecurityKeys = ImmutableSecurityKeys.builder()
                .keyStore(ImmutableSecurityKeysStore.of(resource(SERVER_CERT_PATH).get()))
                .keyStorePassword(Passwords.fromResource(SERVER_PASSWORD_PATH))
                .trustStore(ImmutableSecurityKeysStore.of(resource(TRUST_CERT_PATH).get()))
                .trustStorePassword(Passwords.fromResource(TRUST_PASSWORD_PATH))
                .build();
        final SslContext collectorSslContext = sslFactory.createSecureServerContext(collectorSecurityKeys);
        return new DummyCollector(Optional.of(collectorSslContext));
    }

    private Builder createDefaultOptions() {
        return ImmutableProducerOptions.builder();
    }

    private Try<Path> resource(String resource) {
        return Try.of(() -> Paths.get(Passwords.class.getResource(resource).toURI()));
    }

}
