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

package org.onap.dcaegen2.services.sdk.rest.services.adapters.http;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.model.DmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.model.JsonBodyBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.ConnectionProvider;
import reactor.test.StepVerifier;

class CloudHttpClientIT {

    private static final int MAX_CONNECTIONS = 1;
    private static final String SAMPLE_STRING = "sampleString";
    private static final String SAMPLE_URL = "/sampleURL";
    private static final String JSON_BODY = "{\"correlationId\":\"NOKnhfsadhff\","
        + "\"ipaddress-v4\":\"256.22.33.155\", "
        + "\"ipaddress-v6\":\"200J:0db8:85a3:0000:0000:8a2e:0370:7334\"}";
    private static final ConnectionProvider connectionProvider = ConnectionProvider.fixed("test", MAX_CONNECTIONS);

    private DmaapModel dmaapModel = mock(DmaapModel.class);
    private JsonBodyBuilder<DmaapModel> jsonBodyBuilder = mock(JsonBodyBuilder.class);

    @Test
    void successfulPatchResponse() {
        DisposableServer server = createValidServer();
        HttpClient httpClient = createHttpClientForContextWithAddress(server, connectionProvider);
        CloudHttpClient cloudHttpClient = new CloudHttpClient(httpClient);

        when(jsonBodyBuilder.createJsonBody(dmaapModel)).thenReturn(JSON_BODY);
        Mono<Integer> content = cloudHttpClient.patch(SAMPLE_URL, createRequestDiagnosticContext(), createCustomHeaders(),
            jsonBodyBuilder, dmaapModel);

        StepVerifier.create(content)
            .expectNext(200)
            .expectComplete()
            .verify();
        server.disposeNow();
    }

    @Test
    void errorPatchRequest() {
        DisposableServer server = createInvalidServer();
        HttpClient httpClient = createHttpClientForContextWithAddress(server, connectionProvider);
        CloudHttpClient cloudHttpClient = new CloudHttpClient(httpClient);

        when(jsonBodyBuilder.createJsonBody(dmaapModel)).thenReturn(JSON_BODY);
        Mono<Integer> content = cloudHttpClient.patch(SAMPLE_URL, createRequestDiagnosticContext(), createCustomHeaders(),
            jsonBodyBuilder, dmaapModel);

        StepVerifier.create(content)
            .expectError()
            .verify();
        server.disposeNow();
    }

    /*
        @Test
        void post() {
            assertTrue(true);
        }
    */

    @Test
    void successfulGetResponse() {
        DisposableServer server = createValidServer();
        HttpClient httpClient = createHttpClientForContextWithAddress(server, connectionProvider);
        CloudHttpClient cloudHttpClient = new CloudHttpClient(httpClient);

        Mono<String> content = cloudHttpClient.get(SAMPLE_URL, String.class);

        StepVerifier.create(content)
            .expectNext(SAMPLE_STRING)
            .expectComplete()
            .verify();
        server.disposeNow();
    }
    @Test
    void errorGetRequest() {
        DisposableServer server = createInvalidServer();
        HttpClient httpClient = createHttpClientForContextWithAddress(server, connectionProvider);
        CloudHttpClient cloudHttpClient = new CloudHttpClient(httpClient);

        Mono<String> content = cloudHttpClient.get(SAMPLE_URL, String.class);

        StepVerifier.create(content)
            .expectError()
            .verify();
        server.disposeNow();
    }

    private Map<String, String> createCustomHeaders() {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("X_INVOCATION_ID", UUID.randomUUID().toString());

        return customHeaders;
    }

    private DisposableServer createValidServer() {
        Mono<String> response = Mono.just(SAMPLE_STRING);

        return HttpServer.create()
            .handle((req, resp) -> resp.sendString(response))
            .wiretap(true)
            .bindNow();
    }

    private DisposableServer createInvalidServer() {
        return HttpServer.create()
            .handle((req, resp) -> Mono.error(new Exception("returnError")))
            .wiretap(true)
            .bindNow();
    }

    private RequestDiagnosticContext createRequestDiagnosticContext() {
        return ImmutableRequestDiagnosticContext.builder()
            .invocationId(UUID.randomUUID()).requestId(UUID.randomUUID()).build();
    }

    private HttpClient createHttpClientForContextWithAddress(DisposableServer disposableServer,
        ConnectionProvider connectionProvider) {
        HttpClient client = connectionProvider == null? HttpClient.create() : HttpClient.create(connectionProvider);
        return client.addressSupplier(disposableServer::address).wiretap(true);
    }
}