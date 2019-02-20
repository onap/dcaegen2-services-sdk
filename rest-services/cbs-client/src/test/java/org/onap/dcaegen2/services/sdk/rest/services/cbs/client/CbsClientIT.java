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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client;

import com.google.gson.JsonObject;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.dcae.StreamsParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener.ListenableCbsConfig;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener.MerkleTree;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener.MerkleTreeParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class CbsClientIT {

    @Test
    void test() {
        final Flux<MerkleTree<String>> updates = configurationUpdates().take(10);
        ListenableCbsConfig listenableCbsConfig = new ListenableCbsConfig();
        StreamsParser streamsParser = new StreamsParser();

        listenableCbsConfig.subtreeChanges(List.of("stream_publishes"))
                .subscribeOn(Schedulers.parallel())
                .map(Option::get)
                .map(streamsParser::parse)
                .map(Stream::toList)
                .map(Object::toString)
                .subscribe(System.out::println);

        listenableCbsConfig.subtreeChanges(List.of("collector"))
                .map(Option::get)
                .map(collectorConfig -> collectorConfig.get("timeoutSec").getOrElse("360"))
                .subscribe(System.out::println);

        listenableCbsConfig.subscribeForUpdates(updates).block();
    }

    private Flux<MerkleTree<String>> configurationUpdates() {
        MerkleTreeParser merkleTreeParser = new MerkleTreeParser();
        return callCbsFactory()
                .flatMapMany(cbsClient -> cbsClient.updates("hv-ves", Duration.ZERO, Duration.ofMillis(100)))
                .cast(JsonObject.class)
                .map(merkleTreeParser::fromJsonObject);
    }

    private Mono<CbsClient> callCbsFactory() {
        List<String> jsonFiles = List.of(
                "/sample-configs/simple-0.json",
                "/sample-configs/simple-0.json",
                "/sample-configs/simple-1.json",
                "/sample-configs/simple-2.json");
        return Mono.just(DummyCbsClient.fromResources(jsonFiles.toStream().cycle()));
    }

}

