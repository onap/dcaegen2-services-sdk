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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor.validators;

import static io.vavr.Predicates.isNotNull;
import static io.vavr.collection.Stream.iterate;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.ENUM;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.DEFAULT_METHOD_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.GENERIC_INTERFACE_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.GENERIC_METHOD_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.INCORRECT_MEMBERS_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.METHOD_ARITY_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.NESTED_TYPE_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.WRONG_ELEMENT_TYPE_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ParseException.create;

import io.vavr.collection.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ParseException;

public final class Validator {
    private static final String AAI_MODEL = AaiPojo.class.getSimpleName();

    private Validator() {}

    private static final void fail(ExceptionCode code, Name... elements) {
        throw create(code, List
                .of(elements)
                .map(Name::toString)
                .prepend(AAI_MODEL)
                .toJavaArray(String[]::new));
    }

    /**
     * @param element type that is verified for being contained in the package.
     * @throws ParseException is the type is nested.
     */

    public static void validateIfTypeIsNotNested(Element element) {
        iterate(element, Element::getEnclosingElement)
                .drop(1)
                .takeWhile(isNotNull())
                .filter(x -> x.getKind() == CLASS || x.getKind() == INTERFACE || x.getKind() == ENUM)
                .forEach(x -> fail(NESTED_TYPE_ERROR, element.getSimpleName(), x.getSimpleName()));
    }

    /**
     * @param element is checked for being non-generic interface.
     * @return element casted to TypeElement
     * @throws RuntimeException is the type doesn't pass the verification.
     */

    public static TypeElement validateTypeAnnotatedWithAaiModel(Element element) {
        if (element.getKind() != INTERFACE) {
            fail(WRONG_ELEMENT_TYPE_ERROR, element.getSimpleName());
        }

        final TypeElement type = (TypeElement) element;

        if (!type.getTypeParameters().isEmpty()) {
            fail(GENERIC_INTERFACE_ERROR, type.getQualifiedName());
        }

        return type;
    }

    /**
     * @param child an element being check for being non-parameter, non-generic and non-default method.
     * @param parent a type containing the method.
     * @return child casted to ExecutableElement
     * @throws RuntimeException is the child doesn't pass the verification.
     */

    public static ExecutableElement validateMemberOfTypeAnnotatedWithAaiModel(TypeElement parent, Element child) {
        if (child.getKind() != ElementKind.METHOD) {
            fail(INCORRECT_MEMBERS_ERROR, parent.getQualifiedName(), child.getSimpleName());
        }

        final ExecutableElement method = (ExecutableElement) child;

        if (method.isDefault()) {
            fail(DEFAULT_METHOD_ERROR, parent.getQualifiedName(), child.getSimpleName());
        }

        if (!method.getParameters().isEmpty()) {
            fail(METHOD_ARITY_ERROR, parent.getQualifiedName(), child.getSimpleName());
        }

        if (!method.getTypeParameters().isEmpty()) {
            fail(GENERIC_METHOD_ERROR, parent.getQualifiedName(), child.getSimpleName());
        }

        return method;
    }
}
