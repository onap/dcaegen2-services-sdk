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

package org.onap.dcaegen2.service.sdk.rest.services.aai.processor.validators;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.validators.Validator.validateIfTypeIsNotNested;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.validators.Validator.validateMemberOfTypeAnnotatedWithAaiModel;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.validators.Validator.validateTypeAnnotatedWithAaiModel;

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import org.junit.jupiter.api.Test;

public class ValidatorTest {
    private final TypeElement classElement = mock(TypeElement.class);

    @Test
    public void ifTypeIsNestedRuntimeExceptionShouldBeThrown() {
        final TypeElement parent = mock(TypeElement.class);

        given(parent.getKind()).willReturn(ElementKind.CLASS);
        given(classElement.getEnclosingElement()).willReturn(parent);

        assertThatThrownBy(() -> validateIfTypeIsNotNested(classElement))
                .isInstanceOf(RuntimeException.class);

    }

    @Test
    public void ifTypeIsContainedDirectlyInThePassedMethodShouldSucceed() {
        final TypeElement parent = mock(TypeElement.class);

        given(parent.getKind()).willReturn(ElementKind.PACKAGE);
        given(classElement.getEnclosingElement()).willReturn(parent);

        validateIfTypeIsNotNested(classElement);
    }

    @Test
    public void ifTypeIsNotInterfaceRuntimeExceptionShouldBeThrown() {
        given(classElement.getKind()).willReturn(ElementKind.CLASS);

        assertThatThrownBy(() -> validateTypeAnnotatedWithAaiModel(classElement));
    }

    @Test
    public void ifTypeIsGenericInterfaceRuntimeExceptionShouldBeThrown() {
        final TypeParameterElement param = mock(TypeParameterElement.class);
        final List<TypeParameterElement> params = singletonList(param);

        given(classElement.getKind()).willReturn(ElementKind.INTERFACE);

        willReturn(params)
                .given(classElement)
                .getTypeParameters();

        assertThatThrownBy(() -> validateTypeAnnotatedWithAaiModel(classElement));
    }

    @Test
    public void ifTypeIsInterfaceAndIsNotGenericShouldReturnArgumentCastedToTypeElement() {
        final List<TypeParameterElement> params = emptyList();

        given(classElement.getKind()).willReturn(ElementKind.INTERFACE);

        willReturn(params)
                .given(classElement)
                .getTypeParameters();

        assertThat(validateTypeAnnotatedWithAaiModel(classElement)).isEqualTo(classElement);
    }

    @Test
    public void ifInterfaceContainsOtherElementsThatNonGenericNonArgumentMethodsShouldThrowRuntimeException() {
        final Element child = mock(Element.class);

        // wrong element type
        given(child.getKind()).willReturn(ElementKind.FIELD);

        assertThatThrownBy(() -> validateMemberOfTypeAnnotatedWithAaiModel(classElement, child))
            .isInstanceOf(RuntimeException.class);


        given(child.getKind()).willReturn(ElementKind.INTERFACE);

        assertThatThrownBy(() -> validateMemberOfTypeAnnotatedWithAaiModel(classElement, child))
                .isInstanceOf(RuntimeException.class);



        final ExecutableElement method = mock(ExecutableElement.class);

        // an interface has a default method
        given(method.getKind()).willReturn(ElementKind.METHOD);
        given(method.isDefault()).willReturn(true);

        assertThatThrownBy(() -> validateMemberOfTypeAnnotatedWithAaiModel(classElement, method))
                .isInstanceOf(RuntimeException.class);


        // an interface has a method with arguments
        final VariableElement variable = mock(VariableElement.class);

        given(method.isDefault()).willReturn(false);

        willReturn(singletonList(variable))
                .given(method)
                .getParameters();

        assertThatThrownBy(() -> validateMemberOfTypeAnnotatedWithAaiModel(classElement, method))
                .isInstanceOf(RuntimeException.class);


        // an interface has a generic method
        final TypeParameterElement type = mock(TypeParameterElement.class);

        willReturn(emptyList())
                .given(method)
                .getParameters();

        willReturn(singletonList(type))
                .given(method)
                .getTypeParameters();

        assertThatThrownBy(() -> validateMemberOfTypeAnnotatedWithAaiModel(classElement, method))
                .isInstanceOf(RuntimeException.class);

    }
}
