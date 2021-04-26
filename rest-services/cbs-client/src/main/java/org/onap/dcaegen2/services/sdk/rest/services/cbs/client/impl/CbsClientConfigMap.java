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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClientSource;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class CbsClientConfigMap implements CbsClientSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientImpl.class);
    private final Supplier<JsonObject> jsonObjectSupplier;

    public CbsClientConfigMap(Supplier<JsonObject> jsonObjectSupplier) {
        this.jsonObjectSupplier = jsonObjectSupplier;
    }

    @Override
    public @NotNull Mono<JsonObject> get(CbsRequest request) {
        return Mono.fromSupplier(this.jsonObjectSupplier)
                .doOnNext(this::logConfigMapOutput);
    }

    private void logConfigMapOutput(JsonObject jsonObject) {
        LOGGER.info("Got successful output from ConfigMap file");
        LOGGER.debug("ConfigMap output: {}", jsonObject);
    }
}
