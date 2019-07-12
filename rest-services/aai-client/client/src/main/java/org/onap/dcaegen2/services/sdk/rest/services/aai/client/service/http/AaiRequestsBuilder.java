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

import static java.lang.String.format;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RequestBody.fromString;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.net.URI;
import java.nio.file.Paths;
import java.util.function.Function;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.Unit;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.adapters.AaiExceptions;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.utils.RelationParser;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationType;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import reactor.core.publisher.Mono;

public final class AaiRequestsBuilder {
    private final static String RESOURCE_VERSION = "resource-version";
    private final static String URI_CREATION_ERROR = "Couldn't construct proper URI from %s for type %s";

    private final static Map<String, String> JSON_CONTENT = HashMap.of("Content-Type", "application/json");
    private final static Map<String, String> ACCEPT_JSON = HashMap.of("Content-Type", "application/json");
    private final static Map<String, String> PATCH_CONTENT = HashMap.of("Content-Type", "application/merge-patch+json");

    private AaiRequestsBuilder() {
    }

    private static ImmutableHttpRequest.Builder prepareRequest(
            final String url,
            final Map<String, String> headers,
            final HttpMethod method) {
        return ImmutableHttpRequest
                .builder()
                .url(url)
                .customHeaders(headers)
                .diagnosticContext(AaiHttpAbstractActionFactory.createRequestDiagnosticContext())
                .method(method);
    }

    public static <T extends AaiModel, U extends T> AaiGetAction<T, U> buildGetAction(
            final Gson gson,
            final Class<U> out,
            final Map<String, String> headers,
            final RxHttpClient httpClient,
            final Function<? super T, String> mapToUrl) {

        final JsonParser parser = new JsonParser();

        return aaiModel ->
                httpClient.call(
                        prepareRequest(
                                mapToUrl.apply(aaiModel),
                                headers.merge(ACCEPT_JSON),
                                HttpMethod.GET
                        ).build()
                ).flatMap(AaiExceptions::mapToExceptionIfUnsuccessful)
                        .map(HttpResponse::bodyAsString)
                        .map(parser::parse)
                        .filter(JsonElement::isJsonObject)
                        .map(JsonElement::getAsJsonObject)
                        .map(JsonObject::entrySet)
                        .map(x -> x
                                .stream()
                                .reduce(
                                        gson.toJsonTree(aaiModel).getAsJsonObject(),
                                        (acc, item) -> {
                                            acc.add(item.getKey(), item.getValue());
                                            return acc;
                                        }, (l, r) -> l)
                        ).map(x -> gson.fromJson(x, out));
    }

    public static <T extends AaiModel> AaiCreateAction<T> buildCreateAction(
            final Gson gson,
            final Map<String, String> headers,
            final RxHttpClient httpClient,
            final Function<? super T, String> mapToUrl) {

        return aaiModel -> httpClient.call(
                prepareRequest(
                        mapToUrl.apply(aaiModel),
                        headers.merge(JSON_CONTENT),
                        HttpMethod.PUT
                ).body(fromString(gson.toJson(aaiModel)))
                        .build()
        ).flatMap(AaiExceptions::mapToExceptionIfUnsuccessful)
                .map(omit -> Unit.UNIT);
    }

    public static <T extends AaiModel> AaiUpdateAction<T> buildUpdateAction(
            final Gson gson,
            final Map<String, String> headers,
            final RxHttpClient httpClient,
            final Function<? super T, String> mapToUrl) {

        return aaiModel ->
                httpClient.call(
                        prepareRequest(
                                mapToUrl.apply(aaiModel),
                                headers.merge(PATCH_CONTENT),
                                HttpMethod.PATCH
                        ).body(fromString(gson.toJson(aaiModel)))
                                .build()
                ).flatMap(AaiExceptions::mapToExceptionIfUnsuccessful)
                        .map(omit -> Unit.UNIT);
    }

    public static <T extends AaiDeletable> AaiDeleteAction<T> buildDeleteAction(
            final Map<String, String> headers,
            final RxHttpClient httpClient,
            final Function<? super T, String> mapToUrl) {

        return aaiModel ->
                httpClient.call(
                        prepareRequest(
                                appendResourceVersion(aaiModel, mapToUrl.apply(aaiModel)),
                                headers.merge(JSON_CONTENT),
                                HttpMethod.DELETE
                        ).build()
                ).flatMap(AaiExceptions::mapToExceptionIfUnsuccessful)
                        .map(omit -> Unit.UNIT);
    }

    private static URI toURI(Class<?> type, String toUri) {
        final String normalized = "/" + Paths.get("/", toUri);

        return Try
                .of(() -> new URI(normalized))
                .getOrElseThrow(() -> new IllegalArgumentException(format(URI_CREATION_ERROR, toUri, type)));
    }

    private static Relationship buildRelationship(Class<?> type, String uri, RelationType relation) {
        final URI parsed = toURI(type, uri);
        final String link = parsed.getPath()
                + Option.of(parsed.getQuery()).map(z -> '?' + z).getOrElse("")
                + Option.of(parsed.getFragment()).map(z -> '#' + z).getOrElse("");

        final ImmutableRelationship.BuildFinal relationship =
                ImmutableRelationship
                        .builder()
                        .relatedLink(link);

        if(relation != RelationType.UNKNOWN) {
            relationship.relationshipLabel(relation.type);
        }

        return relationship.build();
    }

    private static <T extends AaiDeletable> String appendResourceVersion(T model, String uri) {
        final String varPrefix = toURI(model.getClass(), uri).getQuery() == null ? "?" : "&";

        return format("%s%s%s=%s", uri, varPrefix, RESOURCE_VERSION, model.getResourceVersion());
    }

    public static <T extends AaiModel, U extends AaiModel> AaiAddRelationAction<T, U> buildAddRelation(
            final Gson gson,
            final RxHttpClient httpClient,
            final Map<String, String> headers,
            final Function<? super T, String> mapFromUrl,
            final Function<? super U, String> mapToUrl) {
        return x -> {
            final String fromUrl = mapFromUrl.apply(x.from());
            final Relationship relationship = buildRelationship(
                    x.to().getClass(),
                    mapToUrl.apply(x.to()),
                    x.relationType());

            return httpClient.call(
                    prepareRequest(
                            fromUrl,
                            headers.merge(JSON_CONTENT),
                            HttpMethod.PUT
                    ).body(fromString(gson.toJson(relationship)))
                     .build()
            ).flatMap(AaiExceptions::mapToExceptionIfUnsuccessful)
             .map(omit -> Unit.UNIT);
        };
    }

    public static <T extends AaiDeletable & AaiRelationable, U extends AaiModel> AaiDeleteRelationAction<T, U>
    buildDeleteRelation(
            final Gson gson,
            final RxHttpClient httpClient,
            final Map<String, String> headers,
            final Function<? super T, String> mapFromUrl,
            final String aaiTypeName) {
        return x -> {
            final String fromUrl = mapFromUrl.apply(x.from());
            final String varPrefix = toURI(x.getClass(), fromUrl).getQuery() == null ? "?" : "&";
            final Option<String> link =
                    RelationParser.findRelatedTo(
                            x.from().getRelationshipList(),
                            aaiTypeName
                    ).map(Relationship::getRelatedLink);

            if (link.isEmpty()) {
                return Mono.empty();
            }

            final Relationship relationship = ImmutableRelationship
                    .builder()
                    .relatedLink(link.get())
                    .build();

            return httpClient.call(
                    prepareRequest(
                            appendResourceVersion(x.from(), fromUrl),
                            headers.merge(JSON_CONTENT),
                            HttpMethod.DELETE
                    ).body(fromString(gson.toJson(relationship)))
                            .build()
            ).flatMap(AaiExceptions::mapToExceptionIfUnsuccessful)
                    .map(omit -> Unit.UNIT);
        };
    }
}
