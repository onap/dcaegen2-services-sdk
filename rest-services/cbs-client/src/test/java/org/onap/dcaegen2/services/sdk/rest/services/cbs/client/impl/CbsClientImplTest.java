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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsClientImplTest {
    private final CloudHttpClient httpClient = mock(CloudHttpClient.class);

    @Test
    void shouldFetchUsingProperUrl() {
        // given
        InetSocketAddress cbsAddress = InetSocketAddress.createUnresolved("cbshost", 6969);
        String serviceName = "dcaegen2-ves-collector";
        final CbsClientImpl cut = CbsClientImpl.create(httpClient, cbsAddress, serviceName);
        final JsonObject httpResponse = new JsonObject();
        given(httpClient.get(anyString(), any(RequestDiagnosticContext.class), any(Class.class))).willReturn(Mono.just(httpResponse));
        RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final JsonObject result = cut.get(diagnosticContext).block();

        // then
        final String expectedUrl = "http://cbshost:6969/service_component/dcaegen2-ves-collector";
        verify(httpClient).get(expectedUrl, diagnosticContext, JsonObject.class);
        assertThat(result).isSameAs(httpResponse);
    }
}