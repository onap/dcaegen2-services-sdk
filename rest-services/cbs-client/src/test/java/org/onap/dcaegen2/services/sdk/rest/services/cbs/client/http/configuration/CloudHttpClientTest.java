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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.http.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/16/18
 */
class CloudHttpClientTest {

    private static final String SOMEURL = "http://someurl";
    private static final String DATA = "{}";
    private Gson gson = new Gson();
    private HttpClient webClient = mock(HttpClient.class);
    private HttpClientRequest requestBodyUriSpec = mock(HttpClientRequest.class);
    private HttpClientResponse responseSpec = mock(HttpClientResponse.class);

    @Test
    void shouldReturnJsonObjectOnGetCall() {
        //given
        //mockWebClientDependantObject();
        CloudHttpClient httpGetClient = new CloudHttpClient(webClient);
        //when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(DATA));

        //when/then
        StepVerifier.create(httpGetClient.callHttpGet(SOMEURL, JsonObject.class)).expectSubscription()
            .expectNext(gson.fromJson(DATA, JsonObject.class)).verifyComplete();
    }

    @Test
    void shouldReturnMonoErrorOnGetCall() {
        //given
        //mockWebClientDependantObject();
        CloudHttpClient httpGetClient = new CloudHttpClient(webClient);
        // when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("some wrong data"));

        //when/then
        StepVerifier.create(httpGetClient.callHttpGet(SOMEURL, JsonObject.class)).expectSubscription()
            .expectError(JsonSyntaxException.class).verify();
    }


    private void mockWebClientDependantObject() {
        doReturn(requestBodyUriSpec).when(webClient).get();
        //when(requestBodyUriSpec.(SOMEURL)).thenReturn(requestBodyUriSpec);
        //doReturn(responseSpec).when(requestBodyUriSpec).retrieve();
        //doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        //doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
    }
}