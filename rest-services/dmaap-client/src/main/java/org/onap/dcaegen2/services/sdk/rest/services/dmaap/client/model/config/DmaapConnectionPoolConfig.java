/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
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
package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config;

import org.immutables.value.Value;
import reactor.netty.resources.ConnectionProvider;

@Value.Immutable
public interface DmaapConnectionPoolConfig {

    @Value.Default
    default int connectionPool(){
        return ConnectionProvider.DEFAULT_POOL_MAX_CONNECTIONS;
    }
    @Value.Default
    default int maxLifeTime(){
        return Integer.MAX_VALUE;
    }
    @Value.Default
    default int maxIdleTime(){
        return  Integer.MAX_VALUE;
    }
}