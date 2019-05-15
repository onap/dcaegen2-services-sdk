/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.producer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapPublisherConfiguration;

import java.net.URISyntaxException;
import java.nio.file.Paths;


class DmaaPRestTemplateFactoryTest {

    private static final String KEY_STORE_FILE_PATH = testResourceToPath("/org.onap.dcae.jks");
    private static final String KEYSTORE_PASSWORD_FILE_PATH = testResourceToPath("/keystore.password");
    private static final String TRUSTSTORE_PASSWORD_FILE_PATH = testResourceToPath("/truststore.password");
    private static final String TRUST_STORE_FILE_PATH = testResourceToPath("/org.onap.dcae.trust.jks");
    private DmaapPublisherConfiguration publisherConfiguration = mock(DmaapPublisherConfiguration.class);
    private DmaaPRestTemplateFactory factory = new DmaaPRestTemplateFactory();

    @Test
    void build_shouldCreateRestTemplateWithoutSslConfiguration(){
        when(publisherConfiguration.enableDmaapCertAuth()).thenReturn(false);

        Assertions.assertNotNull(factory.build(publisherConfiguration));
    }

    @Test
    void build_shouldCreateRestTemplateWithSslConfiguration() {
        when(publisherConfiguration.enableDmaapCertAuth()).thenReturn(true);
        when(publisherConfiguration.keyStorePath()).thenReturn(KEY_STORE_FILE_PATH);
        when(publisherConfiguration.keyStorePasswordPath()).thenReturn(
                KEYSTORE_PASSWORD_FILE_PATH);
        when(publisherConfiguration.trustStorePath()).thenReturn(TRUST_STORE_FILE_PATH);
        when(publisherConfiguration.trustStorePasswordPath()).thenReturn(
                TRUSTSTORE_PASSWORD_FILE_PATH);

        Assertions.assertNotNull(factory.build(publisherConfiguration));
    }

    private static String testResourceToPath(String resource) {
        try {
            return Paths.get(DmaaPRestTemplateFactoryTest.class.getResource(resource).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed resolving test resource path", e);
        }
    }
}