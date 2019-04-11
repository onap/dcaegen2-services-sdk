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

import com.google.gson.Gson;
import io.netty.handler.ssl.SslContext;
import io.vavr.Lazy;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.annotations.ExperimentalApi;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.MessageRouterPublisherImpl;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.MessageRouterSubscriberImpl;

/**
 * <b>WARNING</b>: This is a proof-of-concept. It is untested. API may change or be removed.  Use at your own risk.
 * You've been warned.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
@ExperimentalApi
public final class DmaapClientFactory {

    private static final Duration DEFAULT_MAX_BATCH_DURATION = Duration.ofSeconds(1);
    private static final int DEFAULT_MAX_BATCH_SIZE = 512;

    private DmaapClientFactory() {
    }

    public static @NotNull MessageRouterPublisher createMessageRouterPublisher() {
        return new MessageRouterPublisherImpl(
                RxHttpClient.create(),
                DEFAULT_MAX_BATCH_SIZE,
                DEFAULT_MAX_BATCH_DURATION);
    }

    public static @NotNull MessageRouterPublisher createMessageRouterPublisher(@NotNull SslContext sslContext) {
        return new MessageRouterPublisherImpl(
                RxHttpClient.create(sslContext),
                DEFAULT_MAX_BATCH_SIZE,
                DEFAULT_MAX_BATCH_DURATION);
    }

    public static @NotNull MessageRouterSubscriber createMessageRouterSubscriber() {
        return new MessageRouterSubscriberImpl(RxHttpClient.create(), new Gson());
    }

    public static @NotNull MessageRouterSubscriber createMessageRouterSubscriber(@NotNull SslContext sslContext) {
        return new MessageRouterSubscriberImpl(
                RxHttpClient.create(sslContext),
                new Gson());
    }
}
