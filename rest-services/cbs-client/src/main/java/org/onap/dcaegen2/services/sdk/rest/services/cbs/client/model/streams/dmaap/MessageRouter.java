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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.annotations.ExperimentalApi;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.AafCredentials;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
@ExperimentalApi
public interface MessageRouter {

    /**
     * URL for accessing the topic to publish or receive events.
     */
    @SerializedName("topic_url")
    String topicUrl();

    /**
     * AAF client role that’s requesting publish or subscribe access to the topic.
     */
    @SerializedName("client_role")
    @Nullable String clientRole();

    /**
     * Client id for given AAF client.
     */
    @SerializedName("client_id")
    @Nullable String clientId();

    /**
     * DCAE location for the publisher or subscriber, used to set up routing.
     */
    @SerializedName("location")
    @Nullable String location();

    /**
     * The AAF credentials.
     */
    @SerializedName("aaf_credentials")
    @Nullable AafCredentials aafCredentials();
}
