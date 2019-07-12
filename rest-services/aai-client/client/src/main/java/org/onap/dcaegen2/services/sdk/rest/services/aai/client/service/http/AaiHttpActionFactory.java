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

import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.JsonHelpers.mergeObjects;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser.findRelatedTo;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser.toRelationType;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.Unit.UNIT;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.JsonHelpers;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClientFactory;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import reactor.core.publisher.Mono;

public class AaiHttpActionFactory extends AaiHttpAbstractActionFactory {
    private final AaiRequestsBuilder requestsBuilder;

    public AaiHttpActionFactory(AaiClientConfiguration configuration) {
        this(configuration, RxHttpClientFactory::createInsecure, RxHttpClientFactory::create);
    }

    public AaiHttpActionFactory(
            AaiClientConfiguration configuration,
            Supplier<RxHttpClient> createInsecureClient,
            Function<SecurityKeys, RxHttpClient> createSecuredClient) {
        super(configuration, createInsecureClient, createSecuredClient);

        this.requestsBuilder = new AaiRequestsBuilder(getConverter(), build(), getHeaders());
    }

    private <U extends AaiModel> U convertToObject(Class<U> to, Relationship relation) {
        final JsonObject asJson = RelationParser.toJsonObject(relation, getConverter());

        return getConverter().fromJson(asJson, to);
    }

    @Override
    protected <T extends AaiModel> AaiCreateAction<T> create(Class<T> type) {
        return model -> requestsBuilder
                .buildCreateAction(getUrl(type, model), model)
                .map(omit -> UNIT);
    }

    @Override
    protected <T extends AaiModel> AaiUpdateAction<T> update(Class<T> type) {
        return model -> requestsBuilder
                .buildUpdateAction(getUrl(type, model), model)
                .map(omit -> UNIT);
    }

    @Override
    protected <T extends AaiDeletable> AaiDeleteAction<T> delete(Class<T> type) {
        return model -> requestsBuilder
                .buildDeleteAction(getUrl(type, model), model)
                .map(omit -> UNIT);
    }


    private <T extends AaiModel> Optional<JsonObject> mergeFromJsonObject(T model, Class<T> req, JsonObject from) {
        return JsonHelpers
                .toJsonObject(model, req, getConverter())
                .map(obj -> mergeObjects(from, obj))
                .toJavaOptional();
    }

    @Override
    protected <T extends AaiModel, U extends T> AaiGetAction<T, U> get(Class<T> req, Class<U> full) {
        final JsonParser parser = new JsonParser();

        return model -> requestsBuilder
                .buildGetAction(getUrl(req, model))
                .map(HttpResponse::bodyAsString)
                .map(parser::parse)
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(obj -> mergeFromJsonObject(model, req, obj))
                .flatMap(Mono::justOrEmpty)
                .map(obj -> getConverter().fromJson(obj, full));
    }

    @Override
    protected <T extends AaiRelationable, U extends AaiModel>
        AaiGetRelationAction<T, U> getRelation(Class<U> to) {

        return new AaiGetRelationAction<T, U>() {
            @Override
            public <S extends T> Mono<AaiRelation<S, U>> callT(S model) {
                return findRelatedTo(model.getRelationshipList(), getAaiType(to))
                        .map(toRel -> new AaiRelation<>(model, convertToObject(to, toRel), toRelationType(toRel)))
                        .map(Mono::just)
                        .getOrElse(Mono::empty);
            }
        };
    }

    @Override
    protected <T extends AaiModel, U extends AaiModel>
        AaiAddRelationAction<T, U> addRelation(Class<T> from, Class<U> to) {

        return relation -> requestsBuilder
                .buildAddRelation(
                        addRelationUrlPart(getUrl(from, relation.from())),
                        getUrl(to, relation.to()),
                        relation
                ).map(omit -> UNIT);

    }

    @Override
    protected <T extends AaiDeletable & AaiRelationable, U extends AaiModel>
        AaiDeleteRelationAction<T, U> deleteRelation(Class<T> from, Class<U> to) {

        return relation -> requestsBuilder
                .buildDeleteRelation(
                        addRelationUrlPart(getUrl(from, relation.from())),
                        getAaiType(to),
                        relation
                ).map(omit -> UNIT);
    }
}