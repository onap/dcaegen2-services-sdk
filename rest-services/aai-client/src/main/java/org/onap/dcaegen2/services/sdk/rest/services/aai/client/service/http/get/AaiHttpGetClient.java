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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.get;

import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.model.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.ssl.SslFactory;
import org.onap.dcaegen2.services.sdk.rest.services.uri.URI;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class AaiHttpGetClient {

    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;
    private final String aaiBasePath;
    private final String aaiPnfPath;



    public AaiHttpGetClient(SslFactory sslFactory, AaiClientConfiguration configuration) {
        this.aaiHost = configuration.aaiHost();
        this.aaiProtocol = configuration.aaiProtocol();
        this.aaiHostPortNumber = configuration.aaiPort();
        this.aaiBasePath = configuration.aaiBasePath();
        this.aaiPnfPath = configuration.aaiPnfPath();
    }

    private Mono<String> getAaiRequest(AaiModel model, HttpClient httpClient) {
        return httpClient
                .baseUrl(getUri(model.getCorrelationId()))
                .get()
                .responseContent()
                .aggregate()
                .asString();
    }


    String getUri(String pnfName) {
        return new URI.URIBuilder()
                .scheme(aaiProtocol)
                .host(aaiHost)
                .port(aaiHostPortNumber)
                .path(aaiBasePath + aaiPnfPath + "/" + pnfName).build().toString();
    }
}
