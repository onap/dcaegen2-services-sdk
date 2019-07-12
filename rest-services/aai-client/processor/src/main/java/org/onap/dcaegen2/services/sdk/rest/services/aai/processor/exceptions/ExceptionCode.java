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

public enum ExceptionCode {

    AAI_OPTIONAL_FILED_ON_PRIMITIVE_TYPE_ERROR("AAI field %s on type %s is optional but has primitive type!!"),
    AAI_FIELD_NOT_FOUND_ON_TYPE_ERROR("AAI field %s wasn't found on type %s"),
    WRONG_ELEMENT_TYPE_ERROR("%s annotation can be used only on interfaces!! %s isn't an interface!!"),
    GENERIC_INTERFACE_ERROR("An interface annotated with %s can't be generic!! %s is generic!!"),
    INCORRECT_MEMBERS_ERROR("An interface annotated with %s can only contain non-default methods!! %s contains %s!!"),
    GENERIC_METHOD_ERROR("An interface annotated with %s can't contain generic methods!! %s contains %s!!"),
    METHOD_ARITY_ERROR("An interface annotated with %s can't contain methods with arguments!! %s contains %s!!"),
    NESTED_TYPE_ERROR("A type annotated with %s must be contained in a package!! %s is nested inside %s!!"),
    DEFAULT_METHOD_ERROR("An interface annotated with %s can't have default methods!! %s has a default method %s!!");

    final String messagePrototype;

    ExceptionCode(String messagePrototype) {
        this.messagePrototype = messagePrototype;
    }
}
