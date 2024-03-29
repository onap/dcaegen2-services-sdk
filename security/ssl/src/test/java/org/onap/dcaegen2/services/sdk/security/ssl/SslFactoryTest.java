/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2022 Nokia. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.security.ssl;

import io.netty.handler.ssl.SslContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.security.ssl.exceptions.ReadingSecurityKeysStoreException;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.onap.dcaegen2.services.sdk.security.ssl.Passwords.fromResource;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since April 2019
 */
class SslFactoryTest {

    private SslFactory sut = new SslFactory();

    @Test
    void testSuccessCase() throws Exception {
        // given
        final SecurityKeys securityKeys = ImmutableSecurityKeys.builder()
                .keyStore(keyStoreFromResource("/sample/cert.jks"))
                .keyStorePassword(fromResource("/sample/jks.pass"))
                .trustStore(keyStoreFromResource("/sample/trust.jks"))
                .trustStorePassword(fromResource("/sample/trust.pass"))
                .build();

        // when
        final SslContext ctx = sut.createSecureServerContext(securityKeys);

        // then
        assertThat(ctx.isServer()).describedAs("is server ssl context").isTrue();
    }

    @Test
    void testInvalidKeyStorePasswordCase() throws Exception {
        // given
        final SecurityKeys securityKeys = ImmutableSecurityKeys.builder()
                .keyStore(keyStoreFromResource("/sample/cert.jks"))
                .keyStorePassword(fromResource("/sample/invalid.pass"))
                .trustStore(keyStoreFromResource("/sample/trust.jks"))
                .trustStorePassword(fromResource("/sample/trust.pass"))
                .build();

        // when & then
        assertThatThrownBy(() -> sut.createSecureServerContext(securityKeys))
                .isInstanceOf(ReadingSecurityKeysStoreException.class)
                .hasMessageContaining("Keystore was tampered with, or password was incorrect");
    }

    @Test
    void testInvalidTrustStorePasswordCase() throws Exception {
        // given
        final SecurityKeys securityKeys = ImmutableSecurityKeys.builder()
                .keyStore(keyStoreFromResource("/sample/cert.jks"))
                .keyStorePassword(fromResource("/sample/jks.pass"))
                .trustStore(keyStoreFromResource("/sample/trust.jks"))
                .trustStorePassword(fromResource("/sample/invalid.pass"))
                .build();

        // when & then
        assertThatThrownBy(() -> sut.createSecureServerContext(securityKeys))
                .isInstanceOf(ReadingSecurityKeysStoreException.class)
                .hasMessageContaining("Keystore was tampered with, or password was incorrect");
    }

    @Test
    void testCreateInsecureClient () {
        assertThatCode(() -> sut.createInsecureClientContext())
            .doesNotThrowAnyException();
    }

    @Test
    void testCreateSecureClientContextFromSecurityKeys() throws Exception {
        // given
        final SecurityKeys securityKeys = ImmutableSecurityKeys.builder()
            .keyStore(keyStoreFromResource("/sample/cert.jks"))
            .keyStorePassword(fromResource("/sample/jks.pass"))
            .trustStore(keyStoreFromResource("/sample/trust.jks"))
            .trustStorePassword(fromResource("/sample/trust.pass"))
            .build();

        // when
        assertThatCode(() -> sut.createSecureClientContext(securityKeys))
            .doesNotThrowAnyException();
    }

    private @NotNull SecurityKeysStore keyStoreFromResource(String resource) throws URISyntaxException {
        return SecurityKeysStore.fromPath(
                Paths.get(Passwords.class.getResource(resource).toURI()));
    }

}
