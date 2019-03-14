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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.ImmutableKafkaSink;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.ImmutableKafkaSink.Builder;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.KafkaSink;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public class KafkaSinkParser implements StreamFromGsonParser<KafkaSink> {

    @Override
    public KafkaSink unsafeParse(JsonObject json) {
        final Builder result = ImmutableKafkaSink.builder();

        final JsonObject kafkaInfo = requiredChild(json, "kafka_info").getAsJsonObject();
        result.bootstrapServers(requiredString(kafkaInfo, "bootstrap_servers"));
        result.topicName(requiredString(kafkaInfo, "topic_name"));
        result.clientId(optionalString(kafkaInfo, "client_id"));
        result.clientRole(optionalString(kafkaInfo, "client_role"));
        
        return result.build();
    }

    private String requiredString(JsonObject parent, String childName) {
        return requiredChild(parent, childName).getAsString();
    }

    private String optionalString(JsonObject parent, String childName) {
        final JsonElement result = optionalChild(parent, childName);
        return result == null ? null : result.getAsString();
    }

    private JsonElement requiredChild(JsonObject parent, String childName) {
        if (parent.has(childName)) {
            return parent.get(childName);
        } else {
            throw new IllegalArgumentException(
                    "Could not find sub-node '" + childName + "'. Actual sub-nodes: " + stringifyChildrenNames(parent));
        }
    }

    private String stringifyChildrenNames(JsonObject parent) {
        return parent.entrySet().stream().map(Entry::getKey).collect(Collectors.joining(", "));
    }

    private JsonElement optionalChild(JsonObject parent, String childName) {
        return parent.has(childName) ? parent.get(childName) : null;
    }
}
