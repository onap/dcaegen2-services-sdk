/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.AaiClientConfigurations.insecureConfiguration;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.AaiClientConfigurations.secureConfiguration;

import io.netty.handler.ssl.SslContext;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.SslFactory;

class AaiHttpClientFactoryTest {

    private SslFactory sslFactory;

    @BeforeEach
    void setup() {
        this.sslFactory = Mockito.mock(SslFactory.class);
    }

    @Test
    void createRequestDiagnosticContext_shouldReturnNonNullContext() {
        assertNotNull(AaiHttpClientFactory.createRequestDiagnosticContext());
    }

    @Test
    void build_onSecureConfigurationProvided_shouldReturnSecureClient() throws SSLException {
        when(sslFactory.createSecureClientContext(any())).thenReturn(SslContext.newClientContext());
        AaiHttpClientFactory cut = new AaiHttpClientFactory(secureConfiguration(), sslFactory);

        cut.build();

        verify(sslFactory).createSecureClientContext(any(SecurityKeys.class));
        verify(sslFactory, never()).createInsecureClientContext();
    }

    @Test
    void build_onInsecureConfigurationProvided_shouldReturnInsecureClient() throws SSLException {
        when(sslFactory.createInsecureClientContext()).thenReturn(SslContext.newClientContext());
        AaiHttpClientFactory cut = new AaiHttpClientFactory(insecureConfiguration(), sslFactory);

        cut.build();

        verify(sslFactory).createInsecureClientContext();
        verify(sslFactory, never()).createSecureClientContext(any(SecurityKeys.class));
    }
}
