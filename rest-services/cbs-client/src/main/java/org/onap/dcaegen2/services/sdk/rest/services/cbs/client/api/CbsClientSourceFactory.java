/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.CbsClientConfigMap;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.CbsClientRest;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.services.external.schema.manager.service.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

public class CbsClientSourceFactory implements CbsClientSource{
    private CbsClientSource cbsClientSource;
    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientSourceFactory.class);
    public CbsClientSourceFactory(
            RxHttpClient httpClient,
            String serviceName,
            InetSocketAddress cbsAddress,
            String protocol,
            String configMapFilePath) {
            try {
                LOGGER.info("Trying to load configuration from configMap file: {}", configMapFilePath);
                Supplier<JsonObject> jsonObjectSupplier = () -> new Gson().fromJson(new FileReader(configMapFilePath).getContent(), JsonObject.class);
                jsonObjectSupplier.get().isJsonObject();
                this.cbsClientSource = new CbsClientConfigMap(jsonObjectSupplier);
            } catch(Exception ex) {
                LOGGER.error("Error loading configuration from configMap file: {}", ex.getMessage());
                LOGGER.info("Fallback to Config Binding Service address: {}", cbsAddress);
                this.cbsClientSource = new CbsClientRest(httpClient, serviceName, cbsAddress, protocol);
            }
    }
    @Override
    public @NotNull Mono<JsonObject> get(CbsRequest request) {
        return this.cbsClientSource.get(request);
    }
}
