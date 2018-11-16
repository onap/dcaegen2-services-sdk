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
 *
 */

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.http.configuration.CloudHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.http.configuration.EnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/15/18
 */
final class ReactiveCloudConfigurationProvider implements CloudConfiguratinProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveCloudConfigurationProvider.class);

    private final CloudHttpClient cloudHttpClient;

    public ReactiveCloudConfigurationProvider() {
        this(new CloudHttpClient());
    }

    ReactiveCloudConfigurationProvider(
        CloudHttpClient cloudHttpClient) {
        this.cloudHttpClient = cloudHttpClient;
    }

    @Override
    public Mono<JsonObject> callForServiceConfigurationReactive(EnvProperties envProperties) {
        return callConsulForConfigBindingServiceEndpoint(envProperties)
            .flatMap(this::callConfigBindingServiceForPrhConfiguration);
    }

    @Override
    public Mono<JsonObject> callForServiceConfigurationReactive(String consulHost, int consulPort, String cbsName,
        String appName) {
        throw new UnsupportedOperationException("Unsupported method call: " + this);
    }

    @Override
    public JsonObject callForServiceConfiguration(String consulHost, int consulPort, String cbsName, String appName) {
        throw new UnsupportedOperationException("Unsupported method call: " + this);
    }

    @Override
    public JsonObject callForServiceConfiguration(EnvProperties envProperties) {
        throw new UnsupportedOperationException("Unsupported method call: " + this);
    }

    private Mono<String> callConsulForConfigBindingServiceEndpoint(EnvProperties envProperties) {
        LOGGER.info("Retrieving Config Binding Service endpoint from Consul");
        return cloudHttpClient.callHttpGet(getConsulUrl(envProperties), JsonArray.class)
            .flatMap(jsonArray -> this.createConfigBindingServiceUrl(jsonArray, envProperties.appName()));
    }

    private String getConsulUrl(EnvProperties envProperties) {
        return getUri(envProperties.consulHost(), envProperties.consulPort(), "/v1/catalog/service",
            envProperties.cbsName());
    }

    private Mono<JsonObject> callConfigBindingServiceForPrhConfiguration(String configBindingServiceUri) {
        LOGGER.info("Retrieving PRH configuration");
        return cloudHttpClient.callHttpGet(configBindingServiceUri, JsonObject.class);
    }


    private Mono<String> createConfigBindingServiceUrl(JsonArray jsonArray, String appName) {
        return getConfigBindingObject(jsonArray)
            .flatMap(jsonObject -> buildConfigBindingServiceUrl(jsonObject, appName));
    }

    private Mono<String> buildConfigBindingServiceUrl(JsonObject jsonObject, String appName) {
        return Mono.just(getUri(jsonObject.get("ServiceAddress").getAsString(),
            jsonObject.get("ServicePort").getAsInt(), "/service_component", appName));
    }

    private Mono<JsonObject> getConfigBindingObject(JsonArray jsonArray) {
        try {
            if (jsonArray.size() > 0) {
                return Mono.just(jsonArray.get(0).getAsJsonObject());
            } else {
                throw new IllegalStateException("JSON Array was empty");
            }
        } catch (IllegalStateException e) {
            LOGGER.warn("Failed to retrieve JSON Object from array", e);
            return Mono.error(e);
        }
    }

    private String getUri(String host, Integer port, String... paths) {
        return new DefaultUriBuilderFactory().builder()
            .scheme("http")
            .host(host)
            .port(port)
            .path(String.join("/", paths))
            .build().toString();
    }
}
