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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl.streams.gson.dmaap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.streams.ImmutableAafCredentials;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public class DmaapUtils {

    public static final ImmutableAafCredentials EMPTY_CREDENTIALS = ImmutableAafCredentials.builder().build();

    private DmaapUtils() {
    }

    public static @Nullable AafCredentials extractAafCredentials(Gson gson, JsonObject input) {
        final AafCredentials aafCredentials = gson.fromJson(input, ImmutableAafCredentials.class);
        return EMPTY_CREDENTIALS.equals(aafCredentials) ? null : aafCredentials;
    }
}
