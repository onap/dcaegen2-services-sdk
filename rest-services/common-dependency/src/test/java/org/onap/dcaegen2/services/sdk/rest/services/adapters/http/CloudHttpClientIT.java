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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.ConnectionProvider;
import reactor.test.StepVerifier;

class CloudHttpClientIT {

    private static final String SAMPLE_URL = "/sampleURL";
    private static final int MAX_CONNECTIONS = 1;
    private static final ConnectionProvider connectionProvider = ConnectionProvider.fixed("test", MAX_CONNECTIONS);

    @Test
    public void post() {
        assertTrue(true);
    }

    @Test
    public void patch() {
        assertTrue(true);
    }

    @Test
    void successfulGetResponse() {
        String sampleString = "sampleString";
        Mono<String> response = Mono.just(sampleString);
        DisposableServer server =
            HttpServer.create()
                .handle((req, resp) -> resp.sendString(response))
                .wiretap(true)
                .bindNow();
        HttpClient httpClient = createHttpClientForContextWithAddress(server, connectionProvider);
        CloudHttpClient cloudHttpClient = new CloudHttpClient(httpClient);

        Mono<String> content = cloudHttpClient.get(SAMPLE_URL, String.class);

        StepVerifier.create(content)
            .expectNext(sampleString)
            .expectComplete()
            .verify();
        server.disposeNow();
    }

    @Test
    void errorGetRequest() {
        DisposableServer server =
            HttpServer.create()
                .handle((req, resp) -> Mono.error(new Exception("returnError")))
                .wiretap(true)
                .bindNow();
        HttpClient httpClient = createHttpClientForContextWithAddress(server, connectionProvider);
        CloudHttpClient cloudHttpClient = new CloudHttpClient(httpClient);

        Mono<String> content = cloudHttpClient.get(SAMPLE_URL, String.class);

        StepVerifier.create(content)
            .expectError()
            .verify();
        server.disposeNow();
    }

    private HttpClient createHttpClientForContextWithAddress(DisposableServer context) {
        return createHttpClientForContextWithAddress(context, null);
    }

    private HttpClient createHttpClientForContextWithAddress(DisposableServer disposableServer,
        ConnectionProvider connectionProvider) {
        HttpClient client = connectionProvider == null? HttpClient.create() : HttpClient.create(connectionProvider);
        return client.addressSupplier(disposableServer::address).wiretap(true);
    }
}