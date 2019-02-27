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

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
public final class OnapMdc {
    public static final String REQUEST_ID = "RequestID";
    public static final String CLIENT_NAME = "PartnerName";
    public static final String CLIENT_IP = "ClientIPAddress";
    public static final String INVOCATION_ID = "InvocationID";
    public static final String INVOCATION_TIMESTAMP = "InvokeTimestamp";
    public static final String STATUS_CODE = "StatusCode";
    public static final String INSTANCE_ID = "InstanceID";
    public static final String SERVER_FQDN = "ServerFQDN";

    static final String HTTP_HEADER_PREFIX = "X-";
}
