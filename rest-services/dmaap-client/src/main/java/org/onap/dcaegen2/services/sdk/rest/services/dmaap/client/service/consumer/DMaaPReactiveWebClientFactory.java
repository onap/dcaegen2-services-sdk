/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
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

import io.netty.handler.ssl.SslContext;
import javax.net.ssl.SSLException;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.ssl.SslFactory;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.CloudHttpClient;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 7/4/18
 */
public class DMaaPReactiveWebClientFactory {

    private final SslFactory sslFactory;
    public DMaaPReactiveWebClientFactory() {
        this(new SslFactory());
    }

    DMaaPReactiveWebClientFactory(SslFactory sslFactory) {
        this.sslFactory = sslFactory;
    }

    /**
     * Construct CloudHttpClient with appropriate settings.
     *
     * @return CloudHttpClient
     */

    public CloudHttpClient build(DmaapConsumerConfiguration consumerConfiguration) throws SSLException {
        SslContext sslContext = createSslContext(consumerConfiguration);
        return new CloudHttpClient(sslContext);
    }

    private SslContext createSslContext(DmaapConsumerConfiguration consumerConfiguration) throws SSLException {
        if (consumerConfiguration.enableDmaapCertAuth()) {
            return sslFactory.createSecureContext(
                    consumerConfiguration.keyStorePath(), consumerConfiguration.keyStorePasswordPath(),
                    consumerConfiguration.trustStorePath(), consumerConfiguration.trustStorePasswordPath()
            );
        }
        return sslFactory.createInsecureContext();
    }
}
