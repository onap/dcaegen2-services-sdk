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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions;

import static io.vavr.collection.List.of;

public class ParseException extends RuntimeException {
    private final ExceptionCode code;

    private ParseException(ExceptionCode code, String... params) {
        super(of(params).foldLeft(code.messagePrototype, (acc, item) -> acc.replaceFirst("%s", item)));

        this.code = code;
    }

    public ExceptionCode getExceptionCode() {
        return code;
    }

    public static ParseException create(ExceptionCode code, String... params) {
        return new ParseException(code, params);
    }
}
