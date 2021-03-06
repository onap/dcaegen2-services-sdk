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
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api;

import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.PayloadType;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ProducerOptions;
import org.onap.ves.VesEventOuterClass.VesEvent;
import org.reactivestreams.Publisher;

/**
 * <p>Main High Volume VES producer interface.</p>
 *
 * <p>Client code should use this interface for sending events to the endpoint configured when calling
 * {@link HvVesProducerFactory#create(ProducerOptions)}.</p>
 *
 * <p>Sample usage with <a href="https://projectreactor.io/">Project Reactor</a>:</p>
 *
 * <pre>
 *     ProducerOptions options = ImmutableProducerOptions.builder(). ... .build()
 *     HvVesProducer hvVes = {@link HvVesProducerFactory}.create(options);
 *
 *     Flux.just(msg1, msg2, msg3)
 *          .transform(hvVes::sendRaw)
 *          .subscribe();
 * </pre>
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @see org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.ImmutableProducerOptions
 * @since 1.1.1
 */
public interface HvVesProducer {

    /**
     * Send ves events to the collector.
     *
     * Returns a Publisher that completes when all the messages are sent. The returned Publisher fails with an error in
     * case of any problem with sending the messages.
     *
     * Each invocation of this method will yield a new TCP connection. It is recommended to call this method only once
     * feeding it with a stream of consecutive events.
     *
     * @param messages source of ves events to be sent
     * @return empty publisher which completes after ves events are sent or error occurs
     * @since 1.1.1
     */
    @NotNull Publisher<Void> send(Publisher<VesEvent> messages);

    /**
     * Send the specific type of messages as raw bytes to the collector.
     *
     * This is more generic version of @{@link #send(Publisher)},
     * that accepts raw payload and explicit message type.
     *
     * Should be used when sending messages in format different from VES Common Event Format.
     * As currently High-Volume VES Collector supports only VesEvent messages it is recommended to use the @{@link #send(Publisher)} method directly.
     *
     * Returns a Publisher that completes when all the messages are sent. The returned Publisher fails with an error in
     * case of any problem with sending the messages.
     *
     * Each invocation of this method will yield a new TCP connection. It is recommended to call this method only once
     * feeding it with a stream of consecutive events.
     *
     * @param messages source of raw messages to be sent
     * @param payloadType type of messages to be sent
     * @return empty publisher which completes after messages are sent or error occurs
     * @since 1.1.1
     */
    @NotNull Publisher<Void> sendRaw(Publisher<ByteBuffer> messages, PayloadType payloadType);
}
