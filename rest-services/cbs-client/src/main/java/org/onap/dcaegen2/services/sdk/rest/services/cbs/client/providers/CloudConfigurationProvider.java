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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.EnvProperties;
import reactor.core.publisher.Mono;

/**
 * Rest CloudConfigurationClient interface which will define all necessary methods in connection with downloading JSON
 * for each component in DCAEGEN2 repository.
 *
 * This interface holds contract for user-defined client or our defined implementation in CBS-client module.
 *
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/16/18
 * @version 1.0.0 has got only one implementation of this interface: 1. ReactiveCloudConfigurationProvider.class
 * @since 1.0.0
 */
public interface CloudConfigurationProvider {

    /* callForServiceConfigurationReactive */

    /**
     * Getting configuration for appName from ConfigBindingService.
     *
     * @param envProperties - Object holds consulPort, consulURL, configBindingSeriveName, applicationName which have
     * been defined in dcaegen2 cloud environment.
     * @return Single reactive response which @Mono which holds inside them JsonObject with configuration for specified
     * application Name
     */
    Mono<JsonObject> callForServiceConfigurationReactive(EnvProperties envProperties);

    /* callForServiceConfigurationReactive */

    /**
     * Getting configuration for appName from ConfigBindingService.
     *
     * @param consulHost - Hostname/IPAddress of consul Database
     * @param consulPort - Port number of consul Database
     * @param cbsName - ConfigBindingService url
     * @param appName - ApplicationName for each config will be returned
     * @return rective configuration for specified application in dcaegen2 cloud infrastructure.
     */
    Mono<JsonObject> callForServiceConfigurationReactive(String consulHost, int consulPort, String cbsName,
        String appName);


    /*callForServiceConfiguration*/

    /**
     * Getting configuration for appName from ConfigBindingService.
     *
     * @param consulHost - Hostname/IPAddress of consul Database
     * @param consulPort - Port number of consul Database
     * @param cbsName - ConfigBindingService url
     * @param appName - ApplicationName for each config will be returned
     * @return configuration for specified application in dcaegen2 cloud infrastructure.
     */
    JsonObject callForServiceConfiguration(String consulHost, int consulPort, String cbsName, String appName);

    /*callForServiceConfiguration*/

    /**
     * Getting configuration for appName from ConfigBindingService.
     *
     * @param envProperties - Object holds consulPort, consulURL, configBindingSeriveName, applicationName which have
     * @return configuration for specified application in dcaegen2 cloud infrastructure.
     */
    JsonObject callForServiceConfiguration(EnvProperties envProperties);

}
