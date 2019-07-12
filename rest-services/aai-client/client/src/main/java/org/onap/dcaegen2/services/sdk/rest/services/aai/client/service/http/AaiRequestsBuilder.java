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

import static io.vavr.collection.LinkedHashMap.of;
import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;
import static java.lang.String.format;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser.findRelatedTo;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.DELETE;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.GET;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PATCH;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PUT;

import com.google.gson.Gson;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.net.URI;
import java.nio.file.Paths;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.adapters.AaiExceptions;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationType;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import reactor.core.publisher.Mono;

class AaiRequestsBuilder {
    private static final String RESOURCE_VERSION = "resource-version";
    private static final String URI_CREATION_ERROR = "Couldn't construct proper URI from %s for type %s";

    private static final Map<String, String> JSON_CONTENT = of("Content-Type", "application/json");
    private static final Map<String, String> ACCEPT_JSON = of("Content-Type", "application/json");
    private static final Map<String, String> PATCH_CONTENT = of("Content-Type", "application/merge-patch+json");

    private final Gson converter;
    private final RxHttpClient client;
    private final Map<String, String> headers;

    AaiRequestsBuilder(Gson converter, RxHttpClient client, Map<String, String> headers) {
        this.converter = converter;
        this.client = client;
        this.headers = headers;
    }

    private <T> Mono<HttpResponse> makeRequest(
            String url,
            HttpMethod method,
            Map<String, String> additionalHeaders,
            Option<T> payload) {

        final ImmutableHttpRequest request = ImmutableHttpRequest
                .builder()
                .url(url)
                .customHeaders(headers.merge(additionalHeaders))
                .diagnosticContext(AaiHttpAbstractActionFactory.createRequestDiagnosticContext())
                .method(method)
                .build();

        return client.call(payload
                .map(converter::toJson)
                .map(RequestBody::fromString)
                .map(request::withBody)
                .getOrElse(request)
        ).flatMap(AaiExceptions::mapToExceptionIfUnsuccessful);
    }

    private Mono<HttpResponse> makeRequest(
            String url,
            HttpMethod method,
            Map<String, String> additionalHeaders) {

        return makeRequest(url, method, additionalHeaders, none());
    }

    private static Relationship buildRelationship(Class<?> type, String uri, RelationType relation) {
        final URI parsed = toUri(type, uri);
        final String link = parsed.getPath()
                + Option.of(parsed.getQuery()).map(z -> '?' + z).getOrElse("")
                + Option.of(parsed.getFragment()).map(z -> '#' + z).getOrElse("");

        final ImmutableRelationship.BuildFinal relationship =
                ImmutableRelationship
                        .builder()
                        .relatedLink(link);

        if (relation != RelationType.UNKNOWN) {
            relationship.relationshipLabel(relation.type);
        }

        return relationship.build();
    }

    private static <T extends AaiDeletable> String appendResourceVersion(T model, String uri) {
        final String varPrefix = toUri(model.getClass(), uri).getQuery() == null ? "?" : "&";

        return format("%s%s%s=%s", uri, varPrefix, RESOURCE_VERSION, model.getResourceVersion());
    }

    private static URI toUri(Class<?> type, String toUri) {
        final String normalized = "/" + Paths.get("/", toUri);

        return Try
                .of(() -> new URI(normalized))
                .getOrElseThrow(() -> new IllegalArgumentException(format(URI_CREATION_ERROR, toUri, type)));
    }

    public <T extends AaiModel, U extends T> Mono<HttpResponse> buildGetAction(String url) {

        return makeRequest(url, GET, ACCEPT_JSON);
    }

    public <T extends AaiModel> Mono<HttpResponse> buildCreateAction(String url, T model) {

        return makeRequest(url, PUT, JSON_CONTENT, some(model));
    }

    public <T extends AaiModel> Mono<HttpResponse> buildUpdateAction(String url, T model) {

        return makeRequest(url, PATCH, PATCH_CONTENT, some(model));
    }

    public <T extends AaiDeletable> Mono<HttpResponse> buildDeleteAction(String url, T model) {

        return makeRequest(appendResourceVersion(model, url), DELETE, JSON_CONTENT);
    }

    public <T extends AaiModel, U extends AaiModel> Mono<HttpResponse> buildAddRelation(
            String fromUrl,
            String toUrl,
            AaiRelation<T, U> relation) {

        final Class<?> toType = relation.to().getClass();
        final Relationship relationship = buildRelationship(toType, toUrl, relation.relationType());

        return makeRequest(fromUrl, PUT, JSON_CONTENT, some(relationship));
    }

    public <T extends AaiDeletable & AaiRelationable, U extends AaiModel> Mono<HttpResponse>
        buildDeleteRelation(String fromUrl, String aaiTypeName, AaiRelation<T, U> relation) {

        final T from = relation.from();
        final Option<Relationship> relatedTo = findRelatedTo(from.getRelationshipList(), aaiTypeName);
        final Option<String> maybeLink = relatedTo.map(Relationship::getRelatedLink);

        return maybeLink
                .map(ImmutableRelationship.builder()::relatedLink)
                .map(ImmutableRelationship.BuildFinal::build)
                .map(relationship -> makeRequest(
                        appendResourceVersion(from, fromUrl),
                        DELETE,
                        JSON_CONTENT,
                        some(relationship))
                ).getOrElse(Mono.empty());
    }
}
