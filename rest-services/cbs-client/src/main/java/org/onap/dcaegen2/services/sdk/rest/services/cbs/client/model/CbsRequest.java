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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model;

import java.util.UUID;
import org.immutables.value.Value;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;

/**
 * A recipe on which CBS endpoint to call. Usually you should use {@link org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsRequests}
 * which is a factory to each request type.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
@Value.Immutable
public interface CbsRequest {

    /**
     * The CBS request path. It will be created by the library.
     */
    RequestPath requestPath();

    /**
     * Diagnostic context as defined in Logging Guideline
     */
    RequestDiagnosticContext diagnosticContext();

    /**
     * Return a view on this CbsRequest with updated InvocationID.
     */
    default CbsRequest withNewInvocationId() {
        return ImmutableCbsRequest.copyOf(this).withDiagnosticContext(diagnosticContext().withNewInvocationId());
    }
}
