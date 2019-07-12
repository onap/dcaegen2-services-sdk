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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor.validators;

import static io.vavr.Predicates.isNotNull;
import static io.vavr.collection.Stream.iterate;
import static java.lang.String.format;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;

public final class Validator {
    private static final String AAI_MODEL = AaiPojo.class.getSimpleName();
    private static final String WRONG_ELEMENT_TYPE_ERROR =
            "%s annotation can be used only on interfaces!! %s isn't an interface!!";
    private static final String GENERIC_INTERFACE_ERROR =
            "An interface annotated with %s can't be generic!! %s is generic!!";
    private static final String INCORRECT_MEMBERS_ERROR =
            "An interface annotated with %s can only contain non-default methods!! %s contains %s!!";
    private static final String GENERIC_METHOD_ERROR =
            "An interface annotated with %s can't contain generic methods!! %s contains %s!!";
    private static final String METHOD_ARITY_ERROR =
            "An interface annotated with %s can't contain methods with arguments!! %s contains %s!!";
    private static final String NESTED_TYPE_ERROR =
            "A type annotated with %s must be contained in a package!! %s is nested inside %s!!";
    private static final String DEFAULT_METHOD_ERROR =
            "An interface annotated with %s can't have default methods!! %s has a default method %s!!";

    private Validator() {}

    private static final void fail(String msg, Name... elements) {
        throw new RuntimeException(format(msg, AAI_MODEL, elements[0], elements[1]));
    }

    /**
     * @param element type that is verified for being contained in the package.
     * @throws RuntimeException is the type is nested.
     */

    public static void validateIfTypeIsntNested(Element element) {
        iterate(element, Element::getEnclosingElement)
                .drop(1)
                .takeWhile(isNotNull())
                .filter(x -> x.getKind() != ElementKind.PACKAGE)
                .forEach(x -> fail(NESTED_TYPE_ERROR, element.getSimpleName(), x.getSimpleName()));
    }

    /**
     * @param element is checked for being non-generic interface.
     * @return element casted to TypeElement
     * @throws RuntimeException is the type doesn't pass the verification.
     */

    public static TypeElement validateTypeAnnotatedWithAaiModel(Element element) {
        if (element.getKind() != ElementKind.INTERFACE) {
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
