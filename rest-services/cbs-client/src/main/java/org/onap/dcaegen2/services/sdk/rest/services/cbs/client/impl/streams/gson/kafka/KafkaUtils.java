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

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils.optionalChild;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils.requiredChild;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.vavr.control.Option;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.ImmutableAafCredentials;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
final class KafkaUtils {

    private KafkaUtils() {
    }

    static KafkaInfo extractKafkaInfo(Gson gson, JsonObject input) {
        final JsonElement kafkaInfoJson = requiredChild(input, "kafka_info");
        return gson.fromJson(kafkaInfoJson, ImmutableKafkaInfo.class);
    }

    static Option<AafCredentials> extractAafCredentials(Gson gson, JsonObject input) {
        return optionalChild(input, "aaf_credentials")
                .map(aafCredsJson -> gson.fromJson(aafCredsJson, ImmutableAafCredentials.class));
    }
}
