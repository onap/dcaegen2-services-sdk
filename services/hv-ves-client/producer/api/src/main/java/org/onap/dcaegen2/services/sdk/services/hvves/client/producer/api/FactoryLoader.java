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

import io.vavr.collection.Stream;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
final class FactoryLoader {

    private FactoryLoader() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FactoryLoader.class);

    static <T> T findInstance(Class<T> clazz) {
        return Stream.ofAll(ServiceLoader.load(clazz))
                .headOption()
                .peek(head -> LOGGER.info(
                        " Using {} as a {} implementation.", head.getClass().getName(), clazz.getSimpleName()))
                .getOrElseThrow(() -> new IllegalStateException(
                        "No " + clazz.getSimpleName() + " instances were configured. "
                                + "Are you sure you have runtime dependency on an implementation module?"));
    }
}
