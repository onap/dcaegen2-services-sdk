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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ResponseTransformer;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.SimpleHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.ServiceLookupException;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
public class CbsLookup {

    private static final String CONSUL_JSON_SERVICE_ADDRESS = "ServiceAddress";
    private static final String CONSUL_JSON_SERVICE_PORT = "ServicePort";
    private final SimpleHttpClient httpClient;

    public CbsLookup(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Mono<InetSocketAddress> lookup(EnvProperties env) {
        return Mono.fromCallable(() -> createConsulUrl(env))
                .flatMap(this::fetchHttpData)
                .flatMap(this::firstService)
                .map(this::parseServiceEntry);
    }

    private String createConsulUrl(EnvProperties env) {
        return String.format("http://%s:%s/v1/catalog/service/%s", env.consulHost(), env.consulPort(), env.cbsName());
    }

    private Mono<JsonArray> fetchHttpData(String consulUrl) {
        return httpClient.call(
                ImmutableHttpRequest.builder()
                        .method(HttpMethod.GET)
                        .url(consulUrl)
                        .build(),
                ResponseTransformer.fromJson(JsonArray.class)
        );
    }

    private Mono<JsonObject> firstService(JsonArray services) {
        return services.size() == 0
                ? Mono.error(new ServiceLookupException("Consul server did not return any service with given name"))
                : Mono.just(services.get(0).getAsJsonObject());
    }

    private InetSocketAddress parseServiceEntry(JsonObject service) {
        return InetSocketAddress.createUnresolved(
                service.get(CONSUL_JSON_SERVICE_ADDRESS).getAsString(),
                service.get(CONSUL_JSON_SERVICE_PORT).getAsInt());
    }

}
