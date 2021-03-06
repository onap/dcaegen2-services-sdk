/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * =========================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================
 */
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.dummyservices.ImplementedService;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.dummyservices.NotImplementedService;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.dummyservices.ServiceWithMultipleImplementations;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 */
class FactoryLoaderTest {

    @Test
    void findInstance_shouldReturnInstance_ifItsConfigured() {
        final ImplementedService result = FactoryLoader.findInstance(ImplementedService.class);
        assertThat(result).isNotNull();
    }

    @Test
    void findInstance_shouldReturnArbitraryInstance_ifMultipleImplementationsArePresent() {
        final ServiceWithMultipleImplementations result = FactoryLoader.findInstance(ServiceWithMultipleImplementations.class);
        assertThat(result).isNotNull();
    }

    @Test
    void findInstance_shouldThrowException_whenNoImplementationsArePresent() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> FactoryLoader.findInstance(NotImplementedService.class))
                .withMessageContaining(NotImplementedService.class.getSimpleName());
    }
}

