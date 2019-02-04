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

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class SslFactory {

    /**
     * Creates Netty SSL <em>client</em> context using provided security keys.
     *
     * @param keys - Security keys to be used
     * @return configured SSL context
     */
    public Try<SslContext> createSecureClientContext(final SecurityKeys keys) {
        return Try.success(SslContextBuilder.forClient())
                .flatMap(ctx -> keyManagerFactory(keys).map(ctx::keyManager))
                .flatMap(ctx -> trustManagerFactory(keys).map(ctx::trustManager))
                .mapTry(SslContextBuilder::build);
    }

    /**
     * Creates Netty SSL <em>server</em> context using provided security keys.
     *
     * @param keys - Security keys to be used
     * @return configured SSL context
     */
    public Try<SslContext> createSecureServerContext(final SecurityKeys keys) {
        return keyManagerFactory(keys)
                .map(SslContextBuilder::forServer)
                .flatMap(ctx -> trustManagerFactory(keys).map(ctx::trustManager))
                .mapTry(SslContextBuilder::build);
    }

    /**
     * Function for creating insecure SSL context.
     *
     * @return configured insecure ssl context
     * @deprecated Do not use in production. Will trust anyone.
     */
    @Deprecated
    public Try<SslContext> createInsecureClientContext() {
        return Try.success(SslContextBuilder.forClient())
                .map(ctx -> ctx.trustManager(InsecureTrustManagerFactory.INSTANCE))
                .mapTry(SslContextBuilder::build);
    }

    private Try<TrustManagerFactory> trustManagerFactory(SecurityKeys keys) {
        return trustManagerFactory(keys.trustStore(), keys.trustStorePassword());
    }

    private Try<KeyManagerFactory> keyManagerFactory(SecurityKeys keys) {
        return keyManagerFactory(keys.keyStore(), keys.keyStorePassword());
    }

    private Try<KeyManagerFactory> keyManagerFactory(SecurityKeysStore store, Password password) {
        return password.useChecked(passwordChars -> {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(loadKeyStoreFromFile(store, passwordChars), passwordChars);
            return kmf;
        });
    }

    private Try<TrustManagerFactory> trustManagerFactory(SecurityKeysStore store, Password password) {
        return password.useChecked(passwordChars -> {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(loadKeyStoreFromFile(store, passwordChars));
            return tmf;
        });
    }

    private KeyStore loadKeyStoreFromFile(SecurityKeysStore store, char[] keyStorePassword)
            throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(store.type());
        ks.load(Files.newInputStream(store.path(), StandardOpenOption.READ), keyStorePassword);
        return ks;
    }
}
