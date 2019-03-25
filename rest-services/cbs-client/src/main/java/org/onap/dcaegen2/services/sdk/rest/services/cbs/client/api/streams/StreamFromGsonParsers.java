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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams;

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap.dr.DataRouterSinkParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap.dr.DataRouterSourceParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap.mr.MessageRouterSinkParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap.mr.MessageRouterSourceParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.kafka.KafkaSinkParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.kafka.KafkaSourceParser;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.*;

/**
 * Factory methods for GSON-based {@code StreamParser}s
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
public final class StreamFromGsonParsers {

    private StreamFromGsonParsers() {
    }

    /**
     * Creates a stream parser capable of parsing Kafka sinks.
     *
     * @return a stream parser
     */
    public static StreamFromGsonParser<KafkaSink> kafkaSinkParser() {
        return KafkaSinkParser.create();
    }

    /**
     * Creates a stream parser capable of parsing Kafka sources.
     *
     * @return a stream parser
     */
    public static StreamFromGsonParser<KafkaSource> kafkaSourceParser() {
        return KafkaSourceParser.create();
    }

    /**
     * Creates a stream parser capable of parsing DMaaP Message Router sinks.
     *
     * @return a stream parser
     */
    public static StreamFromGsonParser<MessageRouterSink> messageRouterSinkParser() {
        return MessageRouterSinkParser.create();
    }

    /**
     * Creates a stream parser capable of parsing DMaaP Message Router sources.
     *
     * @return a stream parser
     */
    public static StreamFromGsonParser<MessageRouterSource> messageRouterSourceParser() {
        return MessageRouterSourceParser.create();
    }

    /**
     * Creates a stream parser capable of parsing DMaaP Data Router sinks.
     *
     * @return a stream parser
     */
    public static StreamFromGsonParser<DataRouterSink> dataRouterSinkParser() {
        return DataRouterSinkParser.create();
    }

    /**
     * Creates a stream parser capable of parsing DMaaP Data Router sources.
     *
     * @return a stream parser
     */
    public static StreamFromGsonParser<DataRouterSource> dataRouterSourceParser() {
        return DataRouterSourceParser.create();
    }
}
