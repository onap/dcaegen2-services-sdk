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

package org.onap.dcaegen2.services.sdk.standardization.moher.health.api;

import io.vavr.Function0;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface HealthProvider {
    Mono<Health> currentHealth();

    static HealthProvider fromFunction(Function0<Health> function) {
        return () -> Mono.fromCallable(function::apply);
    }

    static HealthProvider fromSupplier(Supplier<Health> function) {
        return () -> Mono.fromCallable(function::get);
    }
}