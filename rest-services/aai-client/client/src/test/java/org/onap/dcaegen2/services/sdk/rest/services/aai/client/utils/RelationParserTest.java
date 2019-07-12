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

package org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser.findRelatedTo;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.client.utils.RelationParser.toJsonObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.HashMap;
import io.vavr.control.Option;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.ImmutableRelationshipList;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.Relationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.RelationshipList;

public class RelationParserTest {
    private static final Gson CONVERTER = new Gson();
    private static final String LINK = "SomeLink";
    private static final String OTHER_TYPE = "OtherType";
    private static final String RELATED_TYPE = "SomeType";
    private static final Relationship RELATIONSHIP =
            ImmutableRelationship
                    .builder()
                    .relatedLink(LINK)
                    .relatedTo(RELATED_TYPE)
                    .build();

    private static final RelationshipList SINGLE_RELATIONSHIP =
            ImmutableRelationshipList
                    .builder()
                    .addRelationship(RELATIONSHIP)
                    .build();

    private static final RelationshipList MULTIPLE_RELATIONSHIP =
            ImmutableRelationshipList
                    .copyOf(SINGLE_RELATIONSHIP)
                    .withRelationship(
                            RELATIONSHIP,
                            ImmutableRelationship
                                    .builder()
                                    .relatedLink(LINK)
                                    .relatedTo(OTHER_TYPE)
                                    .build());

    @Nested
    public static class FindRelatedToTest {

        @Test
        public void findRelationOnEmptyRelationListShouldReturnEmptyOptional() {

            //given
            final RelationshipList emptyRelations =
                    ImmutableRelationshipList
                            .builder()
                            .build();

            //when
            final Option<Relationship> maybeRel1 = findRelatedTo(emptyRelations, RELATED_TYPE);
            final Option<Relationship> maybeRel2 = findRelatedTo(emptyRelations, OTHER_TYPE);

            //then
            assertThat(maybeRel1).isEmpty();
            assertThat(maybeRel2).isEmpty();
        }

        @Test
        public void findRelationWithNonContainedTypeShouldReturnEmptyOptional() {

            //when
            final Option<Relationship> maybeRel = findRelatedTo(SINGLE_RELATIONSHIP, OTHER_TYPE);

            //then
            assertThat(maybeRel).isEmpty();
        }

        @Test
        public void findRelationWithContainedTypeShouldReturnDefinedOptional() {

            //when
            final Option<Relationship> maybeRel1 = findRelatedTo(SINGLE_RELATIONSHIP, RELATED_TYPE);
            final Option<Relationship> maybeRel2 = findRelatedTo(MULTIPLE_RELATIONSHIP, RELATED_TYPE);
            final Option<Relationship> maybeRel3 = findRelatedTo(MULTIPLE_RELATIONSHIP, OTHER_TYPE);

            //then
            assertThat(maybeRel1).isNotEmpty();
            assertThat(maybeRel2).isNotEmpty();
            assertThat(maybeRel3).isNotEmpty();
        }
    }

    @Nested
    public static class ToJsonObjectTest {
        private static final String KEY_1 = "SomeKey1";
        private static final String KEY_2 = "SomeKey2";

        private static final String VALUE_1 = "SomeValue1";
        private static final String VALUE_2 = "SomeValue2";

        private static final RelationshipData DATA_1 =
                ImmutableRelationshipData
                        .builder()
                        .relationshipKey(KEY_1)
                        .relationshipValue(VALUE_1)
                        .build();

        private static final RelationshipData DATA_2 =
                ImmutableRelationshipData
                        .builder()
                        .relationshipKey(KEY_2)
                        .relationshipValue(VALUE_2)
                        .build();

        private static final Relationship RELATIONSHIP_WITH_DATA =
                ImmutableRelationship
                        .copyOf(RELATIONSHIP)
                        .withRelationshipData(DATA_1, DATA_2);

        private static final Map<String, JsonElement> FIELDS_MAP = HashMap
                .of(KEY_1, VALUE_1, KEY_2, VALUE_2)
                .<JsonElement>mapValues(JsonPrimitive::new)
                .toJavaMap();

        @Test
        public void emptyRelationshipDataShouldReturnEmptyJsonObject() {

            //when
            final JsonObject result = toJsonObject(CONVERTER, RELATIONSHIP);

            //then
            assertThat(result).isEqualTo(new JsonObject());
        }

        @Test
        public void ifRelationshipKeyDontContainDotThenTheyShouldBePreserved() {

            //when
            final JsonObject result = toJsonObject(CONVERTER, RELATIONSHIP_WITH_DATA);

            //then
            assertThat(result.entrySet()).containsExactlyInAnyOrderElementsOf(FIELDS_MAP.entrySet());
        }

        @Test
        public void ifRelationshipContainDotOnlyPartAfterTheLastDotShouldBePreserved() {

            //given
            final Relationship rel = ImmutableRelationship
                    .copyOf(RELATIONSHIP)
                    .withRelationshipData(
                            ImmutableRelationshipData
                                    .copyOf(DATA_1)
                                    .withRelationshipKey("Foo." + KEY_1),
                            ImmutableRelationshipData
                                    .copyOf(DATA_2)
                                    .withRelationshipKey("Bar.Baz." + KEY_2));

            //when
            final JsonObject result = toJsonObject(CONVERTER, rel);

            //then
            assertThat(result.entrySet()).containsExactlyInAnyOrderElementsOf(FIELDS_MAP.entrySet());
        }
    }
}
