/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error;

import java.util.Collections;

public class ClientErrorReasons {

    private ClientErrorReasons() { }

    public static final ClientErrorReason TIMEOUT = ImmutableClientErrorReason.builder()
            .header("408 Request Timeout")
            .text("Client timeout exception occurred, Error code is %1")
            .messageId("SVC0001")
            .variables(Collections.singletonList("408")).build();

}
