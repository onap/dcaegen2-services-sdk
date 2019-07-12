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

import static io.vavr.collection.List.of;
import static java.util.Map.Entry;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.vavr.collection.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class JsonHelpersTest {

    @Nested
    public static class MergeObjectsTest {
        private static final String FIRST_PROPERTY_NAME = "SomeProperty1";
        private static final String SECOND_PROPERTY_NAME = "SomeProperty2";
        private static final JsonElement FIRST_PROPERTY_VALUE = new JsonPrimitive("SomeValue1");
        private static final JsonElement SECOND_PROPERTY_VALUE = new JsonPrimitive("SomeValue2");

        private static final Map<String, JsonElement> KEYS_VALUES = HashMap.of(
                FIRST_PROPERTY_NAME, FIRST_PROPERTY_VALUE,
                SECOND_PROPERTY_NAME, SECOND_PROPERTY_VALUE
        ).toJavaMap();

        private final JsonObject first = new JsonObject();
        private final JsonObject second = new JsonObject();

        @Test
        public void objectMergedWithEmptyObjectShouldReturnSelf() {
            //given
            second.add(SECOND_PROPERTY_NAME, SECOND_PROPERTY_VALUE);

            //when
            final JsonObject leftEmpty = JsonHelpers.mergeObjects(first, second);
            final JsonObject rightEmpty = JsonHelpers.mergeObjects(second, first);

            //then
            assertThat(leftEmpty).isEqualTo(second);
            assertThat(rightEmpty).isEqualTo(second);
        }

        @Test
        public void mergedObjectShouldContainFieldsFromBothObjects() {
            //given
            first.add(FIRST_PROPERTY_NAME, FIRST_PROPERTY_VALUE);
            second.add(SECOND_PROPERTY_NAME, SECOND_PROPERTY_VALUE);

            //when
            final JsonObject firstSecond = JsonHelpers.mergeObjects(first, second);
            final JsonObject secondFirst = JsonHelpers.mergeObjects(second, first);

            //then
            assertThat(firstSecond.entrySet()).containsExactlyInAnyOrderElementsOf(KEYS_VALUES.entrySet());
            assertThat(secondFirst.entrySet()).containsExactlyInAnyOrderElementsOf(KEYS_VALUES.entrySet());
        }

        @Test
        public void ifFieldsCollideSecondArgumentValueShouldBeUsed() {
            //given
            first.add(FIRST_PROPERTY_NAME, FIRST_PROPERTY_VALUE);
            second.add(FIRST_PROPERTY_NAME, SECOND_PROPERTY_VALUE);

            //when
            final JsonObject firstSecond = JsonHelpers.mergeObjects(first, second);
            final JsonObject secondFirst = JsonHelpers.mergeObjects(second, first);

            //then
            assertThat(firstSecond.get(FIRST_PROPERTY_NAME))
                    .isNotNull()
                    .isEqualTo(SECOND_PROPERTY_VALUE);

            assertThat(secondFirst.get(FIRST_PROPERTY_NAME))
                    .isNotNull()
                    .isEqualTo(FIRST_PROPERTY_VALUE);
        }
    }

    @Nested
    public static class ToJsonObjectTest {
        private static final Gson CONVERTER = new Gson();

        @Test
        public void nullsAndArraysAreNotConvertibleToJsonObjects() {
            //given
            final List<String> stringList = of("A", "B").toJavaList();
            final String[] stringArray = {"A", "B"};

            //then
            assertThat(JsonHelpers.toJsonObject(CONVERTER, null)).isEmpty();
            assertThat(JsonHelpers.toJsonObject(CONVERTER, stringList)).isEmpty();
            assertThat(JsonHelpers.toJsonObject(CONVERTER, stringArray)).isEmpty();
        }


        private static final String FIRST_FIELD_NAME = "field1";
        private static final String SECOND_FIELD_NAME = "field2";
        private static final JsonPrimitive FIRST_FIELD_VALUE = new JsonPrimitive("A");
        private static final JsonPrimitive SECOND_FIELD_VALUE = new JsonPrimitive(1);
        private static final Map<String, JsonElement> OBJECT_FIELDS = HashMap.<String, JsonElement>of(
                FIRST_FIELD_NAME, FIRST_FIELD_VALUE,
                SECOND_FIELD_NAME, SECOND_FIELD_VALUE
        ).toJavaMap();

        private class SomeParent {
            public final String field1 = FIRST_FIELD_VALUE.getAsString();
        }

        private class SomeChild extends SomeParent {
            public final int field2 = SECOND_FIELD_VALUE.getAsInt();
        }

        @Test
        public void objectsAndMapsAreConvertibleToJsonObjects() {
            //given
            final SomeChild object = new SomeChild();
            final HashMap<String, String> mapOfString = HashMap.of("A", "B", "C", "D");

            Set<Entry<String, JsonElement>> fieldsFromMap = mapOfString
                    .<JsonElement>mapValues(JsonPrimitive::new)
                    .toJavaMap()
                    .entrySet();


            //then
            assertThat(JsonHelpers.toJsonObject(CONVERTER, object))
                    .flatExtracting(JsonObject::entrySet)
                    .hasSameElementsAs(OBJECT_FIELDS.entrySet());

            assertThat(JsonHelpers.toJsonObject(CONVERTER, mapOfString.toJavaMap()))
                    .flatExtracting(JsonObject::entrySet)
                    .hasSameElementsAs(fieldsFromMap);
        }

        @Test
        public void whenSuperClassConverterUsedShouldNotHaveFieldsFromSubClass() {
            //given
            final SomeChild object = new SomeChild();

            //then
            assertThat(JsonHelpers.toJsonObject(CONVERTER, object, SomeParent.class))
                    .flatExtracting(JsonObject::entrySet)
                    .isSubsetOf(OBJECT_FIELDS.entrySet())
                    .hasSize(1);
        }

    }
}