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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.utlis;

import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapCustomConfig;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class SecurityKeysUtilTest {

    private static final String KEY_STORE_FILE_PATH = testResourceToPath("/org.onap.dcae.jks");
    private static final String KEY_STORE_PASSWORD_FILE_PATH = testResourceToPath("/keystore.password");
    private static final String TRUST_STORE_FILE_PATH = testResourceToPath("/org.onap.dcae.trust.jks");
    private static final String TRUST_STORE_PASSWORD_FILE_PATH = testResourceToPath("/truststore.password");

    private DmaapCustomConfig dmaapConfig = mock(DmaapCustomConfig.class);

    @Test
    void shouldLoadSecurityKeysUsingSpecifiedFilePaths() {
        when(dmaapConfig.keyStorePath()).thenReturn(KEY_STORE_FILE_PATH);
        when(dmaapConfig.keyStorePasswordPath()).thenReturn(KEY_STORE_PASSWORD_FILE_PATH);
        when(dmaapConfig.trustStorePath()).thenReturn(TRUST_STORE_FILE_PATH);
        when(dmaapConfig.trustStorePasswordPath()).thenReturn(TRUST_STORE_PASSWORD_FILE_PATH);

        SecurityKeys securityKeys = SecurityKeysUtil.fromDmappCustomConfig(dmaapConfig);

        assertEquals("mYHC98!qX}7h?W}jRv}MIXTJ", securityKeys.keyStorePassword().use(String::new));
        assertEquals(Paths.get(KEY_STORE_FILE_PATH), securityKeys.keyStore().path());
        assertEquals("*TQH?Lnszprs4LmlAj38yds(", securityKeys.trustStorePassword().use(String::new));
        assertEquals(Paths.get(TRUST_STORE_FILE_PATH), securityKeys.trustStore().path());
    }

    private static String testResourceToPath(String resource) {
        try {
            return Paths.get(SecurityKeysUtilTest.class.getResource(resource).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed resolving test resource path", e);
        }
    }
}