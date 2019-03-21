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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.requests.CbsRequestWithDiagnosticCtx;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsRequest;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.EnvProperties;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public final class CbsRequests {

    public static @NotNull CbsRequest getConfiguration(RequestDiagnosticContext diagnosticContext, EnvProperties env) {
        return getConfiguration(diagnosticContext, env.appName());
    }

    public static @NotNull CbsRequest getConfiguration(RequestDiagnosticContext diagnosticContext, String serviceName) {
        return new CbsRequestWithDiagnosticCtx(diagnosticContext, () -> "/service_component/" + serviceName);
    }

    public static @NotNull CbsRequest getByKey(RequestDiagnosticContext diagnosticContext, EnvProperties env,
            String key) {
        return getByKey(diagnosticContext, env.appName(), key);
    }

    public static @NotNull CbsRequest getByKey(
            RequestDiagnosticContext diagnosticContext,
            String serviceName,
            String key) {
        return new CbsRequestWithDiagnosticCtx(diagnosticContext, () -> "/" + key + "/" + serviceName);
    }

    public static @NotNull CbsRequest getAll(RequestDiagnosticContext diagnosticContext, EnvProperties env) {
        return getAll(diagnosticContext, env.appName());
    }

    public static @NotNull CbsRequest getAll(RequestDiagnosticContext diagnosticContext, String serviceName) {
        return new CbsRequestWithDiagnosticCtx(diagnosticContext, () -> "/service_component_all/" + serviceName);
    }

}
