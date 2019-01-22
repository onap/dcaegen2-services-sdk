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
package org.onap.dcaegen2.services.sdk.services.hvves.client.producer.impl.utils;

public enum VesEventDomain {
    FAULT("fault"),
    HEARTBEAT("heartbeat"),
    MEASUREMENT("measurement"),
    MOBILE_FLOW("mobileFlow"),
    OTHER("other"),
    PNF_REGISTRATION("pnfRegistration"),
    SIP_SIGNALING("sipSignaling"),
    STATE_CHANGE("stateChange"),
    SYSLOG("syslog"),
    THRESHOLD_CROSSING_ALERT("thresholdCrossingAlert"),
    VOICE_QUALITY("voiceQuality"),
    PERF3GPP("perf3gpp");

    private String name;

    VesEventDomain(String name) {
        this.name = name;
    }

    public String domainName() {
        return name;
    }
}
