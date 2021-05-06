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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.CbsClientConfigMapException;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.services.common.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Mono;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CbsClientConfigMap implements CbsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsClientConfigMap.class);
    private final String configMapFilePath;
    private final Pattern shellEnvPattern = Pattern.compile("\\$\\{(.+?)}");

    public CbsClientConfigMap (String configMapFilePath) {
        this.configMapFilePath = configMapFilePath;
    }

    @Override
    public @NotNull Mono<JsonObject> get(CbsRequest request) {
        return Mono.just(this.loadConfigMapFile())
                .doOnNext(this::processEnvironmentVariables)
                .doOnNext(this::logConfigMapOutput);
    }

    public boolean verifyConfigMapFile() {
        try {
            LOGGER.info("Trying to load configuration from configMap file: {}", configMapFilePath);
            this.loadConfigMapFile().isJsonObject();
            return true;
        } catch(Exception ex) {
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

    void processEnvironmentVariables(JsonObject jsonObject) {
            for (String key : jsonObject.keySet()) {
                if (jsonObject.get(key) instanceof JsonObject) {
                    processEnvironmentVariables(jsonObject.get(key).getAsJsonObject());
                } else
                {
                    Matcher matcher = shellEnvPattern.matcher(jsonObject.get(key).getAsString());
                    if (matcher.find()) {
                        String envName = matcher.group(1);
                        String envValue = System.getenv(envName);
                        if (envValue == null || "".equals(envValue)) {
                            throw new CbsClientConfigMapException("Cannot read " + envName + " from environment.");
                        }
                        jsonObject.addProperty(key, envValue);
                    }
                }
            }
    }

    private void logConfigMapOutput(JsonObject jsonObject) {
        LOGGER.info("Got successful output from ConfigMap file");
        LOGGER.debug("ConfigMap output: {}", jsonObject);
    }

    private void logConfigMapError(Exception ex) {
        LOGGER.error("Error loading configuration from configMap file: {}", ex.getMessage());
    }
}