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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.adapters.CloudHttpClient;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
public class CbsLookup {

    private final CloudHttpClient httpClient;

    public CbsLookup(CloudHttpClient httpClient) {
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
        return httpClient.callHttpGet(consulUrl, JsonArray.class);
    }

    private Mono<JsonObject> firstService(JsonArray services) {
        return services.size() == 0
                ? Mono.empty()
                : Mono.just(services.get(0).getAsJsonObject());
    }

    private InetSocketAddress parseServiceEntry(JsonObject service) {
        return InetSocketAddress.createUnresolved(
                service.get("ServiceAddress").getAsString(),
                service.get("ServicePort").getAsInt());
    }

}

