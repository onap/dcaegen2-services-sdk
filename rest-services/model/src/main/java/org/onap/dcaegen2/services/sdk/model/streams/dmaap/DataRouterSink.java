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
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.model.streams.SinkStream;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
@Gson.TypeAdapters
@Value.Immutable
public interface DataRouterSink extends DataRouter, SinkStream {

    /**
     * URL to which the publisher makes Data Router publish requests.
     */
    @SerializedName("publish_url")
    String publishUrl();

    /**
     * Publisher id in Data Router
     */
    @SerializedName("publisher_id")
    @Nullable String publisherId();

    /**
     * URL from which log data for the feed can be obtained.
     */
    @SerializedName("log_url")
    @Nullable String logUrl();

}
