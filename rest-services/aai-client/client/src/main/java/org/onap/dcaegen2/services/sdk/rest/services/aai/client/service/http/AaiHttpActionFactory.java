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

import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration.copyOf;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.JsonHelpers.mergeObjects;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser.findRelatedTo;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser.toJsonObject;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser.toRelationType;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.Unit.UNIT;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationType.UNKNOWN;
import static reactor.core.publisher.Flux.just;
import static reactor.core.publisher.Mono.justOrEmpty;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.vavr.control.Try;
import java.util.function.Function;
import java.util.function.Supplier;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.ImmutableAaiClientConfiguration;
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
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiActionFactoryBase;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClientFactory;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AaiHttpActionFactory extends AaiHttpAbstractActionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AaiHttpActionFactory.class);

    private final JsonParser parser = new JsonParser();
    private final AaiRequestsBuilder requestsBuilder;

    /**
     * Specific {@link AaiActionFactoryBase} implementation. Uses default RxHttpClient factory methods.
     *
     * @param configuration http client configuration options {@link AaiClientConfiguration}.
     */
    public AaiHttpActionFactory(AaiClientConfiguration configuration) {
        this(
                configuration,
                RxHttpClientFactory::create,
                RxHttpClientFactory::createInsecure,
                RxHttpClientFactory::create);
    }

    /**
     * Specific {@link AaiActionFactoryBase} implementation. Uses HTTP api.
     *
     * @param configuration http client configuration options {@link AaiClientConfiguration}.
     * @param createHttpClient used for creation of RxHttpClient without SSL.
     * @param createInsecureHttpsClient used for creation of RxHttpClient with SSL that accepts any certificate.
     * @param createSecuredHttpsClient  used for creation of RxHttpClient with SSL.
     * @throws NullPointerException if one of params is null.
     */
    public AaiHttpActionFactory(
            AaiClientConfiguration configuration,
            Supplier<RxHttpClient> createHttpClient,
            Supplier<RxHttpClient> createInsecureHttpsClient,
            Function<SecurityKeys, RxHttpClient> createSecuredHttpsClient) {
        super(copyOf(configuration)
                .withBaseUrl(configuration.baseUrl().replaceFirst("^(?i)http[s]?://", "")));

        this.requestsBuilder = new AaiRequestsBuilder(
                getConverter(),
                build(configuration.baseUrl(), createHttpClient, createInsecureHttpsClient, createSecuredHttpsClient),
                getHeaders());
    }

    protected RxHttpClient build(
            String baseUrl,
            Supplier<RxHttpClient> createHttpClient,
            Supplier<RxHttpClient> createInsecureHttpsClient,
            Function<SecurityKeys, RxHttpClient> createSecuredHttpsClient) {

        if (baseUrl.startsWith("https")) {
            LOGGER.debug("Setting ssl context");

            if (configuration.enableAaiCertAuth()) {
                LOGGER.debug("Secure client has been created");

                return createSecuredHttpsClient.apply(createSslKeys());
            } else {
                LOGGER.debug("Insecure client has been created");

                return createInsecureHttpsClient.get();
            }
        } else {
            return createHttpClient.get();
        }
    }

    private <U extends AaiModel> U convertToObject(Class<U> to, Relationship relation) {
        final JsonObject asJson = RelationParser.toJsonObject(getConverter(), relation);

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


    private <T extends AaiModel> Mono<JsonObject> mergeFromJsonObject(T model, Class<T> req, JsonObject from) {
        return justOrEmpty(JsonHelpers
                .toJsonObject(getConverter(), model, req)
                .map(obj -> mergeObjects(from, obj))
                .toJavaOptional());
    }

    private Mono<JsonObject> responseAsJson(HttpResponse resp) {
        return justOrEmpty(Try
                .of(() -> parser.parse(resp.bodyAsString()))
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .toJavaOptional());
    }

    @Override
    protected <T extends AaiModel, U extends T> AaiGetAction<T, U> get(Class<T> req, Class<U> full) {
        return model -> requestsBuilder
                .buildGetAction(getUrl(req, model))
                .flatMap(this::responseAsJson)
                .flatMap(obj -> mergeFromJsonObject(model, req, obj))
                .map(obj -> getConverter().fromJson(obj, full));
    }

    private <T extends AaiRelationable, U extends AaiModel>
        Mono<AaiRelation<T, U>> queryRelation(T from, Class<U> toType, Relationship relationship) {

        final String url = buildUri(relationship.getRelatedLink());
        final JsonObject req = toJsonObject(getConverter(), relationship);

        return requestsBuilder
                .buildGetAction(url)
                .flatMap(AaiHttpActionFactory.this::responseAsJson)
                .map(obj -> mergeObjects(req, obj))
                .map(obj -> getConverter().fromJson(obj, toType))
                .map(obj -> AaiRelation.create(from, obj, toRelationType(relationship)));
    }

    @Override
    protected <T extends AaiRelationable, U extends AaiModel>
        AaiGetRelationAction<T, U> getRelation(Class<U> to) {

        return model -> just(model.getRelationshipList())
                        .map(relList -> findRelatedTo(relList, getAaiType(to), UNKNOWN))
                        .flatMap(Flux::fromIterable)
                        .flatMap(rel -> queryRelation(model, to, rel));
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
                        getQueryUrlMapping(to).apply(relation.to()),
                        getAaiType(to),
                        relation
                ).map(omit -> UNIT);
    }
}
