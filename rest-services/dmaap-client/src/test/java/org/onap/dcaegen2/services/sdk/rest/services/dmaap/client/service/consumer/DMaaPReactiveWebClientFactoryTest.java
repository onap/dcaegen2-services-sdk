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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.handler.ssl.SslContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.utlis.SecurityKeysUtil;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;

import java.net.URISyntaxException;
import java.nio.file.Paths;


/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/5/18
 */
class DMaaPReactiveWebClientFactoryTest {

    private static final String KEY_STORE_FILE_PATH = testResourceToPath("/org.onap.dcae.jks");
    private static final String KEY_STORE_PASS_FILE_PATH = testResourceToPath("/keystore.password");
    private static final String TRUST_STORE_FILE_PATH = testResourceToPath("/org.onap.dcae.trust.jks");
    private static final String TRUST_STORE_PASS_FILE_PATH = testResourceToPath("/truststore.password");
    private SslFactory sslFactory = mock(SslFactory.class);
    private SslContext dummySslContext = mock(SslContext.class);
    private DMaaPReactiveWebClientFactory webClientFactory = new DMaaPReactiveWebClientFactory(sslFactory);
    private ArgumentCaptor<SecurityKeys> securityKeysArgumentCaptor = ArgumentCaptor
            .forClass(SecurityKeys.class);

    @Test
    void builder_shouldBuildDMaaPReactiveWebClientwithInsecureSslContext(){
        //given
        DmaapConsumerConfiguration dmaapConsumerConfiguration = givenDmaapConfigurationWithSslDisabled();

        //when
        CloudHttpClient dmaapReactiveWebClient = webClientFactory.build(dmaapConsumerConfiguration);

        //then
        assertNotNull(dmaapReactiveWebClient);
        verify(sslFactory).createInsecureClientContext();
    }

    @Test
    void builder_shouldBuildDMaaPReactiveWebClientwithSecureSslContext(){
        //given
        DmaapConsumerConfiguration dmaapConsumerConfiguration = givenDmaapConfigurationWithSslEnabled();
        SecurityKeys givenKeys = SecurityKeysUtil.fromDmappCustomConfig(dmaapConsumerConfiguration);

        //when
        CloudHttpClient dmaapReactiveWebClient = webClientFactory.build(dmaapConsumerConfiguration);

        //then
        assertNotNull(dmaapReactiveWebClient);

        verify(sslFactory).createSecureClientContext(securityKeysArgumentCaptor.capture());

        SecurityKeys capturedKeys = securityKeysArgumentCaptor.getValue();

        assertEquals(capturedKeys.keyStore().path(), givenKeys.keyStore().path());
        assertEquals(capturedKeys.keyStorePassword().toString(), givenKeys.keyStorePassword().toString());
        assertEquals(capturedKeys.trustStore().path(), givenKeys.trustStore().path());
        assertEquals(capturedKeys.trustStorePassword().toString(), givenKeys.trustStorePassword().toString());
    }

    private DmaapConsumerConfiguration givenDmaapConfigurationWithSslDisabled(){
        DmaapConsumerConfiguration dmaapConsumerConfiguration = mock(DmaapConsumerConfiguration.class);
        when(dmaapConsumerConfiguration.enableDmaapCertAuth()).thenReturn(false);
        when(sslFactory.createInsecureClientContext()).thenReturn(dummySslContext);
        return dmaapConsumerConfiguration;
    }

    private DmaapConsumerConfiguration givenDmaapConfigurationWithSslEnabled(){
        DmaapConsumerConfiguration dmaapConsumerConfiguration = mock(DmaapConsumerConfiguration.class);

        when(dmaapConsumerConfiguration.enableDmaapCertAuth()).thenReturn(true);
        when(dmaapConsumerConfiguration.keyStorePath()).thenReturn(KEY_STORE_FILE_PATH);
        when(dmaapConsumerConfiguration.keyStorePasswordPath()).thenReturn(KEY_STORE_PASS_FILE_PATH);
        when(dmaapConsumerConfiguration.trustStorePath()).thenReturn(TRUST_STORE_FILE_PATH);
        when(dmaapConsumerConfiguration.trustStorePasswordPath()).thenReturn(TRUST_STORE_PASS_FILE_PATH);

        when(sslFactory.createSecureClientContext(any(SecurityKeys.class))).thenReturn(dummySslContext);

        return dmaapConsumerConfiguration;
    }

    private static String testResourceToPath(String resource) {
        try {
            return Paths.get(DMaaPReactiveWebClientFactoryTest.class.getResource(resource).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed resolving test resource path", e);
        }
    }
}