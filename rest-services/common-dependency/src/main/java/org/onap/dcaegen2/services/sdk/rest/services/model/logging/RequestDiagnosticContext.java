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
public interface RequestDiagnosticContext {

    UUID requestId();

    @Nullable UUID invocationId();

    @Value.Default
    default GlobalDiagnosticContext global() {
        return GlobalDiagnosticContext.instance();
    }

    @Value.Derived
    default Map<String, String> remoteCallHttpHeaders() {
        java.util.Map<String, String> result = new java.util.HashMap<>();

        if (requestId() != null) {
            result.put(MdcVariables.HTTP_HEADER_PREFIX + MdcVariables.REQUEST_ID, requestId().toString());
        }

        if (invocationId() != null) {
            result.put(MdcVariables.HTTP_HEADER_PREFIX + MdcVariables.INVOCATION_ID, invocationId().toString());
        }

        return HashMap.ofAll(result);
    }

    @Value.Derived
    default Map<String, String> asMap() {
        java.util.Map<String, String> result = new java.util.HashMap<>();

        if (requestId() != null) {
            result.put(MdcVariables.REQUEST_ID, requestId().toString());
        }

        if (invocationId() != null) {
            result.put(MdcVariables.INVOCATION_ID, invocationId().toString());
        }

        return global().asMap().merge(HashMap.ofAll(result));
    }

    default void setSlf4jMdc() {
        MDC.setContextMap(asMap().toJavaMap());
    }

    static ImmutableRequestDiagnosticContext create() {
        return ImmutableRequestDiagnosticContext.builder()
                .requestId(UUID.randomUUID())
                .invocationId(UUID.randomUUID())
                .build();
    }
}


