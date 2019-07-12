/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils;

import io.vavr.collection.Stream;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;

public final class General {
    private General() {
    }

    /**
     * @param elements from which array should be created.
     * @return array of type T containing elements.
     */
    @SafeVarargs
    public static <T> T[] array(T... elements) {
        return elements;
    }

    /**
     * @param matcher regex matches. matcher is mutated during Stream evaluation, so it shouldn't be used after
     *                passing it as argument to this function.
     * @return lazy stream of regex matches.
     * @throws NullPointerException if matcher is null.
     */
    public static Stream<MatchResult> asStream(@Nonnull Matcher matcher) {
        return Stream
                .continually(matcher)
                .takeWhile(Matcher::find)
                .map(Matcher::toMatchResult);
    }
}
