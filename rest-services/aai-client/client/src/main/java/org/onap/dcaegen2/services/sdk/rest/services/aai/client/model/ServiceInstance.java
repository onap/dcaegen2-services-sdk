/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiOptional;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;

@AaiPojo(path = "/business/customers/customer/${global-customer-id}"
        + "/service-subscriptions/service-subscription/${service-type}"
        + "/service-instances/service-instance/${service-instance-id}",
        type = "service-instance")
public interface ServiceInstance {

    @AaiRequired("service-instance-id")
    String getServiceInstanceId();

    @AaiRequired("service-type")
    String getServiceType();

    @AaiRequired("global-customer-id")
    String globalCustomerId();

    
    @AaiOptional("service-instance-name")
    String getServiceInstanceName();

    
    @AaiOptional("service-role")
    String getServiceRole();

    
    @AaiOptional("environment-context")
    String getEnvironmentContext();

    
    @AaiOptional("workload-context")
    String getWorkloadContext();

    
    @AaiOptional("created-at")
    String getCreatedAt();

    
    @AaiOptional("updated-at")
    String getUpdatedAt();

    
    @AaiOptional("description")
    String getDescription();

    
    @AaiOptional("model-invariant-id")
    String getModelInvariantId();

    
    @AaiOptional("model-version-id")
    String getModelVersionId();

    
    @AaiOptional("persona-model-version")
    String getPersonaModelVersion();

    
    @AaiOptional("widget-model-id")
    String getWidgetModelId();

    
    @AaiOptional("widget-model-version")
    String getWidgetModelVersion();

    
    @AaiOptional("bandwidth-total")
    String getBandwidthTotal();

    
    @AaiOptional("vhn-portal-url")
    String getVhnPortalUrl();

    
    @AaiOptional("service-instance-location-id")
    String getServiceInstanceLocationId();

    @AaiOptional("selflink")
    String getSelflink();

    
    @AaiOptional("orchestration-status")
    String getOrchestrationStatus();

    
    @AaiOptional("input-parameters")
    String getInputParameters();
}
