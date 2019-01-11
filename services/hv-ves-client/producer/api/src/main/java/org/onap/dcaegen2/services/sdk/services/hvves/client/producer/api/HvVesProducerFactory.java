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

/**
 * <p>
 *     Factory for High-Volume VES Producer.
 * </p>
 *
 * <p>
 *     Sample usage:
 * </p>
 *
 * <pre>
 *     {@link HvVesProducer} producer = HvVesProducerFactory.create(
 *          {@link ImmutableProducerOptions}.builder().
 *              ...
 *              .build())
 * </pre>
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.2.1
 */
public abstract class HvVesProducerFactory {

    /**
     * Must be implemented by implementing classes. Should not be used directly by client code. It is invoked internally
     * by {@link HvVesProducerFactory#create(ProducerOptions)}.
     *
     * @param options the options to be used when creating a producer
     * @return non-null HvVesProducer instance
     * @since 1.2.1
     */
    protected abstract @NotNull HvVesProducer createProducer(ProducerOptions options);

    /**
     * Creates an instance of {@link HvVesProducer}. Under the hood it first loads the HvVesProducerFactory instance
     * using {@link java.util.ServiceLoader} facility. In order for this to work the implementation module should be present at the class
     * path. Otherwise a runtime exception is thrown.
     *
     * @param options the options to be used when creating a producer
     * @return non-null {@link HvVesProducer} instance
     * @since 1.2.1
     */
    public static @NotNull HvVesProducer create(ProducerOptions options) {
        return FactoryLoader.findInstance(HvVesProducerFactory.class).createProducer(options);
    }
}
