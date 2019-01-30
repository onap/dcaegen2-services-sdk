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
     * Function for creating secure ssl context.
     *
     * @param keys - Security keys to be used
     * @return configured SSL context
     */
    public Try<SslContext> createSecureContext(final SecurityKeys keys) {
        final Try<KeyManagerFactory> keyManagerFactory =
                keyManagerFactory(keys.keyStore(), keys.keyStorePassword());
        final Try<TrustManagerFactory> trustManagerFactory =
                trustManagerFactory(keys.trustStore(), keys.trustStorePassword());

        return Try.success(SslContextBuilder.forClient())
                .flatMap(ctx -> keyManagerFactory.map(ctx::keyManager))
                .flatMap(ctx -> trustManagerFactory.map(ctx::trustManager))
                .mapTry(SslContextBuilder::build);
    }

    private Try<KeyManagerFactory> keyManagerFactory(Path path, Password password) {
        return password.useChecked(passwordChars -> {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(loadKeyStoreFromFile(path, passwordChars), passwordChars);
            return kmf;
        });
    }

    private Try<TrustManagerFactory> trustManagerFactory(Path path, Password password) {
        return password.useChecked(passwordChars -> {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(loadKeyStoreFromFile(path, passwordChars));
            return tmf;
        });
    }

    private KeyStore loadKeyStoreFromFile(Path path, char[] keyStorePassword)
            throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        ks.load(Files.newInputStream(path, StandardOpenOption.READ), keyStorePassword);
        return ks;
    }

    /**
     * Function for creating insecure ssl context.
     *
     * @return configured insecure ssl context
     */
    public Try<SslContext> createInsecureContext() {
        return Try.success(SslContextBuilder.forClient())
            .map(ctx -> ctx.trustManager(InsecureTrustManagerFactory.INSTANCE))
            .mapTry(SslContextBuilder::build);
    }
}
