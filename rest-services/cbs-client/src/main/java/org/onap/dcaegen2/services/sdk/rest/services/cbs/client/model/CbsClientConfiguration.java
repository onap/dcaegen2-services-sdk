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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;

import java.nio.file.Paths;

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

    Integer HTTPS_PORT = 10443;

    Integer HTTP_PORT = 10000;

    String HTTPS_ENABLED = "1";

    /**
     * Name of environment variable containing Config Binding Service network hostname.
     */
    String ENV_CBS_HOSTNAME = "CONFIG_BINDING_SERVICE";

    /**
     * Name of environment variable containing current application name.
     */
    String ENV_APP_NAME = "HOSTNAME";

    /**
     * Name of environment variable indicating usage of the https protocol. ("1" - https enabled, other - not enabled)
     */
    String ENV_USE_HTTPS = "USE_HTTPS";

    /**
     * Name of environment variable containing path to the TLS private key.
     */
    String ENV_HTTPS_KEY_PATH = "HTTPS_KEY_PATH";

    /**
     * Name of environment variable containing path to the TLS private key password.
     */
    String ENV_HTTPS_KEY_PASS_PATH = "HTTPS_KEY_PASS_PATH";

    /**
     * Name of environment variable containing path to the TLS certificate.
     */
    String ENV_HTTPS_CERT_PATH = "HTTPS_CERT_PATH";

    /**
     * Name of environment variable containing path to the TLS certificate password.
     */
    String ENV_HTTPS_CERT_PASS_PATH = "HTTPS_CERT_PASS_PATH";

    /**
     * Name of environment variable containing Consul host name.
     *
     * @deprecated CBS lookup in Consul service should not be needed,
     * instead {@link #ENV_CBS_HOSTNAME} should be used directly.
     */
    @Deprecated
    String ENV_CONSUL_HOST = "CONSUL_HOST";
    /**
     * Name of environment variable containing Config Binding Service <em>service name</em> as registered in Consul
     * services API.
     *
     * @deprecated CBS lookup in Consul service should not be needed,
     * instead {@link #ENV_CBS_HOSTNAME} should be used directly.
     */
    @Deprecated
    String ENV_CBS_NAME = "CONFIG_BINDING_SERVICE";

    @Value.Parameter
    @Nullable
    String hostname();

    @Value.Parameter
    @Nullable
    Integer port();

    @Value.Parameter
    String appName();

    @Value.Default
    default @Nullable SecurityKeys securityKeys() {
        return null;
    }

    @Value.Default
    @Deprecated
    default String consulHost() {
        return "consul-server";
    }
    @Value.Default
    @Deprecated
    default Integer consulPort() {
        return 8500;
    }
    @Value.Default
    @Deprecated
    default String cbsName() {
        return "config-binding-service";
    }

    /**
     * Creates CbsClientConfiguration from system environment variables.
     *
     * @return an instance of CbsClientConfiguration
     * @throws NullPointerException when at least one of required parameters is missing
     */
    static CbsClientConfiguration fromEnvironment() {

        ImmutableCbsClientConfiguration configuration = ImmutableCbsClientConfiguration.builder()
                .hostname(System.getenv(ENV_CBS_HOSTNAME))
                .appName(System.getenv(ENV_APP_NAME))
                .build();

        if (System.getenv(ENV_CONSUL_HOST) != null) {
            configuration.withConsulHost(System.getenv(ENV_CONSUL_HOST));
        }

        return !HTTPS_ENABLED.equals(System.getenv(ENV_USE_HTTPS))
                ? configuration.withPort(HTTP_PORT)
                : configuration.withSecurityKeys(crateSecurityKeysFromEnvironment()).withPort(HTTPS_PORT);
    }

    static SecurityKeys crateSecurityKeysFromEnvironment() {
        return ImmutableSecurityKeys.builder()
                .keyStore(ImmutableSecurityKeysStore.of(Paths.get(System.getenv(ENV_HTTPS_KEY_PATH))))
                .keyStorePassword(Passwords.fromPath(Paths.get(System.getenv(ENV_HTTPS_KEY_PASS_PATH))))
                .trustStore(ImmutableSecurityKeysStore.of(Paths.get(System.getenv(ENV_HTTPS_CERT_PATH))))
                .trustStorePassword(Passwords.fromPath(Paths.get(System.getenv(ENV_HTTPS_CERT_PASS_PATH))))
                .build();
    }
}
