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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener;

import com.google.gson.JsonElement;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;

enum JsonElementType {
    JSON_NULL,
    JSON_PRIMITIVE,
    JSON_ARRAY,
    JSON_OBJECT;

    @NotNull
    static JsonElementType typeOf(Tuple2<?, JsonElement> e) {
        return JsonElementType.typeOf(e._2);
    }

    static JsonElementType typeOf(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return JSON_PRIMITIVE;
        } else if (json.isJsonArray()) {
            return JSON_ARRAY;
        } else if (json.isJsonObject()) {
            return JSON_OBJECT;
        }
        return JSON_NULL;
    }
}
