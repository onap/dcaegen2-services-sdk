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
package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap.mr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.MessageRouter;

import java.util.Objects;

/**
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 */

abstract class GsonMessageRouter implements MessageRouter {
    private final String name;
    private final MessageRouterDmaapInfo dmaapInfo;
    private final AafCredentials aafCredentials;

    GsonMessageRouter(String name, @NotNull MessageRouterDmaapInfo dmaapInfo,
            @Nullable AafCredentials aafCredentials) {
        this.name = name;
        this.dmaapInfo = Objects.requireNonNull(dmaapInfo, "dmaapInfo");
        this.aafCredentials = aafCredentials;
    }

    public String name() {
        return name;
    }

    @Override
    public String topicUrl() {
        return dmaapInfo.topicUrl();
    }

    @Override
    public @Nullable String clientRole() {
        return dmaapInfo.clientRole();
    }

    @Override
    public @Nullable String clientId() {
        return dmaapInfo.clientId();
    }

    @Override
    public @Nullable String location() {
        return dmaapInfo.location();
    }

    @Override
    public @Nullable AafCredentials aafCredentials() {
        return aafCredentials;
    }
}