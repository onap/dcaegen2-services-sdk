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

import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableEnvProperties;
import reactor.core.publisher.Mono;

/**
 * Complete CloudConfiguration HTTPClient API.
 *
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/16/18
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CloudConfigurationClient implements CloudConfigurationProvider {

    private final CloudConfigurationProvider cloudConfigurationProvider;

    /**
     * Default constructor for CloudConfigurationClient, set CloudConfigurationProvider cloudConfigurationProvider
     * property by calling: {@link ReactiveCloudConfigurationProvider}.
     * Calls other constructor in this class {@link #CloudConfigurationClient(CloudConfigurationProvider)}.
     */
    public CloudConfigurationClient() {
        this(new ReactiveCloudConfigurationProvider());
    }

    /**
     * Constructor for CloudConfigurationClient, set loudConfigurationProvider cloudConfigurationProvider property
     * by passing them in constructor {@link org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers.CloudConfigurationProvider}
     * implementation client.
     *
     * @param cloudConfigurationProvider - client provider for calling ConfigBindingService
     */
    public CloudConfigurationClient(
        CloudConfigurationProvider cloudConfigurationProvider) {
        this.cloudConfigurationProvider = cloudConfigurationProvider;
    }

    /**
     * Documentation in {@link org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers.CloudConfigurationProvider}
     *
     * @param consulHost - Hostname/IPAddress of consul Database
     * @param consulPort - Port number of consul Database
     * @param cbsName - ConfigBindingService url
     * @param appName - ApplicationName for each config will be returned
     */
    @Override
    public Mono<JsonObject> callForServiceConfigurationReactive(String consulHost, int consulPort, String cbsName,
        String appName) {
        return cloudConfigurationProvider.callForServiceConfigurationReactive(
            ImmutableEnvProperties.builder().consulHost(consulHost)
                .consulPort(consulPort).cbsName(cbsName)
                .appName(appName).build());
    }

    /**
     * Documentation in {@link org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers.CloudConfigurationProvider}.
     *
     * @param envProperties - Object holds consulPort, consulURL, configBindingSeriveName, applicationName which have
     * been defined in dcaegen2 cloud environment.
     */
    @Override
    public Mono<JsonObject> callForServiceConfigurationReactive(EnvProperties envProperties) {
        return cloudConfigurationProvider.callForServiceConfigurationReactive(envProperties);
    }

    /**
     * Documentation in {@link org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers.CloudConfigurationProvider}.
     *
     * @param consulHost - Hostname/IPAddress of consul Database
     * @param consulPort - Port number of consul Database
     * @param cbsName - ConfigBindingService url
     * @param appName - ApplicationName for each config will be returned
     */
    @Override
    public JsonObject callForServiceConfiguration(String consulHost, int consulPort, String cbsName, String appName) {
        return cloudConfigurationProvider.callForServiceConfigurationReactive(
            ImmutableEnvProperties.builder().consulHost(consulHost)
                .consulPort(consulPort).cbsName(cbsName)
                .appName(appName).build()).block();
    }

    /**
     * Documentation in {@link org.onap.dcaegen2.services.sdk.rest.services.cbs.client.providers.CloudConfigurationProvider}.
     *
     * @param envProperties - Object holds consulPort, consulURL, configBindingSeriveName, applicationName which have
     */
    @Override
    public JsonObject callForServiceConfiguration(EnvProperties envProperties) {
        return cloudConfigurationProvider.callForServiceConfigurationReactive(envProperties).block();
    }
}
