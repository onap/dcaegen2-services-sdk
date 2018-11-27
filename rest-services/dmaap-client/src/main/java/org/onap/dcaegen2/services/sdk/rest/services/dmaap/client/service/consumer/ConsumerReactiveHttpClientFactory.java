/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.service.consumer;

import javax.net.ssl.SSLException;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 6/26/18
 */
public class ConsumerReactiveHttpClientFactory {

    private final DMaaPReactiveWebClientFactory reactiveWebClientFactory;

    public ConsumerReactiveHttpClientFactory(DMaaPReactiveWebClientFactory reactiveWebClientFactory) {
        this.reactiveWebClientFactory = reactiveWebClientFactory;
    }

    public DMaaPConsumerReactiveHttpClient create(DmaapConsumerConfiguration consumerConfiguration)
            throws SSLException {
        return new DMaaPConsumerReactiveHttpClient(consumerConfiguration,
                reactiveWebClientFactory.build(consumerConfiguration));
    }

}
