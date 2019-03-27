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
import io.vavr.Lazy;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.annotations.ExperimentalApi;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.MessageRouterClientImpl;

/**
 * <b>WARNING</b>: This is a proof-of-concept. It is untested. API may change or be removed.  Use at your own risk.
 * You've been warned.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.4
 */
@ExperimentalApi
public final class DmaapClientFactory {

    private static final Lazy<MessageRouterClientImpl> THE_CLIENT = Lazy.of(() ->
            new MessageRouterClientImpl(RxHttpClient.create(), new Gson()));

    private DmaapClientFactory() {
    }

    public static @NotNull MessageRouterPublisher createMessageRouterPublisher() {
        return THE_CLIENT.get();
    }

    public static @NotNull MessageRouterSubscriber createMessageRouterSubscriber() {
        return THE_CLIENT.get();
    }
}
