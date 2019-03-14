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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams.StreamFromGsonParser;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.ImmutableAafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.MessageRouterSink;

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.StreamsConstants.DMAAP_INFO_CHILD_NAME;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.StreamsConstants.MESSAGE_ROUTER_TYPE;
import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils.*;

public final class MessageRouterSinkParser implements StreamFromGsonParser<MessageRouterSink> {

    private final Gson gson;

    public static MessageRouterSinkParser create() {
        return new MessageRouterSinkParser(gsonInstance());
    }

    private MessageRouterSinkParser(Gson gson) {
        this.gson = gson;
    }

    public MessageRouterSink unsafeParse(JsonObject input) {
        assertStreamType(input, MESSAGE_ROUTER_TYPE);

        final AafCredentials aafCredentials = gson.fromJson(input, ImmutableAafCredentials.class);

        final JsonElement dmaapInfoJson = requiredChild(input, DMAAP_INFO_CHILD_NAME);
        final MessageRouterDmaapInfo dmaapInfo = gson.fromJson(dmaapInfoJson, ImmutableMessageRouterDmaapInfo.class);

        return new GsonMessageRouterSink(dmaapInfo, aafCredentials);

    }
}
