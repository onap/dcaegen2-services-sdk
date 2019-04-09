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

import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapCustomConfig;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;

public class SecurityKeysUtil {

    @NotNull
    public static SecurityKeys fromDmappCustomConfig(DmaapCustomConfig configuration){
        return ImmutableSecurityKeys.builder()
                .keyStore(ImmutableSecurityKeysStore.of(resource(configuration.keyStorePath()).get()))
                .keyStorePassword(Passwords.fromResource(configuration.keyStorePasswordPath()))
                .trustStore(ImmutableSecurityKeysStore.of(resource(configuration.trustStorePath()).get()))
                .trustStorePassword(Passwords.fromResource(configuration.trustStorePasswordPath()))
                .build();
    }

    private static Try<Path> resource(String resource) {
        return Try.of(() -> Paths.get(Passwords.class.getResource(resource).toURI()));
    }
}
