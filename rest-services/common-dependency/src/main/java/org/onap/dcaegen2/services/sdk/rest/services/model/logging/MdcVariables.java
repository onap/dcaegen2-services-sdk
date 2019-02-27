/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.model.logging;

import org.slf4j.MDC;

import java.util.Map;

/**
 * @deprecated Use new API from {@link RequestDiagnosticContext} class.
 */
@Deprecated
public final class MdcVariables {

    public static final String X_ONAP_REQUEST_ID = "X-ONAP-RequestID";
    public static final String X_INVOCATION_ID = "X-InvocationID";
    public static final String REQUEST_ID = OnapMdc.REQUEST_ID;
    public static final String INVOCATION_ID = OnapMdc.INVOCATION_ID;
    public static final String INSTANCE_UUID = "InstanceUUID";
    public static final String RESPONSE_CODE = "ResponseCode";
    public static final String SERVICE_NAME = "ServiceName";

    private MdcVariables() {
    }

    public static void setMdcContextMap(Map<String, String> mdcContextMap) {
        if (mdcContextMap != null) {
            MDC.setContextMap(mdcContextMap);
        }
    }
}
