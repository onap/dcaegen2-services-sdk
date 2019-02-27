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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
@Value.Immutable(singleton = true)
public interface GlobalDiagnosticContext {

    @Value.Default
    default String instanceId() {
        return UUID.randomUUID().toString();
    }

    @Value.Default
    default String serverFqdn() {
        try {
            return InetAddress.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            return InetAddress.getLoopbackAddress().toString();
        }
    }

    @Value.Default
    default String serviceName() {
        return System.getenv().getOrDefault("HOSTNAME", "unknown_service");
    }

    @Value.Derived
    default Map<String, String> asMap() {
        return HashMap.of(
                MdcVariables.INSTANCE_ID, instanceId(),
                MdcVariables.SERVER_FQDN, serverFqdn(),
                MdcVariables.SERVICE_NAME, serviceName());
    }

    static GlobalDiagnosticContext instance() {
        return ImmutableGlobalDiagnosticContext.of();
    }
}


