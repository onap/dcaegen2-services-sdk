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

public final class MdcVariables {

    @Deprecated
    public static final String X_ONAP_REQUEST_ID = "X-ONAP-RequestID";
    @Deprecated
    public static final String X_INVOCATION_ID = "X-InvocationID";

    public static final String INSTANCE_UUID = "InstanceUUID";
    public static final String RESPONSE_CODE = "ResponseCode";
    public static final String REQUEST_ID = "RequestID";
    public static final String CLIENT_NAME = "PartnerName";
    public static final String CLIENT_IP = "ClientIPAddress";
    public static final String INVOCATION_ID = "InvocationID";
    public static final String INVOCATION_TIMESTAMP = "InvokeTimestamp";
    public static final String STATUS_CODE = "StatusCode";
    public static final String INSTANCE_ID = "InstanceID";
    public static final String SERVER_FQDN = "ServerFQDN";
    public static final String SERVICE_NAME = "ServiceName";
    public static final String CONTENT_TYPE = "Content-Type";


    private static final String HTTP_HEADER_PREFIX = "X-";

    private MdcVariables() {
    }

    public static String httpHeader(String mdcName) {
        return HTTP_HEADER_PREFIX + mdcName;
    }

    /**
     * @deprecated use {@link RequestDiagnosticContext#withSlf4jMdc(Runnable)}.
     * @param mdcContextMap
     */
    @Deprecated
    public static void setMdcContextMap(Map<String, String> mdcContextMap) {
        if (mdcContextMap != null) {
            MDC.setContextMap(mdcContextMap);
        }
    }
}
