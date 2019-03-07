/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;

class ConsumerReactiveHttpClientFactoryTest {

    private DmaapConsumerConfiguration dmaapConsumerConfiguration = mock(DmaapConsumerConfiguration.class);
    private DMaaPReactiveWebClientFactory reactiveWebClientFactory = mock(DMaaPReactiveWebClientFactory.class);
    private ConsumerReactiveHttpClientFactory httpClientFactory =
        new ConsumerReactiveHttpClientFactory(reactiveWebClientFactory);

    @Test
    void create_shouldReturnNotNullFactoryInstance() throws Exception {
        Assertions.assertNotNull(httpClientFactory.create(dmaapConsumerConfiguration));
        verify(reactiveWebClientFactory).build(dmaapConsumerConfiguration);
    }
}