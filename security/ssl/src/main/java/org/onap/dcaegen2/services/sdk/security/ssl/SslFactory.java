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

package org.onap.dcaegen2.services.sdk.security.ssl;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.onap.dcaegen2.services.sdk.security.ssl.exceptions.ReadingSecurityKeysStoreException;
import org.onap.dcaegen2.services.sdk.security.ssl.exceptions.SecurityConfigurationException;

/**
 * @since 1.1.1
 */
public class SslFactory {

    private static final String EXCEPTION_MESSAGE = "Could not create SSL context";

    /**
     * Creates Netty SSL <em>client</em> context using provided security keys.
     *
     * @param keys - Security keys to be used
     * @return configured SSL context
     */
    public SslContext createSecureClientContext(final SecurityKeys keys) {
        try {
            return SslContextBuilder.forClient()
                    .keyManager(keyManagerFactory(keys))
                    .trustManager(trustManagerFactory(keys))
                    .build();
        } catch (SSLException e) {
            throw new SecurityConfigurationException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Creates Netty SSL <em>server</em> context using provided security keys. Will require client authentication.
     *
     * @param keys - security keys to be used
     * @return configured SSL context
     */
    public SslContext createSecureServerContext(final SecurityKeys keys) {
        return createSecureServerContext(keys, ClientAuth.REQUIRE);
    }

    /**
     * Creates Netty SSL <em>server</em> context using provided security keys.
     *
     * @param keys - security keys to be used
     * @param clientAuth - how to authenticate client
     * @return configured SSL context
     */
    public SslContext createSecureServerContext(final SecurityKeys keys, final ClientAuth clientAuth) {
        try {
            return SslContextBuilder.forServer(keyManagerFactory(keys))
                    .trustManager(trustManagerFactory(keys))
                    .clientAuth(clientAuth)
                    .build();
        } catch (SSLException e) {
            throw new SecurityConfigurationException(EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Function for creating insecure SSL context.
     *
     * @return configured insecure ssl context
     * @deprecated Do not use in production. Will trust anyone.
     */
    @Deprecated
    public SslContext createInsecureClientContext() {
        try {
            return SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (SSLException e) {
            throw new SecurityConfigurationException(EXCEPTION_MESSAGE, e);
        }
    }

    private TrustManagerFactory trustManagerFactory(SecurityKeys keys) {
        return trustManagerFactory(keys.trustStore(), keys.trustStorePassword());
    }

    private KeyManagerFactory keyManagerFactory(SecurityKeys keys) {
        return keyManagerFactory(keys.keyStore(), keys.keyStorePassword());
    }

    private KeyManagerFactory keyManagerFactory(SecurityKeysStore store, Password password) {
        return password.use(passwordChars -> {
            try {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(loadKeyStoreFromFile(store, passwordChars), passwordChars);
                return kmf;
            } catch (GeneralSecurityException | IOException ex) {
                throw new ReadingSecurityKeysStoreException(
                        "Could not read private keys from store: " + ex.getMessage(), ex);
            }
        });
    }

    private TrustManagerFactory trustManagerFactory(SecurityKeysStore store, Password password) {
        return password.use(passwordChars -> {
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(loadKeyStoreFromFile(store, passwordChars));
                return tmf;
            } catch (GeneralSecurityException | IOException ex) {
                throw new ReadingSecurityKeysStoreException(
                        "Could not read trusted keys from store: " + ex.getMessage(), ex);
            }
        });
    }

    private KeyStore loadKeyStoreFromFile(SecurityKeysStore store, char[] keyStorePassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(store.type());
        ks.load(Files.newInputStream(store.path(), StandardOpenOption.READ), keyStorePassword);
        return ks;
    }
}
