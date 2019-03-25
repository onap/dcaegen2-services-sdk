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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.kafka;

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.DataStreamUtils.assertStreamType;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils.gsonInstance;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.kafka.KafkaUtils.extractAafCredentials;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.kafka.KafkaUtils.extractKafkaInfo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.model.streams.StreamType;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.KafkaSink;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public final class KafkaSinkParser implements StreamFromGsonParser<KafkaSink> {
    private final Gson gson;

    private KafkaSinkParser(Gson gson) {
        this.gson = gson;
    }

    public static KafkaSinkParser create() {
        return new KafkaSinkParser(gsonInstance());
    }

    @Override
    public KafkaSink unsafeParse(RawDataStream<JsonObject> input) {
        assertStreamType(input, StreamType.KAFKA, DataStreamDirection.SINK);
        final JsonObject json = input.descriptor();

        final KafkaInfo kafkaInfo = extractKafkaInfo(gson, json);
        final AafCredentials aafCreds = extractAafCredentials(gson, json).getOrNull();

        return new GsonKafkaSink(input.name(), kafkaInfo, aafCreds);
    }
}
