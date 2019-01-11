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

import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.domain.VesEvent;
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
 *     ProducerOptions options = {@link ImmutableProducerOptions}.builder(). ... .build()
 *     HvVesProducer hvVes = {@link HvVesProducerFactory}.create(options);
 *
 *     Flux.just(msg1, msg2, msg3)
 *          .transform(hvVes::send)
 *          .subscribe();
 * </pre>
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.2.1
 */
@FunctionalInterface
public interface HvVesProducer {

    /**
     * Send the messages to the collector.
     *
     * Returns a Publisher that completes when all the messages are sent. The returned Producer fails with an error in
     * case of any problem with sending the messages.
     *
     * @param messages source of the messages to be sent
     * @return empty publisher which completes after messages are sent or error occurs
     * @since 1.2.1
     */
    @NotNull Publisher<Void> send(Publisher<VesEvent> messages);
}
