/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * =========================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================
 */

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.streams;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParserError;
import org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.exceptions.StreamParsingException;
import org.onap.dcaegen2.services.sdk.model.streams.DataStream;
import org.onap.dcaegen2.services.sdk.model.streams.RawDataStream;

/**
 * A generic data stream parser which parses {@code T} to data stream {@code S}.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @param <T> input data type, eg. Gson Object
 * @param <S> output data type
 * @since 1.1.4
 */
public interface StreamParser<T, S extends DataStream> {

    /**
     * Parse the input data {@code T} producing the {@link DataStream}.
     *
     * @param input - the input data
     * @return Right(parsing result) or Left(parsing error)
     */
    default Either<StreamParserError, S> parse(RawDataStream<T> input) {
        return Try.of(() -> unsafeParse(input))
                .toEither()
                .mapLeft(StreamParserError::fromThrowable);
    }

    /**
     * Parse the input data {@code T} producing the {@link DataStream}. Will throw StreamParsingException when input
     * was invalid.
     *
     * @param input - the input data
     * @return parsing result
     * @throws StreamParsingException when parsing was unsuccessful
     */
    default S unsafeParse(RawDataStream<T> input) {
        return parse(input).getOrElseThrow(StreamParsingException::new);
    }
}
