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

package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.Password;
import org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options.SecurityKeys;

/*
 * TODO: To be merged with org.onap.dcaegen2.services.sdk.rest.services.ssl.SslFactory
 */
public class SslFactory {

    /**
     * Function for creating secure ssl context.
     *
     * @param keys - Security keys to be used
     * @return configured SSL context
     */
    public Try<SslContext> createSecureContext(final SecurityKeys keys) {
        return Try.of(() -> {
            return SslContextBuilder
                    .forClient()
                    .keyManager(keyManagerFactory(keys.keyStore(), keys.keyStorePassword()))
                    .trustManager(trustManagerFactory(keys.trustStore(), keys.trustStorePassword()))
                    .build();
        });
    }

    private KeyManagerFactory keyManagerFactory(Path path, Password password)
            throws GeneralSecurityException, IOException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        password.use(passwordChars -> kmf.init(loadKeyStoreFromFile(path, passwordChars), passwordChars));
        return kmf;
    }

    private TrustManagerFactory trustManagerFactory(Path path, Password password)
            throws GeneralSecurityException, IOException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        password.use(passwordChars -> tmf.init(loadKeyStoreFromFile(path, passwordChars)));
        return tmf;
    }

    private KeyStore loadKeyStoreFromFile(Path path, char[] keyStorePassword)
            throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        ks.load(Files.newInputStream(path, StandardOpenOption.READ), keyStorePassword);
        return ks;
    }
}