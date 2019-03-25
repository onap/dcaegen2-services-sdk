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

import static io.vavr.Predicates.not;

import io.vavr.collection.List;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.model.streams.AafCredentials;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public interface Kafka {

    /**
     * Kafka bootstrap servers as defined in Kafka client documentation under <em>bootstrap.servers</em> configuration
     * key.
     */
    String bootstrapServers();

    /**
     * The name of the topic where application should publish or subscribe for the messages.
     */
    String topicName();

    /**
     * The credentials to use when authenticating to Kafka cluster or null when connection should be unauthenticated.
     */
    @Nullable AafCredentials aafCredentials();

    /**
     * AAF client role that’s requesting publish or subscribe access to the topic.
     */
    @Nullable String clientRole();

    /**
     * Client id for given AAF client.
     */
    @Nullable String clientId();

    /**
     * The limit on the size of message published to/subscribed from the topic. Can be used to set Kafka client
     * <em>max.request.size</em> configuration property.
     */
    @Value.Default
    default int maxPayloadSizeBytes() {
        return 1024 * 1024;
    }

    /**
     * The {@code bootstrapServers} converted to the list of servers' addresses.
     */
    @Value.Derived
    default List<String> bootstrapServerList() {
        return List.of(bootstrapServers().split(",")).filter(not(String::isEmpty));
    }
}
