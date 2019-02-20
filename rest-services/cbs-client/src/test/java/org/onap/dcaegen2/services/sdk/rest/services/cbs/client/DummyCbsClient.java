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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.vavr.collection.Stream;
import java.io.InputStreamReader;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.CbsClient;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
class DummyCbsClient implements CbsClient {
    private static final JsonParser JSON_PARSER = new JsonParser();
    private Stream<JsonElement> consecutiveJsonResults;

    DummyCbsClient(Stream<JsonElement> consecutiveJsonResults) {
        this.consecutiveJsonResults = consecutiveJsonResults;
    }

    static DummyCbsClient fromResources(Stream<String> resources) {
        return new DummyCbsClient(
                resources
                        .map(DummyCbsClient.class::getResourceAsStream)
                        .map(InputStreamReader::new)
                        .map(JSON_PARSER::parse));
    }

    @Override
    public @NotNull Mono<JsonElement> get(String serviceComponentName) {
        return consecutiveJsonResults.headOption().fold(Mono::empty, head -> {
            consecutiveJsonResults = consecutiveJsonResults.tail();
            return Mono.just(head);
        });
    }
}
