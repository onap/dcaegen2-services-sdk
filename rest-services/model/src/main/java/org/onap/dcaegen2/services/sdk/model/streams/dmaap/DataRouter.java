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
package org.onap.dcaegen2.services.sdk.model.streams.dmaap;


import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public interface DataRouter {

    /**
     * DCAE location for the publisher, used to set up routing.
     */
    @SerializedName("location")
    @Nullable String location();

    /**
     * Username
     * <ul>
     * <li>Data Router uses to authenticate to the subscriber when delivering files OR</li>
     * <li>the publisher uses to authenticate to Data Router.</li>
     * </ul>
     */
    @SerializedName("username")
    @Nullable String username();

    /**
     * Password
     * <ul>
     * <li>Data Router uses to authenticate to the subscriber when delivering files OR</li>
     * <li>the publisher uses to authenticate to Data Router.</li>
     * </ul>
     */
    @SerializedName("password")
    @Nullable String password();
}
