/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.rest.services.aai.client;

import java.util.HashMap;
import java.util.Map;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;

public final class AaiClientConfigurations {

    private AaiClientConfigurations() {
    }

    public static AaiClientConfiguration secureConfiguration() {
        return secureConfiguration(new HashMap<>());
    }

    public static AaiClientConfiguration secureConfiguration(Map<String, String> headers) {
        return validConfiguration(headers, true);
    }

    public static AaiClientConfiguration insecureConfiguration() {
        return validConfiguration(new HashMap<>(), false);
    }

    private static AaiClientConfiguration validConfiguration(Map<String, String> headers, boolean secure) {
        return new ImmutableAaiClientConfiguration.Builder()
                .aaiHost("sample-host")
                .aaiUserName("sample-username")
                .aaiUserPassword("sample-password")
                .aaiIgnoreSslCertificateErrors(false)
                .trustStorePath("/trust.pkcs12")
                .trustStorePasswordPath("/trust.pass")
                .keyStorePath("/server.pkcs12")
                .keyStorePasswordPath("/server.pass")
                .enableAaiCertAuth(secure)
                .aaiHeaders(headers)
                .aaiProtocol("sample-protocol")
                .aaiPort(8080)
                .aaiBasePath("sample-base-path")
                .aaiPnfPath("sample-pnf-path")
                .aaiServiceInstancePath("sample-instance-path")
                .build();
    }
}
