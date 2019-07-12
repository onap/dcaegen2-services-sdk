/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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
import static java.util.Map.Entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.utils.AssertionArgMatcher.assertArg;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.utils.HttpRequestUtils.MatchRequest.matchReq;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.utils.HttpRequestUtils.toFieldSet;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.Unit.UNIT;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.DELETE;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.GET;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PATCH;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PUT;

import com.google.gson.JsonElement;
import java.util.Set;
import org.assertj.core.api.AbstractListAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.service.http.utils.HttpRequestUtils;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiRelation;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiBadArgumentException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiNotFoundException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiPreconditionFailedException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.exceptions.AaiServiceConnectionException;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import reactor.test.StepVerifier;

class AaiHttpActionFactoryTest extends AbstractAaiHttpFactoryTest {
    private static final String RELATION_REGEX = ".*relationship-list/relationship.*";
    private static final String RESOURCE_VERSION_REGEX = ".*resource-version=\\d+.*";

    private static String contains(String text) {
        return format(".*%s.*", text);
    }

    private HttpRequestUtils requestUtil = new HttpRequestUtils(converter);

    AbstractListAssert assertThatLinkContainsUrl(HttpRequest req, String url) {
        return assertThat(requestUtil.toObject(req, Relationship.class))
                .extracting(Relationship::getRelatedLink)
                .hasOnlyOneElementSatisfying(s -> assertThat(s).matches(contains(url)));
    }

    /* ------------------------- AaiActionFactory::getX() tests ----------------------------*/

    @ParameterizedTest
    @MethodSource("provideArgsForGetAction")
    public <T extends AaiModel, U extends T>
        void getRequiredModelShouldReturnCompleteModel(
                T requiredModel,
                U completeModel,
                String resourceUrl,
                AaiGetAction<T, U> getAction) {

        prepareResponseWithObjectAsJson(completeModel);

        StepVerifier
                .create(getAction.call(requiredModel))
                .expectNext(completeModel)
                .verifyComplete();

        then(httpClient)
                .should(times(1))
                .call(matchReq(GET, contains(resourceUrl)));
    }

    @ParameterizedTest
    @MethodSource("provideArgsForGetAction")
    public <T extends AaiModel, U extends T>
        void getRequiredModelShouldThrowNotFoundExceptionOn404(
            T requiredModel,
            U omit,
            String resourceUrl,
            AaiGetAction<T, U> getAction) {

        given(response.statusCode()).willReturn(404);
        given(response.successful()).willReturn(false);

        StepVerifier
                .create(getAction.call(requiredModel))
                .expectError(AaiNotFoundException.class)
                .verify();

        then(httpClient)
                .should(times(1))
                .call(matchReq(GET, contains(resourceUrl)));
    }



    /* ------------------------- AaiActionFactory::createX() tests ----------------------------*/

    @ParameterizedTest
    @MethodSource("provideArgsForCreateAction")
    public <T extends AaiDeletable & AaiRelationable>
        void createModelShouldSendCompleteModelAsJson(
            T completeModel,
            String resourceUrl,
            AaiCreateAction<T> createAction) {

        final Set<Entry<String, JsonElement>> completeAsMap = requestUtil.toFieldSet(completeModel);

        given(response.statusCode()).willReturn(200);
        given(response.successful()).willReturn(true);

        StepVerifier
                .create(createAction.call(completeModel))
                .expectNext(UNIT)
                .verifyComplete();

        then(httpClient)
                .should(times(1))
                .call(matchReq(PUT, contains(resourceUrl)));

        then(httpClient)
                .should(times(1))
                .call(assertArg(req -> assertThat(toFieldSet(req)).isSubsetOf(completeAsMap)));
    }

    @ParameterizedTest
    @MethodSource("provideArgsForCreateAction")
    public <T extends AaiDeletable & AaiRelationable>
        void createModelShouldThrowBadArgumentExceptionOn400(
            T completeModel,
            String resourceUrl,
            AaiCreateAction<T> createAction) {

        given(response.statusCode()).willReturn(400);
        given(response.successful()).willReturn(false);

        StepVerifier
                .create(createAction.call(completeModel))
                .expectError(AaiBadArgumentException.class)
                .verify();

        then(httpClient)
                .should(times(1))
                .call(matchReq(PUT, contains(resourceUrl)));
    }



    /* ------------------------- AaiActionFactory::updateX() tests ----------------------------*/

    @ParameterizedTest
    @MethodSource("provideArgsForUpdateAction")
    public <T extends AaiDeletable & AaiRelationable>
        void updateModelShouldSendCompleteModel(
            T completeModel,
            String resourceUrl,
            AaiUpdateAction<T> updateAction) {

        final Set<Entry<String, JsonElement>> completeAsMap = requestUtil.toFieldSet(completeModel);

        given(response.statusCode()).willReturn(200);
        given(response.successful()).willReturn(true);

        StepVerifier
                .create(updateAction.call(completeModel))
                .expectNext(UNIT)
                .verifyComplete();

        then(httpClient)
                .should(times(1))
                .call(matchReq(PATCH, contains(resourceUrl)));

        then(httpClient)
                .should(times(1))
                .call(assertArg(req -> assertThat(toFieldSet(req)).isSubsetOf(completeAsMap)));
    }

    @ParameterizedTest
    @MethodSource("provideArgsForUpdateAction")
    public <T extends AaiDeletable & AaiRelationable>
        void updateModelShouldThrowPreconditionFailedExceptionOn412(
            T completeModel,
            String resourceUrl,
            AaiUpdateAction<T> updateAction) {

        given(response.statusCode()).willReturn(412);
        given(response.successful()).willReturn(false);

        StepVerifier
                .create(updateAction.call(completeModel))
                .expectError(AaiPreconditionFailedException.class)
                .verify();

        then(httpClient)
                .should(times(1))
                .call(matchReq(PATCH, contains(resourceUrl)));
    }



    /* ------------------------- AaiActionFactory::deleteX() tests ----------------------------*/

    @ParameterizedTest
    @MethodSource("provideArgsForDeleteAction")
    public <T extends AaiDeletable>
        void deleteModelShouldContainResourceVersion(
            T completeModel,
            String resourceUrl,
            AaiDeleteAction<T> deleteAction) {

        given(response.statusCode()).willReturn(200);
        given(response.successful()).willReturn(true);

        StepVerifier
                .create(deleteAction.call(completeModel))
                .expectNext(UNIT)
                .verifyComplete();

        then(httpClient)
                .should(times(1))
                .call(matchReq(DELETE, contains(resourceUrl), RESOURCE_VERSION_REGEX));
    }


    @ParameterizedTest
    @MethodSource("provideArgsForDeleteAction")
    public <T extends AaiDeletable>
        void deleteModelShouldThrowOnOtherHttpErrorCode(
            T completeModel,
            String resourceUrl,
            AaiDeleteAction<T> deleteAction) {

        given(response.statusCode()).willReturn(500);
        given(response.successful()).willReturn(false);

        StepVerifier
                .create(deleteAction.call(completeModel))
                .expectError(AaiServiceConnectionException.class)
                .verify();

        then(httpClient)
                .should(times(1))
                .call(matchReq(DELETE, contains(resourceUrl), RESOURCE_VERSION_REGEX));
    }



    /* ------------------------- AaiActionFactory::getRelationToX() tests ----------------------------*/

    @ParameterizedTest
    @MethodSource("provideArgsForEmptyGetRelationAction")
    public <T extends AaiRelationable, U extends AaiDeletable & AaiRelationable>
        void getRelationToModelThatDoesntHaveRelationShouldReturnEmpty(
                T fromCompleteModel,
                AaiGetRelationAction<T,U> getRelationTo) {

        StepVerifier
                .create(getRelationTo.call(fromCompleteModel))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("provideArgsForGetRelationAction")
    public <T extends AaiRelationable, U extends AaiDeletable & AaiRelationable>
        void getRelationToModelThatHaveRelationShouldReturnCompleteModel(
            T fromCompleteModel,
            U toCompleteModel,
            String toResourceUrl,
            AaiGetRelationAction<T,U> getRelationTo) {

        prepareResponseWithObjectAsJson(toCompleteModel);

        StepVerifier
                .create(getRelationTo.call(fromCompleteModel))
                .assertNext(rel -> assertEquals(toCompleteModel, rel.to()))
                .verifyComplete();


        then(httpClient)
                .should(times(1))
                .call(matchReq(GET, contains(toResourceUrl)));
    }



    /* ------------------------- AaiActionFactory::addRelationFromX() tests ----------------------------*/

    @ParameterizedTest
    @MethodSource("provideArgsForAddRelationFromAction")
    public <T extends AaiModel, U extends AaiModel>
        void addRelationFromShouldSendTransformedToModel(
                T fromRequiredModel,
                U toRequiredModel,
                String fromResourceUrl,
                String toResourceUrl,
                AaiAddRelationAction<T,U> addRelationAction) {

        given(response.statusCode()).willReturn(200);
        given(response.successful()).willReturn(true);

        StepVerifier
                .create(addRelationAction.call(AaiRelation.create(fromRequiredModel, toRequiredModel)))
                .expectNext(UNIT)
                .verifyComplete();

        then(httpClient)
                .should(times(1))
                .call(matchReq(PUT, contains(fromResourceUrl), RELATION_REGEX));

        then(httpClient)
                .should(times(1))
                .call(assertArg(req -> assertThatLinkContainsUrl(req, toResourceUrl)));
    }



    /* ------------------------- AaiActionFactory::deleteRelationFromX() tests ----------------------------*/

    @ParameterizedTest
    @MethodSource("provideArgsForDeleteEmptyRelationFromAction")
    public <T extends AaiDeletable & AaiRelationable, U extends AaiModel>
        void deleteRelationThatDoesntExistShouldReturnEmptyResult(
                T fromCompleteModel,
                U toRequiredModel,
                AaiDeleteRelationAction<T, U> deleteRelationAction) {

        StepVerifier
                .create(deleteRelationAction.call(AaiRelation.create(fromCompleteModel, toRequiredModel)))
                .verifyComplete();

        then(httpClient)
                .should(times(0))
                .call(any());
    }

    @ParameterizedTest
    @MethodSource("provideArgsForDeleteRelationFromAction")
    public <T extends AaiDeletable & AaiRelationable, U extends AaiModel>
        void deleteRelationShouldSendResourceVersion(
                T fromCompleteModel,
                U toRequiredModel,
                String fromResourceUrl,
                String toResourceUrl,
                AaiDeleteRelationAction<T, U> deleteRelationAction) {

        given(response.statusCode()).willReturn(200);
        given(response.successful()).willReturn(true);

        StepVerifier
                .create(deleteRelationAction.call(AaiRelation.create(fromCompleteModel, toRequiredModel)))
                .expectNext(UNIT)
                .verifyComplete();

        then(httpClient)
                .should(times(1))
                .call(matchReq(DELETE, contains(fromResourceUrl), RELATION_REGEX, RESOURCE_VERSION_REGEX));

        then(httpClient)
                .should(times(1))
                .call(assertArg(req -> assertThatLinkContainsUrl(req, toResourceUrl)));
    }
}