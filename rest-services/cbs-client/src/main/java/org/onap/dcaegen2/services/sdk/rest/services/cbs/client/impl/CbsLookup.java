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

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.impl;

import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.model.CbsClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since February 2019
 */
public class CbsLookup {

    private static final Logger LOGGER = LoggerFactory.getLogger(CbsLookup.class);

    public Mono<InetSocketAddress> lookup(CbsClientConfiguration configuration) {
        return Mono.just(createCbsAddress(configuration))
                .doOnNext(this::logCbsServiceAddress);
    }

    private InetSocketAddress createCbsAddress(CbsClientConfiguration configuration) {
        return InetSocketAddress.createUnresolved(
                configuration.hostname(),
                configuration.port());
    }

    private void logCbsServiceAddress(InetSocketAddress address) {
        LOGGER.info("Config Binding Service address: {}", address);
    }

}
