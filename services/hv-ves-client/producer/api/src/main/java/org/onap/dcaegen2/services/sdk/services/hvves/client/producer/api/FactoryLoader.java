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

import java.util.Iterator;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since January 2019
 */
class FactoryLoader {

    private static Logger logger = LoggerFactory.getLogger(FactoryLoader.class);

    static <T> T findInstance(Class<T> clazz) {
        Iterator<T> instances = ServiceLoader.load(clazz).iterator();
        if (instances.hasNext()) {
            final T head = instances.next();
            if (instances.hasNext()) {
                logger.warn("Found more than one implementation of {} on the class path. Using {}.",
                        clazz.getName(), head.getClass().getName());
            }
            return head;
        } else {
            throw new IllegalStateException(
                    "No " + clazz.getName() + " instances were configured. Are you sure you have runtime dependency on an implementation module?");
        }
    }
}
