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

package org.onap.dcaegen2.services.sdk.rest.services.model.logging;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.UUID;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
@Value.Immutable
public interface DiagnosticContext {

    @Nullable UUID requestId();

    @Nullable UUID invocationId();

    @Nullable String instanceId();

    @Nullable String responseCode();

    @Nullable String serverFqdn();

    @Value.Derived
    default Map<String, String> remoteCallHttpHeaders() {
        java.util.Map<String, String> result = new java.util.HashMap<>();

        if (requestId() != null) {
            result.put(OnapMdc.HTTP_HEADER_PREFIX + OnapMdc.REQUEST_ID, requestId().toString());
        }

        if (invocationId() != null) {
            result.put(OnapMdc.HTTP_HEADER_PREFIX + OnapMdc.INVOCATION_ID, invocationId().toString());
        }

        return HashMap.ofAll(result);
    }

    @Value.Derived
    default Map<String, String> asMap() {
        java.util.Map<String, String> result = new java.util.HashMap<>();

        if (requestId() != null) {
            result.put(OnapMdc.REQUEST_ID, requestId().toString());
        }

        if (invocationId() != null) {
            result.put(OnapMdc.INVOCATION_ID, invocationId().toString());
        }

        if (instanceId() != null) {
            result.put(OnapMdc.INSTANCE_ID, instanceId());
        }

        if (serverFqdn() != null) {
            result.put(OnapMdc.SERVER_FQDN, serverFqdn());
        }

        return HashMap.ofAll(result);
    }

    default void setSlf4jMdc() {
        MDC.setContextMap(asMap().toJavaMap());
    }

    static DiagnosticContext create() {
        return ImmutableDiagnosticContext.builder()
                .invocationId(UUID.randomUUID())
                .requestId(UUID.randomUUID())
                .serverFqdn(System.getenv("HOSTNAME"))
                .build();
    }
}


