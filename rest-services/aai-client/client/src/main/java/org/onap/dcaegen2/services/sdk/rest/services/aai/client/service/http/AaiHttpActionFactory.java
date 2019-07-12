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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http;

import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.utils.RelationParser.findRelatedTo;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.utils.RelationParser.toJsonObject;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.utils.RelationParser.toRelationType;

import java.util.function.Function;
import java.util.function.Supplier;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.ImmutableAaiRelation;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import reactor.core.publisher.Mono;

public class AaiHttpActionFactory extends AaiHttpAbstractActionFactory {

    public AaiHttpActionFactory(AaiClientConfiguration configuration) {
        super(configuration);
    }

    public AaiHttpActionFactory(
            AaiClientConfiguration configuration,
            Supplier<RxHttpClient> createInsecureClient,
            Function<SecurityKeys, RxHttpClient> createSecuredClient) {
        super(configuration, createInsecureClient, createSecuredClient);
    }


    @Override
    protected <T extends AaiModel> AaiCreateAction<T> create(Class<T> type) {
        return AaiRequestsBuilder
                .buildCreateAction(
                        getConverter(),
                        getHeaders(),
                        build(),
                        getUrlMapping(type));
    }

    @Override
    protected <T extends AaiModel> AaiUpdateAction<T> update(Class<T> type) {
        return AaiRequestsBuilder
                .buildUpdateAction(
                        getConverter(),
                        getHeaders(),
                        build(),
                        getUrlMapping(type));
    }

    @Override
    protected <T extends AaiDeletable> AaiDeleteAction<T> delete(Class<T> type) {
        return AaiRequestsBuilder
                .buildDeleteAction(
                        getHeaders(),
                        build(),
                        getUrlMapping(type));
    }

    @Override
    protected <T extends AaiModel, U extends T> AaiGetAction<T, U> get(Class<T> req, Class<U> full) {
        return AaiRequestsBuilder
                .buildGetAction(
                        getConverter(),
                        full,
                        getHeaders(),
                        build(),
                        getUrlMapping(req));
    }

    @Override
    protected <T extends AaiRelationable, U extends AaiModel> AaiGetRelationAction<T, U>
    getRelation(Class<T> from, Class<U> to) {
        return item -> findRelatedTo(item.getRelationshipList(), getAaiType(to))
                .map(rel -> (AaiRelation<T, U>) ImmutableAaiRelation
                        .<T, U>builder()
                        .from(item)
                        .to(getConverter().fromJson(toJsonObject(rel, getConverter()), to))
                        .relationType(toRelationType(rel.getRelationshipLabel()))
                        .build()
                ).map(Mono::just)
                .getOrElse(Mono::empty);
    }

    @Override
    protected <T extends AaiModel, U extends AaiModel> AaiAddRelationAction<T, U>
    addRelation(Class<T> from, Class<U> to) {
        return AaiRequestsBuilder
                .buildAddRelation(
                        getConverter(),
                        build(),
                        getHeaders(),
                        getUrlMapping(from).andThen(this::addRelationUrlPart),
                        getUrlMapping(to));
    }

    @Override
    protected <T extends AaiDeletable & AaiRelationable, U extends AaiModel> AaiDeleteRelationAction<T, U>
    deleteRelation(Class<T> from, Class<U> to) {
        return AaiRequestsBuilder
                .buildDeleteRelation(
                        getConverter(),
                        build(),
                        getHeaders(),
                        getUrlMapping(from).andThen(this::addRelationUrlPart),
                        getAaiType(to));
    }
}