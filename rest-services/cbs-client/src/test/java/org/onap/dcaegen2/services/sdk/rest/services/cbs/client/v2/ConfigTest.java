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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.v2;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class ConfigTest {

    @Test
    void listen_simple() {
        Config cut = new Config();

        final List<String> expectedChanges = List.of("1", "2", "3");
        final AtomicReference<List<String>> actualChanges = new AtomicReference<>(List.empty());

        cut.listen(List.of("some-key"), subtreeOption ->
                actualChanges.updateAndGet(
                        changes -> changes.append(subtreeOption.flatMap(subtree -> subtree.get()).getOrElse("[None]")))

        );

        final MerkleTree<String> initialConfig = MerkleTree.<String>emptyWithDefaultDigest(String::getBytes)
                .add(List.of("some-key"), "1");

        final MerkleTree<String> updatedConfig1 = initialConfig
                .add(List.of("some-key"), "2");

        final MerkleTree<String> updatedConfig2 = updatedConfig1
                .add(List.of("some-key"), "3");

        cut.update(initialConfig);
        cut.update(updatedConfig1);
        cut.update(updatedConfig2);

        assertThat(actualChanges.get()).isEqualTo(expectedChanges);

    }


    @Test
    void subtreeChanges_simple() {
        Config cut = new Config();

        final ReplayProcessor<String> replayProcessor = ReplayProcessor.create();
        final List<String> expectedChanges = List.of("1", "2", "3");

        cut.subtreeChanges(List.of("some-key"))
                .map(subtreeOption ->
                        subtreeOption.flatMap(subtree -> subtree.get()).getOrElse("[None]")
                )
                .subscribe(replayProcessor);

        final MerkleTree<String> initialConfig = MerkleTree.<String>emptyWithDefaultDigest(String::getBytes)
                .add(List.of("some-key"), "1");

        final MerkleTree<String> updatedConfig1 = initialConfig
                .add(List.of("some-key"), "2");

        final MerkleTree<String> updatedConfig2 = updatedConfig1
                .add(List.of("some-key"), "3");

        cut.update(initialConfig);
        cut.update(updatedConfig1);
        cut.update(updatedConfig2);

        StepVerifier.create(replayProcessor.take(expectedChanges.size()))
                .expectNextSequence(expectedChanges)
                .verifyComplete();
    }

    @Test
    void subtreeChanges_fancy() {
        Config cut = new Config();

        final ReplayProcessor<String> actualChanges = ReplayProcessor.create();
        final List<String> expectedChanges = List.of("http://dmaap/topic1", "http://dmaap/topic1-updated", "[None]");

        cut.subtreeChanges(List.of("streams", "publishes"))
                .map(subtreeOption ->
                        subtreeOption.flatMap(subtree -> subtree.get("topic1", "dmaap-url")).getOrElse("[None]")
                )
                .subscribe(actualChanges);

        final MerkleTree<String> initialConfig = MerkleTree.<String>emptyWithDefaultDigest(String::getBytes)
                .add(List.of("collector", "treshold"), "145")
                .add(List.of("collector", "listenPort"), "8080");

        final MerkleTree<String> updatedConfig1 = initialConfig
                .add(List.of("streams", "publishes", "topic1", "type"), "message-bus")
                .add(List.of("streams", "publishes", "topic1", "dmaap-url"), "http://dmaap/topic1");

        final MerkleTree<String> updatedConfig2 = updatedConfig1
                .add(List.of("streams", "publishes", "topic1", "type"), "message-bus")
                .add(List.of("streams", "publishes", "topic1", "dmaap-url"), "http://dmaap/topic1-updated");

        final MerkleTree<String> updatedConfig3 = updatedConfig2
                .add(List.of("collector", "treshold"), "1410");

        final MerkleTree<String> updatedConfig4 = initialConfig;

            cut.update(initialConfig);
            cut.update(updatedConfig1);
            cut.update(updatedConfig2);
            cut.update(updatedConfig3);
            cut.update(updatedConfig4);

        StepVerifier.create(actualChanges.take(expectedChanges.size()))
                .expectNextSequence(expectedChanges)
                .verifyComplete();

    }
}