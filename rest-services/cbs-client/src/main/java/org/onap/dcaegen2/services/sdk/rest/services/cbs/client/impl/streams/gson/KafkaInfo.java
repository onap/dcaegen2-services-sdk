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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
@Value.Immutable
@Gson.TypeAdapters
public interface KafkaInfo {

    @SerializedName("bootstrap_servers")
    String bootstrapServers();

    @SerializedName("topic_name")
    String topicName();

    @SerializedName("consumer_group_id")
    @Nullable String consumerGroupId();

    @SerializedName("client_role")
    @Nullable String clientRole();

    @SerializedName("client_id")
    @Nullable String clientId();

    @Value.Default
    @SerializedName("max_payload_size_bytes")
    default int maxPayloadSizeBytes() {
        return 1024 * 1024;
    }
}
