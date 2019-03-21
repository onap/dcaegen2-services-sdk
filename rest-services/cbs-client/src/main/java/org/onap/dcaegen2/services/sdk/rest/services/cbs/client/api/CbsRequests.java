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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api;

import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.ImmutableCbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;

/**
 * A factory to various of requests supported by Config Binding Service.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public final class CbsRequests {

    /**
     * <p>A get-configuration request.</p>
     *
     * <p>Will bind the configuration for given service and return the bound configuration.</p>
     *
     * @param diagnosticContext logging diagnostic context (MDC)
     * @return the CbsRequest ready to be used when calling {@link CbsClient}
     */
    public static @NotNull CbsRequest getConfiguration(RequestDiagnosticContext diagnosticContext) {
        return ImmutableCbsRequest.builder()
                .diagnosticContext(diagnosticContext)
                .requestPath(serviceName -> "/service_component/" + serviceName)
                .build();
    }

    /**
     * <p>A get-by-key request.</p>
     *
     * <p>This will call an endpoint that fetches a generic service_component_name:key out of Consul</p>
     *
     * @param diagnosticContext logging diagnostic context (MDC)
     * @return the CbsRequest ready to be used when calling {@link CbsClient}
     */
    public static @NotNull CbsRequest getByKey(
            RequestDiagnosticContext diagnosticContext,
            String key) {
        return ImmutableCbsRequest.builder()
                .diagnosticContext(diagnosticContext)
                .requestPath(serviceName -> "/" + key + "/" + serviceName)
                .build();
    }

    /**
     * <p>A get-all request.</p>
     *
     * <p>Will bind the configuration for given service and return the bound configuration, policies, and any other
     * keys that are in Consul</p>
     *
     * @param diagnosticContext logging diagnostic context (MDC)
     * @return the CbsRequest ready to be used when calling {@link CbsClient}
     */
    public static @NotNull CbsRequest getAll(RequestDiagnosticContext diagnosticContext) {
        return ImmutableCbsRequest.builder()
                .diagnosticContext(diagnosticContext)
                .requestPath(serviceName -> "/service_component_all/" + serviceName)
                .build();
    }

}
