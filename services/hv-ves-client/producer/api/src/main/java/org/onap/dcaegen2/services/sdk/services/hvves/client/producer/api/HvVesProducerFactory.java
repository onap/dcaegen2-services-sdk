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

/**
 * Factory for High-Volume VES Producer.
 *
 * Usage:
 * <pre>
 *     HvVesProducer producer = HvVesProducerFactory.create(...)
 * </pre>
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since January 2019
 */
public abstract class HvVesProducerFactory {
    protected abstract HvVesProducer createProducer(ProducerOptions options);

    /**
     * Creates an instance of HvVesProducer. Under the hood it first loads the HvVesProducerFactory instance
     * using ServiceLoader facility. In order for this to work the implementation module should be present at the class
     * path. Otherwise a runtime exception is thrown.
     *
     * @param options the options to be used when creating a producer
     * @return non-null HvVesProducer instance
     */
    public static HvVesProducer create(ProducerOptions options) {
        return FactoryLoader.findInstance(HvVesProducerFactory.class).createProducer(options);
    }
}
