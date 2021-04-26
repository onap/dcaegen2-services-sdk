/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientSource;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientSourceFactory;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import reactor.core.publisher.Mono;
import java.net.InetSocketAddress;


public class CbsClientImpl implements CbsClient {

    private final static String DEFAULT_CONFIG_MAP_FILE_PATH = "/app-config/application_config.yaml";
    private final RxHttpClient httpClient;
    private final String serviceName;
    private final InetSocketAddress cbsAddress;
    private final String protocol;
    private final String configMapFilePath;
    private CbsClientSource cbsClientSource = null;

    public CbsClientImpl(RxHttpClient httpClient, String serviceName, InetSocketAddress cbsAddress, String protocol) {
        this(httpClient, serviceName, cbsAddress, protocol, DEFAULT_CONFIG_MAP_FILE_PATH);
    }

    public CbsClientImpl(RxHttpClient httpClient, String serviceName, InetSocketAddress cbsAddress, String protocol, String configMapFilePath) {
        this.httpClient = httpClient;
        this.serviceName = serviceName;
        this.cbsAddress = cbsAddress;
        this.protocol = protocol;
        this.configMapFilePath = configMapFilePath;

    }

    @Override
    public @NotNull Mono<JsonObject> get(CbsRequest request) {
        if (this.cbsClientSource == null) {
            this.cbsClientSource = CbsClientSourceFactory.createCbsClientSource(
                    this.httpClient,
                    this.serviceName,
                    this.cbsAddress,
                    this.protocol,
                    this.configMapFilePath);
        }
        return this.cbsClientSource.get(request);
    }
}
