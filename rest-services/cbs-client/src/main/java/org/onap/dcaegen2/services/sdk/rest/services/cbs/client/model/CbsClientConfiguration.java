/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
 * Copyright (C) 2021 Wipro Limited.
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.CbsClientConfigurationException;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableTrustStoreKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.TrustStoreKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Immutable object which helps with construction of cloudRequestObject for specified Client. For usage take a look in
 * CloudConfigurationClient.class
 *
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 11/16/18
 * @version 1.0.0 can be passed to ReactiveCloudConfigurationProvider, can be constructed out of
 * org.onap.dcaegen2.services.sdk library.
 * @since 1.0.0
 */
@Value.Immutable(prehash = true)
public interface CbsClientConfiguration {
    Logger LOGGER = LoggerFactory.getLogger(CbsClientConfiguration.class);

    String TRUST_JKS = "trust.jks";
    String TRUST_PASS = "trust.pass";


    /**
     * Name of environment variable containing path to the cacert.pem file.
     */
    String DCAE_CA_CERT_PATH = "DCAE_CA_CERTPATH";

    /**
     * Name of environment variable containing current application name.
     */
    String ENV_APP_NAME = "HOSTNAME";

    /**
     * Name of environment variable containing path to application config file.
     */
    String ENV_CBS_CLIENT_CONFIG_PATH = "CBS_CLIENT_CONFIG_PATH";

    /**
     * Name of environment variable containing path to policies file.
     */
    String ENV_CBS_CLIENT_POLICY_PATH = "CBS_CLIENT_POLICY_PATH";


    @Value.Parameter
    String appName();

    @Value.Default
    default @Nullable TrustStoreKeys trustStoreKeys() {
        return null;
    }

    @Value.Default
    default String configMapFilePath() {
        return "/app-config/application_config.yaml";
    }
    @Value.Default
    default String policySyncFilePath() {
        return "/etc/policies/policies.json";
    }


    /**
     * Creates CbsClientConfiguration from system environment variables.
     *
     * @return an instance of CbsClientConfiguration
     * @throws CbsClientConfigurationException when at least one of required parameters is missing
     */
    static CbsClientConfiguration fromEnvironment() {
        String pathToCaCert = System.getenv(DCAE_CA_CERT_PATH);

        ImmutableCbsClientConfiguration.Builder configBuilder = ImmutableCbsClientConfiguration.builder()
                .appName(getEnv(ENV_APP_NAME));

        Optional.ofNullable(System.getenv(ENV_CBS_CLIENT_CONFIG_PATH))
            .ifPresent(configBuilder::configMapFilePath);

        Optional.ofNullable(System.getenv(ENV_CBS_CLIENT_POLICY_PATH))
            .ifPresent(configBuilder::policySyncFilePath);
        return configBuilder.build();
    }

    static String getEnv(String envName) {
        String envValue = System.getenv(envName);
        validateEnv(envName, envValue);
        return envValue;
    }

    static void validateEnv(String envName, String envValue) {
        if (envValue == null || "".equals(envValue)) {
            throw new CbsClientConfigurationException("Cannot read " + envName + " from environment.");
        }
    }


}
