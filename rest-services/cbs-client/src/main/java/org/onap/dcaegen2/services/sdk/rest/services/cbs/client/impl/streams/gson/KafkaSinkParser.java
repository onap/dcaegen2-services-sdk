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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.KafkaSink;

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils.*;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.StreamsConstants.KAFKA_INFO_CHILD_NAME;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.StreamsConstants.KAFKA_TYPE;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public final class KafkaSinkParser implements StreamFromGsonParser<KafkaSink> {
    private final Gson gson;

    public static KafkaSinkParser create() {
        return new KafkaSinkParser(gsonInstance());
    }

    private KafkaSinkParser(Gson gson) {
        this.gson = gson;
    }

    @Override
    public KafkaSink unsafeParse(JsonObject input) {
        assertStreamType(input, KAFKA_TYPE);

        final JsonElement kafkaInfoJson = requiredChild(input, KAFKA_INFO_CHILD_NAME);
        final KafkaInfo kafkaInfo = gson.fromJson(kafkaInfoJson, ImmutableKafkaInfo.class);

        return new GsonKafkaSink(kafkaInfo, null);
    }
}
