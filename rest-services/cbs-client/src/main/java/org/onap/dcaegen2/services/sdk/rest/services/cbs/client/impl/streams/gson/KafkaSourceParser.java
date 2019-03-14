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

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.DataStreamUtils.assertStreamType;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils.gsonInstance;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.KafkaUtils.extractAafCredentials;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.KafkaUtils.extractKafkaInfo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.DataStreamDirection;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.RawDataStream;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.KafkaSource;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public class KafkaSourceParser implements StreamFromGsonParser<KafkaSource> {

    private final Gson gson;

    public static KafkaSourceParser create() {
        return new KafkaSourceParser(gsonInstance());
    }

    KafkaSourceParser(Gson gson) {
        this.gson = gson;
    }

    @Override
    public KafkaSource unsafeParse(RawDataStream<JsonObject> input) {
        assertStreamType(input, "kafka", DataStreamDirection.SOURCE);
        final JsonObject json = input.descriptor();

        final KafkaInfo kafkaInfo = extractKafkaInfo(gson, json);
        final AafCredentials aafCreds = extractAafCredentials(gson, json).getOrNull();

        return new GsonKafkaSource(input.name(), kafkaInfo, aafCreds);
    }
}
