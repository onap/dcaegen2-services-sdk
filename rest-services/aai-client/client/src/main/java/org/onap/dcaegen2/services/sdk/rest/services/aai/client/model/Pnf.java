/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.model;

import com.google.gson.annotations.SerializedName;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiOptional;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;
import reactor.util.annotation.Nullable;

@AaiPojo(path = "/network/pnfs/pnf/${pnf-name}", type = "pnf")
interface Pnf {

    @AaiRequired("pnf-name")
    String getPnfName();

    @AaiOptional("pnf-name2")
    String getPnfName2();

    @AaiOptional("selflink")
    String getSelflink();

    @AaiOptional("pnf-name2-source")
    String getPnfName2Source();

    @AaiOptional("pnf-id")
    String getPnfId();

    @AaiOptional("equip-type")
    String getEquipType();

    @AaiOptional("equip-vendor")
    String getEquipVendor();

    @AaiOptional("equip-model")
    String getEquipModel();

    @AaiOptional("management-option")
    String getManagementOption();

    @AaiOptional("orchestration-status")
    String getOrchestrationStatus();

    @AaiOptional("ipaddress-v4-oam")
    String getIpaddressV4Oam();

    @AaiOptional("sw-version")
    String getSwVersion();

    @AaiOptional("in-maint")
    Boolean getInMaint();

    @AaiOptional("frame-id")
    String getFrameId();

    @AaiOptional("serial-number")
    String getSerialNumber();

    @AaiOptional("ipaddress-v4-loopback-0")
    String getIpaddressV4Loopback0();

    @AaiOptional("ipaddress-v6-loopback-0")
    String getIpaddressV6Loopback0();

    @AaiOptional("ipaddress-v4-aim")
    String getIpaddressV4Aim();

    @AaiOptional("ipaddress-v6-aim")
    String getIpaddressV6Aim();

    @AaiOptional("ipaddress-v6-oam")
    String getIpaddressV6Oam();

    @AaiOptional("inv-status")
    String getInvStatus();

    @Nullable
    @SerializedName("prov-status")
    String getProvStatus();

    @AaiOptional("nf-role")
    String getNfRole();

    @AaiOptional("admin-status")
    String getAdminStatus();

    @AaiOptional("operational-status")
    String getOperationalStatus();

    @AaiOptional("model-customization-id")
    String getModelCustomizationId();

    @AaiOptional("model-invariant-id")
    String getModelInvariantId();

    @AaiOptional("model-version-id")
    String getModelVersionId();

    @AaiOptional("pnf-ipv4-address")
    String getPnfIpv4Address();

    @AaiOptional("pnf-ipv6-address")
    String getPnfIpv6Address();
}
