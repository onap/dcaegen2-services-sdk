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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.SinkStream;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @version 1.2.1
 */
@Gson.TypeAdapters
@ExperimentalApi
@Value.Immutable
public interface DataRouterSink extends DataRouter, SinkStream {

    @SerializedName(value = "publish_url", alternate = "publishUrl")
    String publishUrl();

    @SerializedName(value = "publisher_id", alternate = "publisherId")
    @Nullable String publisherId();

    @SerializedName(value = "log_url", alternate = "logUrl")
    @Nullable String logUrl();

}
