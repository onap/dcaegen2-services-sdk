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
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.DataRouterSource;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.dmaap.ImmutableDataRouterSource;

import static org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.GsonUtils.*;

/**
 * @author <a href="mailto:kornel.janiak@nokia.com">Kornel Janiak</a>
 */

public class DataRouterSourceParser implements StreamFromGsonParser<DataRouterSource> {
    private final Gson gson;

    public static DataRouterSourceParser create() {
        return new DataRouterSourceParser(gsonInstance());
    }

    private DataRouterSourceParser(Gson gson) {
        this.gson = gson;
    }

    public DataRouterSource unsafeParse(JsonObject input) {
        assertStreamType(input, "data_router");

        final JsonElement dmaapInfoJson = requiredChild(input, "dmaap_info");

        return gson.fromJson(dmaapInfoJson, ImmutableDataRouterSource.class);

    }

}