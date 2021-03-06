/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

    @Rule
    public final EnvironmentVariables envs = new EnvironmentVariables();

    @Test
    void fromEnvironment_shouldReturnConfigurationForConnectionWithoutTls_when_DCAE_CA_CERTPATH_isEmpty() {
        // given
        envs.set("DCAE_CA_CERTPATH", "");
        envs.set("CONFIG_BINDING_SERVICE", "config-binding-service");
        envs.set("CONFIG_BINDING_SERVICE_SERVICE_PORT", "10000");
        envs.set("HOSTNAME", "dcae-prh");
        envs.set("CONSUL_HOST", "consul-server.onap");

        // when
        CbsClientConfiguration configuration = CbsClientConfiguration.fromEnvironment();

        // then
        assertThat(configuration.trustStoreKeys()).isEqualTo(null);
        assertThat(configuration.protocol()).isEqualTo("http");
    }

    @Test
    void fromEnvironment_shouldReturnConfigurationForConnectionOverTls_when_DCAE_CA_CERTPATH_isSet() throws URISyntaxException {
        // given
        envs.set("DCAE_CA_CERTPATH", preparePathToCertFile());
        envs.set("CONFIG_BINDING_SERVICE", "config-binding-service");
        envs.set("HOSTNAME", "dcae-prh");
        envs.set("CONSUL_HOST", "consul-server.onap");

        // when
        CbsClientConfiguration configuration = CbsClientConfiguration.fromEnvironment();

        // then
        assertThat(configuration.trustStoreKeys()).isNotNull();
        assertThat(configuration.protocol()).isEqualTo("https");
    }

    @Test
    void fromEnvironment_shouldReturn_CbsClientConfigurationException_When_DCAE_CA_CERTPATH_is_Null() {
        // given
        envs.set("DCAE_CA_CERTPATH", null);
        envs.set("CONFIG_BINDING_SERVICE_SERVICE_PORT", "9090");
        envs.set("CONFIG_BINDING_SERVICE", "config-binding-service");
        envs.set("HOSTNAME", "dcae-prh");
        envs.set("CONSUL_HOST", "consul-server.onap");

        // when
        CbsClientConfiguration configuration = CbsClientConfiguration.fromEnvironment();

        // then
        assertThat(configuration.trustStoreKeys()).isNull();
        assertThat(configuration.protocol()).isEqualTo("http");
    }

    @Test
    void fromEnvironment_shouldReturn_CbsClientConfigurationException_WhenAllEnvVariablesAreMissing() {
        assertThatExceptionOfType(CbsClientConfigurationException.class)
                .isThrownBy(CbsClientConfiguration::fromEnvironment);
    }

    @Test
    void fromEnvironment_shouldReturn_CbsClientConfigurationException_When_DCAE_CA_CERTPATH_isWrong() {
        // given
        envs.set("DCAE_CA_CERTPATH", "/home/cacert.pem");
        envs.set("HOSTNAME", "dcae-prh");
        envs.set("CONFIG_BINDING_SERVICE", "config-binding-service");
        envs.set("CONSUL_HOST", "consul-server.onap");

        // then
        assertThatExceptionOfType(CbsClientConfigurationException.class)
                .isThrownBy(CbsClientConfiguration::fromEnvironment)
                .withMessageContaining("Required files do not exist in /home directory");
    }

    @Test
    void fromEnvironment_shouldReturn_CbsClientConfigurationException_When_HOSTNAME_isMissing() throws URISyntaxException {
        // given
        envs.set("HOSTNAME", "");
        envs.set("DCAE_CA_CERTPATH", preparePathToCertFile());
        envs.set("CONFIG_BINDING_SERVICE", "config-binding-service");
        envs.set("CONSUL_HOST", "consul-server.onap");

        // then
        assertThatExceptionOfType(CbsClientConfigurationException.class)
                .isThrownBy(CbsClientConfiguration::fromEnvironment)
                .withMessageContaining("Cannot read HOSTNAME from environment.");
    }

    @Test
    void fromEnvironment_shouldReturn_CbsClientConfigurationException_When_CONFIG_BINDING_SERVICE_SERVICE_PORT_isEmpty() {
        // given
        envs.set("CONFIG_BINDING_SERVICE_SERVICE_PORT", "");
        envs.set("DCAE_CA_CERTPATH", "");
        envs.set("HOSTNAME", "dcae-prh");
        envs.set("CONFIG_BINDING_SERVICE", "config-binding-service");
        envs.set("CONSUL_HOST", "consul-server.onap");

        // then
        assertThatExceptionOfType(CbsClientConfigurationException.class)
                .isThrownBy(CbsClientConfiguration::fromEnvironment)
                .withMessageContaining("Cannot read CONFIG_BINDING_SERVICE_SERVICE_PORT from environment.");
    }

    private String preparePathToCertFile() throws URISyntaxException {
        return Paths.get(Passwords.class.getResource("/test-certs/cacert.pem").toURI()) + "";
    }
}