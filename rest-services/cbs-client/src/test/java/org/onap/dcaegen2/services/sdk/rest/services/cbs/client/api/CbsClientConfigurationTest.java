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
    void fromEnvironment_shouldReturnConfigurationForUnsecureConnection() {
        // given
        envs.set("DCAE_CA_CERTPATH", "/opt/app/prh/component-name/etc/cert/cacert.pem");
        envs.set("CONFIG_BINDING_SERVICE", "config-binding-service");
        envs.set("CONFIG_BINDING_SERVICE_SERVICE_PORT", "10000");
        envs.set("CONFIG_BINDING_SERVICE_PORT_10443_TCP_PORT", "10443");
        envs.set("HOSTNAME", "dcae-prh");
        envs.set("CONSUL_HOST", "consul-server.onap");

        // when
        CbsClientConfiguration configuration = CbsClientConfiguration.fromEnvironment();

        // then
        assertThat(configuration.securityKeys()).isEqualTo(null);
        assertThat(configuration.protocol()).isEqualTo("http");
    }

    @Test
    void fromEnvironment_shouldReturnConfigurationForSecureConnection() throws URISyntaxException {
        // given
        envs.set("DCAE_CA_CERTPATH", prepareEnv("/test-certs/cacert.pem"));
        envs.set("CONFIG_BINDING_SERVICE", "config-binding-service");
        envs.set("CONFIG_BINDING_SERVICE_SERVICE_PORT", "10000");
        envs.set("CONFIG_BINDING_SERVICE_PORT_10443_TCP_PORT", "10443");
        envs.set("HOSTNAME", "dcae-prh");
        envs.set("CONSUL_HOST", "consul-server.onap");

        // when
        CbsClientConfiguration configuration = CbsClientConfiguration.fromEnvironment();

        // then
        assertThat(configuration.securityKeys()).isNotNull();
        assertThat(configuration.protocol()).isEqualTo("https");
    }

    @Test
    void fromEnvironment_failWhenEnvVariablesAreMissing() {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(CbsClientConfiguration::fromEnvironment);
    }

    private String prepareEnv(String resource) throws URISyntaxException {
        return Paths.get(Passwords.class.getResource(resource).toURI()) + "";
    }
}