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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api;

import com.google.gson.JsonElement;
import java.time.Duration;
import org.onap.dcaegen2.services.sdk.rest.services.annotations.ExperimentalApi;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
@ExperimentalApi
public interface MessageRouterSubscriber {

    Mono<MessageRouterSubscribeResponse> get(MessageRouterSubscribeRequest request);

    default Flux<JsonElement> getElements(MessageRouterSubscribeRequest request) {
        return get(request)
                .doOnNext(response -> {
                    if (response.failed()) {
                        throw new IllegalStateException(response.failReason());
                    }
                })
                .filter(MessageRouterSubscribeResponse::hasElements)
                .flatMapMany(response -> Flux.fromIterable(response.items()));
    }

    default Flux<JsonElement> subscribeForElements(MessageRouterSubscribeRequest request, Duration period) {
        return Flux.interval(period).concatMap(i->getElements(request));
    }
}
