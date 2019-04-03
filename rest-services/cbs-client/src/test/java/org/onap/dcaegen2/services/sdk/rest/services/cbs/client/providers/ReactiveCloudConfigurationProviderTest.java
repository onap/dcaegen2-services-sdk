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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableEnvProperties;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/15/18
 */
class ReactiveCloudConfigurationProviderTest {

    private static final Gson gson = new Gson();
    private static final String configBindingService =
            "[{\"ID\":\"9c8dd674-34ce-7049-d318-e98d93a64303\",\"Node\""
                    + ":\"dcae-bootstrap\",\"Address\":\"10.42.52.82\",\"Datacenter\":\"dc1\",\"TaggedAddresses\":"
                    + "{\"lan\":\"10.42.52.82\",\"wan\":\"10.42.52.82\"},\"NodeMeta\":{\"consul-network-segment\":\"\"},"
                    + "\"ServiceID\":\"dcae-cbs1\",\"ServiceName\":\"config-binding-service\",\"ServiceTags\":[],"
                    + "\"ServiceAddress\":\"config-binding-service\",\"ServicePort\":10000,\"ServiceEnableTagOverride\":false,"
                    + "\"CreateIndex\":14352,\"ModifyIndex\":14352},{\"ID\":\"35c6f540-a29c-1a92-23b0-1305bd8c81f5\",\"Node\":"
                    + "\"dev-consul-server-1\",\"Address\":\"10.42.165.51\",\"Datacenter\":\"dc1\",\"TaggedAddresses\":"
                    + "{\"lan\":\"10.42.165.51\",\"wan\":\"10.42.165.51\"},\"NodeMeta\":{\"consul-network-segment\":\"\"},"
                    + "\"ServiceID\":\"dcae-cbs1\",\"ServiceName\":\"config-binding-service\",\"ServiceTags\":[],"
                    + "\"ServiceAddress\":\"config-binding-service\",\"ServicePort\":10000,\"ServiceEnableTagOverride\":false,"
                    + "\"CreateIndex\":803,\"ModifyIndex\":803}]";
    private static final JsonArray configBindingServiceJson = gson
            .fromJson(configBindingService, JsonArray.class);
    private static final String configurationMock = "{\"test\":1}";
    private static final JsonObject configurationJsonMock = gson
            .fromJson(configurationMock, JsonObject.class);

    private EnvProperties envProperties = ImmutableEnvProperties.builder()
            .appName("dcae-prh")
            .cbsName("config-binding-service")
            .consulHost("consul")
            .consulPort(8500)
            .build();

    private final RxHttpClient httpClient = mock(RxHttpClient.class);

    @Test
    void shouldReturnPrhConfiguration() {
        // given
        ReactiveCloudConfigurationProvider provider = new ReactiveCloudConfigurationProvider(
                httpClient);

        HttpResponse response = mock(HttpResponse.class);

        //when
        when(httpClient.call(any(HttpRequest.class))).thenReturn(Mono.just(response));
        when(response.bodyAsJson(JsonArray.class)).thenReturn(configBindingServiceJson);
        when(response.bodyAsJson(JsonObject.class)).thenReturn(configurationJsonMock);


        //then
        StepVerifier.create(provider.callForServiceConfigurationReactive(envProperties))
                .expectSubscription()
                .expectNext(configurationJsonMock).verifyComplete();
    }

    @Test
    void shouldRequestCorrectUrl(){
        // given
        String consulRequestUrl = "http://consul:8500/v1/catalog/service/config-binding-service";
        String configRequestUrl = "http://config-binding-service:10000/service_component/dcae-prh";
        ReactiveCloudConfigurationProvider provider = new ReactiveCloudConfigurationProvider(
                httpClient);

        HttpResponse response = mock(HttpResponse.class);

        //when
        when(httpClient.call(any(HttpRequest.class))).thenReturn(Mono.just(response));
        when(response.bodyAsJson(JsonArray.class)).thenReturn(configBindingServiceJson);
        when(response.bodyAsJson(JsonObject.class)).thenReturn(configurationJsonMock);


        //then
        StepVerifier.create(provider.callForServiceConfigurationReactive(envProperties))
                .expectSubscription()
                .expectNext(configurationJsonMock).verifyComplete();


        ArgumentCaptor<HttpRequest> httpReq = ArgumentCaptor
                .forClass(HttpRequest.class);
        verify(httpClient, times(2)).call(httpReq.capture());

        List<HttpRequest> allRequests = httpReq.getAllValues();
        assertThat(allRequests.get(0).url()).isEqualTo(consulRequestUrl);
        assertThat(allRequests.get(1).url()).isEqualTo(configRequestUrl);
    }

    @Test
    void shouldReturnMonoErrorWhenConsuleDoesntHaveConfigBindingServiceEntry() {
        // given
        ReactiveCloudConfigurationProvider provider = new ReactiveCloudConfigurationProvider(
                httpClient);

        JsonArray emptyArray = gson.fromJson("[]", JsonArray.class);

        HttpResponse response = mock(HttpResponse.class);

        //when
        when(httpClient.call(any(HttpRequest.class))).thenReturn(Mono.just(response));
        when(response.bodyAsJson(JsonArray.class)).thenReturn(emptyArray);


        //then
        StepVerifier.create(provider.callForServiceConfigurationReactive(envProperties))
                .expectSubscription()
                .expectError(IllegalStateException.class).verify();
    }
}