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

import static io.vavr.Predicates.not;
import static io.vavr.collection.HashMap.ofAll;
import static java.lang.String.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiActionFactoryBase;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AaiHttpAbstractActionFactory extends AaiActionFactoryBase {
    private static final String TYPE_NOT_FOUND = "URI mapping for type %s wasn't found!!";
    private static final String AAI_TYPE_NOT_FOUND = "AAI type for %s wasn't found!!";
    private static final Logger LOGGER = LoggerFactory.getLogger(AaiHttpActionFactory.class);

    private static final String BASIC_AUTH_HEADER = "Authorization";
    private static final String RELATION_URL_PART = "/relationship-list/relationship";

    private final Gson gson;

    private final AaiClientConfiguration configuration;
    private final Supplier<RxHttpClient> createInsecureClient;
    private final Function<SecurityKeys, RxHttpClient> createSecuredClient;

    protected AaiHttpAbstractActionFactory(
            final AaiClientConfiguration configuration,
            final Supplier<RxHttpClient> createInsecureClient,
            final Function<SecurityKeys, RxHttpClient> createSecuredClient) {

        this.configuration = configuration;
        this.createSecuredClient = createSecuredClient;
        this.createInsecureClient = createInsecureClient;
        this.gson = Stream
                .ofAll(typeAdapters)
                .foldLeft(new GsonBuilder(), GsonBuilder::registerTypeAdapterFactory)
                .create();
    }

    protected static RequestDiagnosticContext createRequestDiagnosticContext() {
        return ImmutableRequestDiagnosticContext
                .builder()
                .invocationId(UUID.randomUUID())
                .requestId(UUID.randomUUID())
                .build();
    }

    public RxHttpClient build() {
        LOGGER.debug("Setting ssl context");

        if (configuration.enableAaiCertAuth()) {
            return createSecuredClient.apply(createSslKeys());
        } else {
            return createInsecureClient.get();
        }
    }

    public Gson getConverter() {
        return gson;
    }

    private SecurityKeys createSslKeys() {
        return ImmutableSecurityKeys
                .builder()
                .keyStore(ImmutableSecurityKeysStore.of(Paths.get(configuration.keyStorePath())))
                .keyStorePassword(Passwords.fromPath(Paths.get(configuration.keyStorePasswordPath())))
                .trustStore(ImmutableSecurityKeysStore.of(Paths.get(configuration.trustStorePath())))
                .trustStorePassword(Passwords.fromPath(Paths.get(configuration.trustStorePasswordPath())))
                .build();
    }

    protected String addRelationUrlPart(String url) {
        return Paths
                .get(url, RELATION_URL_PART)
                .toString();
    }

    @SuppressWarnings("unchecked")
    protected <T extends AaiModel> Function<T, String> getUrlMapping(Class<T> type) {
        return Option
                .of(uriMappings.get(type))
                .map(mapper -> (Function<T, String>)mapper)
                .map(mapper -> mapper.andThen(this::buildUri))
                .getOrElseThrow(() -> new RuntimeException(format(TYPE_NOT_FOUND, type)));
    }

    protected <T extends AaiModel> String getUrl(Class<T> type, T model) {
        return getUrlMapping(type).apply(model);
    }

    protected String getAaiType(Class<?> type) {
        return Option
                .of(aaiTypeMappings.get(type))
                .getOrElseThrow(() -> new RuntimeException(format(AAI_TYPE_NOT_FOUND, type)));
    }

    protected String buildUri(final String aaiEndpoint) {
        return Paths.get(configuration.baseUrl(), aaiEndpoint).toString();
    }

    private String toBasicAuth(String user, String password) {
        return "Basic " + Base64
                .getEncoder()
                .encodeToString(format("%s:%s", user, password).getBytes());
    }

    protected Map<String, String> getHeaders() {

        final Option<String> aaiUser = Option.of(configuration.aaiUserName()).filter(not(String::isEmpty));
        final Option<String> aaiPassword = Option.of(configuration.aaiUserName()).filter(not(String::isEmpty));
        final Map<String, String> headers = ofAll(configuration.aaiHeaders());

        return aaiUser
                .flatMap(user -> aaiPassword.map(password -> toBasicAuth(user, password)))
                .map(encoded -> headers.put(BASIC_AUTH_HEADER, encoded))
                .getOrElse(headers);
    }
}