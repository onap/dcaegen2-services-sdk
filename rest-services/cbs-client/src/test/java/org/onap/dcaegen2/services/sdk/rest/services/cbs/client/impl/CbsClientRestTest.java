/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
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

import com.google.gson.JsonObject;
import io.vavr.collection.HashMultimap;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import java.net.InetSocketAddress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsClientRestTest {
    private final RxHttpClient httpClient = mock(RxHttpClient.class);

    @Test
    void shouldFetchUsingProperUrl() {
        // given
        InetSocketAddress cbsAddress = InetSocketAddress.createUnresolved("cbshost", 6969);
        String serviceName = "dcaegen2-ves-collector";
        final CbsClient cut = new CbsClientRest(httpClient, serviceName, cbsAddress, "http");
        final HttpResponse httpResponse = ImmutableHttpResponse.builder()
                .url("http://xxx")
                .statusCode(200)
                .rawBody("{}".getBytes())
                .headers(HashMultimap.withSeq().empty())
                .build();
        given(httpClient.call(any(HttpRequest.class))).willReturn(Mono.just(httpResponse));
        RequestDiagnosticContext diagnosticContext = RequestDiagnosticContext.create();

        // when
        final JsonObject result = cut.get(CbsRequests.getConfiguration(diagnosticContext)).block();

        // then
        final String expectedUrl = "http://cbshost:6969/service_component/dcaegen2-ves-collector";
        verify(httpClient).call(ImmutableHttpRequest.builder()
                .method(HttpMethod.GET)
                .url(expectedUrl)
                .diagnosticContext(diagnosticContext)
                .build());
        assertThat(result.toString()).isEqualTo(httpResponse.bodyAsString());
    }
}
