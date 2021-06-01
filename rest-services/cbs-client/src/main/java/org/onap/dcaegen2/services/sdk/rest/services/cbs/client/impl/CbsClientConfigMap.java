/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
 * Copyright (C) 2021 Wipro Limited.
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;

import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.services.common.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Mono;

public class CbsClientConfigMap implements CbsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientConfigMap.class);
    private final String configMapFilePath;
    private final String policySyncFilePath;
    private final String appName;

    public CbsClientConfigMap(String configMapFilePath, String policySyncFilePath, String appName) {
        this.configMapFilePath = configMapFilePath;
        this.policySyncFilePath = policySyncFilePath;
        this.appName = appName;
    }

    @Override
    public @NotNull Mono<JsonObject> get(CbsRequest request) {
        Mono<JsonObject> configJsonMono =
                Mono.just(this.loadConfigMapFile()).map(CbsClientEnvironmentParsing::processEnvironmentVariables);
        if (this.readPolicySyncFile(request)) {

            return configJsonMono.map(this::loadPolicySyncFile).doOnNext(this::logConfigMapOutput);
        }
        return configJsonMono.doOnNext(this::logConfigMapOutput);
    }

    public boolean verifyConfigMapFile() {
        try {
            LOGGER.info("Trying to load configuration from configMap file: {}", configMapFilePath);
            this.loadConfigMapFile().isJsonObject();
            return true;
        } catch (Exception ex) {
            this.logConfigMapError(ex);
            return false;
        }
    }

    private JsonObject loadConfigMapFile() {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(gson.toJson(this.loadYamlConfigMapFile(), LinkedHashMap.class), JsonObject.class);
    }

    private Object loadYamlConfigMapFile() {
        return new Yaml().load(new FileReader(configMapFilePath).getContent());
    }

    private void logConfigMapOutput(JsonObject jsonObject) {
        LOGGER.info("Got successful output from ConfigMap file");
        LOGGER.debug("ConfigMap output: {}", jsonObject);
    }

    private void logConfigMapError(Exception ex) {
        LOGGER.error("Error loading configuration from configMap file: {}", ex.getMessage());
    }

    private boolean readPolicySyncFile(CbsRequest request) {
        return request.requestPath()
                      .getForService(appName)
                      .contains("service_component_all");
    }

    private JsonObject loadPolicySyncFile(JsonObject configJsonObject) {

        try {

            if (new FileReader(policySyncFilePath).doesFileExists()) {
                LOGGER.info("PolicySync file is present");
                Gson gson = new GsonBuilder().create();
                JsonObject policyJsonObject = gson.fromJson(this.loadJsonStringPolicySyncFile(), JsonObject.class);
                policyJsonObject.add("config", configJsonObject);
                return policyJsonObject;
            }
            LOGGER.info("PolicySync file does not exist");
            JsonObject policyJsonObject = new JsonObject();
            policyJsonObject.add("config", configJsonObject);
            return policyJsonObject;

        } catch (Exception ex) {
            LOGGER.info("PolicySync file does not contain a valid json");
            JsonObject policyJsonObject = new JsonObject();
            policyJsonObject.add("config", configJsonObject);
            return policyJsonObject;
        }

    }

    private String loadJsonStringPolicySyncFile() {
        return new FileReader(policySyncFilePath).getContent();
    }
}
