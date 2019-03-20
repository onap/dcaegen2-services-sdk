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
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.annotations.ExperimentalApi;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.SourceStream;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
@Gson.TypeAdapters
@ExperimentalApi
@Value.Immutable
public interface DataRouterSource extends DataRouter, SourceStream {

    /**
     * URL to which the Data Router should deliver files
     */
    // TODO: since crucial, we need to verify if it should be non-null
    @SerializedName("delivery_url")
    @Nullable String deliveryUrl();

    /**
     * Subscriber id in Data Router
     */
    @SerializedName("subscriber_id")
    @Nullable String subscriberId();
}
