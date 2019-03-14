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

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.Kafka;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.KafkaSource;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
abstract class GsonKafka implements Kafka {

    final KafkaInfo kafkaInfo;
    private final AafCredentials aafCredentials;

    GsonKafka(@NotNull KafkaInfo kafkaInfo,
            @Nullable AafCredentials aafCredentials) {
        this.kafkaInfo = Objects.requireNonNull(kafkaInfo, "kafkaInfo");
        this.aafCredentials = aafCredentials;
    }

    @Override
    public String bootstrapServers() {
        return kafkaInfo.bootstrapServers();
    }

    @Override
    public String topicName() {
        return kafkaInfo.topicName();
    }

    @Override
    public @Nullable AafCredentials aafCredentials() {
        return aafCredentials;
    }

    @Override
    public @Nullable String clientRole() {
        return kafkaInfo.clientRole();
    }

    @Override
    public @Nullable String clientId() {
        return kafkaInfo.clientId();
    }

    @Override
    public int maxPayloadSizeBytes() {
        return kafkaInfo.maxPayloadSizeBytes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GsonKafka gsonKafka = (GsonKafka) o;
        return kafkaInfo.equals(gsonKafka.kafkaInfo) &&
                Objects.equals(aafCredentials, gsonKafka.aafCredentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kafkaInfo, aafCredentials);
    }
}
