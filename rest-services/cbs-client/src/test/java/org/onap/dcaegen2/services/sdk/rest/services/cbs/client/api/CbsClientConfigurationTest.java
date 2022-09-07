/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api;


import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.CbsClientConfigurationException;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsClientConfigurationTest {

    private static final String ENV_APPNAME = "HOSTNAME";
    private static final String ENV_CBS_CLIENT_CONFIG_PATH = "CBS_CLIENT_CONFIG_PATH";
    private static final String ENV_CBS_CLIENT_POLICY_PATH = "CBS_CLIENT_POLICY_PATH";

    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables();

    @BeforeEach
    void setUp(){
        envs.clear(ENV_APPNAME, ENV_CBS_CLIENT_CONFIG_PATH, ENV_CBS_CLIENT_POLICY_PATH);
    }

    @Test
    void fromEnvironment_shouldReturnConfigurationWithCorrectConfigPath_when_CBS_CLIENT_CONFIG_PATH_isSet() {
        // given
        createBasicValidEnvsConfiguration();
        envs.set(ENV_CBS_CLIENT_CONFIG_PATH, "/new/config/path/application.yaml");

        // when
        CbsClientConfiguration configuration = CbsClientConfiguration.fromEnvironment();

        // then
        assertThat(configuration).isNotNull();
        assertThat(configuration.configMapFilePath()).isEqualTo("/new/config/path/application.yaml");
    }

    @Test
    void fromEnvironment_shouldReturnConfigurationWithCorrectPolicyPath_when_CBS_CLIENT_POLICY_PATH_isSet() {
        // given
        createBasicValidEnvsConfiguration();
        envs.set(ENV_CBS_CLIENT_POLICY_PATH, "/new/config/path/policy.json");

        // when
        CbsClientConfiguration configuration = CbsClientConfiguration.fromEnvironment();

        // then
        assertThat(configuration).isNotNull();
        assertThat(configuration.policySyncFilePath()).isEqualTo("/new/config/path/policy.json");
    }

    @Test
    void fromEnvironment_shouldReturnConfigurationWithDefaultPolicyAndConfigPaths_whenEnvsNotSet() {
        // given
        createBasicValidEnvsConfiguration();

        // when
        CbsClientConfiguration configuration = CbsClientConfiguration.fromEnvironment();

        // then
        assertThat(configuration).isNotNull();
        assertThat(configuration.configMapFilePath()).isEqualTo("/app-config/application_config.yaml");
        assertThat(configuration.policySyncFilePath()).isEqualTo("/etc/policies/policies.json");
    }


    @Test
    void fromEnvironment_shouldReturn_CbsClientConfigurationException_WhenAllEnvVariablesAreMissing() {
        assertThatExceptionOfType(CbsClientConfigurationException.class)
                .isThrownBy(CbsClientConfiguration::fromEnvironment);
    }

    private void createBasicValidEnvsConfiguration() {
        envs.set(ENV_APPNAME, "dcae-prh");
    }
}