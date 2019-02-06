/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.api.options;

import org.immutables.value.Value;

@Value.Immutable
public interface WireFrameVersion {
    short SUPPORTED_VERSION_MAJOR = 0x01;
    short SUPPORTED_VERSION_MINOR = 0x00;
    /***
     * Major version of Wire Transfer Protocol interface frame
     * @return major version of interface frame
     * @since 1.1.1
     */
    @Value.Default
    @Value.Parameter
    default short major() {
        return SUPPORTED_VERSION_MAJOR;
    }

    /***
     * Minor version of Wire Transfer Protocol interface frame
     * @return minor version of interface frame
     * @since 1.1.1
     */
    @Value.Default
    @Value.Parameter
    default short minor() {
        return SUPPORTED_VERSION_MINOR;
    }

    @Value.Check
    default void validate() {
        if (!(major() > 0 && minor() >=0)) {
            throw new IllegalArgumentException("Invalid version");
        }
    }

}

