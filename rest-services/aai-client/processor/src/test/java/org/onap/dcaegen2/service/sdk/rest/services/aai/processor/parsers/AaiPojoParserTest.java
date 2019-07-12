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

package org.onap.dcaegen2.service.sdk.rest.services.aai.processor.parsers;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.parsers.AaiPojoParser.parseInterface;

import com.squareup.javapoet.TypeName;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Option;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiOptional;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ClassMeta;

public class AaiPojoParserTest {
    private static final String PACKAGE_NAME = "some.package";
    private static final String AAI_TYPE = "some-type";
    private static final String AAI_PATH = "some/path";
    private static final String CLASS_NAME = "SomeClass";
    private static final String METHOD_NAME1 = "SomeMethod1";
    private static final String METHOD_NAME2 = "SomeMethod2";
    private static final String AAI_FIELD_NAME1 = "SomeAaiField1";
    private static final String AAI_FIELD_NAME2 = "SomeAaiField2";

    private final AaiPojo aaiClassAnnotation = mock(AaiPojo.class);
    private final AaiRequired aaiRequired = mock(AaiRequired.class);
    private final AaiOptional aaiOptional = mock(AaiOptional.class);

    private final Types typesUtils = mock(Types.class);
    private final Elements elementUtils = mock(Elements.class);
    private final TypeMirror mirror = mock(TypeMirror.class);
    private final TypeElement parent = mock(TypeElement.class);
    private final TypeElement ifElement = mock(TypeElement.class);
    private final Name packageName = mock(Name.class);
    private final Name className = mock(Name.class);
    private final PackageElement packageElement = mock(PackageElement.class);
    private final TypeParameterElement typeParameter = mock(TypeParameterElement.class);



    @ParameterizedTest
    @EnumSource(value = ElementKind.class, names = {"INTERFACE"}, mode = EnumSource.Mode.EXCLUDE)
    public void ifElementIsNotAnInterfaceThenShouldThrowRuntimeException(ElementKind kind) {

        given(ifElement.getKind()).willReturn(kind);

        assertThatThrownBy(() -> parseInterface(ifElement, typesUtils, elementUtils))
            .isInstanceOf(RuntimeException.class);
    }


    private ExecutableElement prepareMethod(String name) {
        final ExecutableElement method = mock(ExecutableElement.class);
        final Name methodName = mock(Name.class);
        final TypeMirror returnType = mock(TypeMirror.class);

        given(methodName.toString()).willReturn(name);

        given(returnType.accept(any(), any())).willReturn(TypeName.INT);

        given(method.isDefault()).willReturn(false);
        given(method.getKind()).willReturn(ElementKind.METHOD);
        given(method.getSimpleName()).willReturn(methodName);
        given(method.getReturnType()).willReturn(returnType);

        willReturn(emptySet())
                .given(method)
                .getModifiers();

        willReturn(emptyList())
                .given(method)
                .getThrownTypes();

        willReturn(emptyList())
                .given(method)
                .getParameters();

        willReturn(emptyList())
                .given(method)
                .getTypeParameters();

        return method;
    }

    private void prepareEmptyInterface() {
        given(packageName.toString()).willReturn(PACKAGE_NAME);
        given(packageElement.getQualifiedName()).willReturn(packageName);
        given(elementUtils.getPackageOf(ifElement)).willReturn(packageElement);

        given(ifElement.getKind()).willReturn(ElementKind.INTERFACE);

        given(className.toString()).willReturn(CLASS_NAME);
        given(ifElement.getSimpleName()).willReturn(className);

        given(aaiClassAnnotation.path()).willReturn(AAI_PATH);
        given(aaiClassAnnotation.type()).willReturn(AAI_TYPE);
        given(ifElement.getAnnotation(AaiPojo.class)).willReturn(aaiClassAnnotation);

        willReturn(emptyList())
                .given(ifElement)
                .getInterfaces();

        willReturn(emptyList())
                .given(ifElement)
                .getEnclosedElements();
    }

    @Test
    public void ifInterfaceIsExtendedByGenericInterfaceThenShouldThrowRuntimeException() {

        prepareEmptyInterface();

        willReturn(singletonList(mirror))
                .given(ifElement)
                .getInterfaces();

        willReturn(singletonList(typeParameter))
                .given(parent)
                .getTypeParameters();

        given(typesUtils.asElement(mirror)).willReturn(parent);

        assertThatThrownBy(() -> parseInterface(ifElement, typesUtils, elementUtils))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void ifInterfaceDoesNotContainsMethodMetaShouldContainClassInfo() {

        prepareEmptyInterface();

        assertThat(parseInterface(ifElement, typesUtils, elementUtils))
                .matches(meta -> CLASS_NAME.equals(meta.className()))
                .matches(meta -> PACKAGE_NAME.equals(meta.packageName()))
                .matches(meta -> AAI_PATH.equals(meta.aaiPath()))
                .matches(meta -> AAI_TYPE.equals(meta.aaiType()))
                .matches(meta -> meta.otherAnnotations().isEmpty())
                .matches(meta -> meta.getMethodMeta().isEmpty());
    }

    @Test
    public void metaShouldContainNotAaiAnnotatedMethods() {
        final ExecutableElement method = prepareMethod(METHOD_NAME1);

        prepareEmptyInterface();

        willReturn(singletonList(method))
                .given(ifElement)
                .getEnclosedElements();

        final ClassMeta classMeta = parseInterface(ifElement, typesUtils, elementUtils);

        assertThat(classMeta.getMethodMeta())
                .hasSize(1)
                .extracting(Tuple2::_2)
                .hasOnlyOneElementSatisfying(meta -> meta.aaiName().isEmpty())
                .hasOnlyOneElementSatisfying(meta -> METHOD_NAME1.equals(meta.methodName()));
    }

    @Test
    public void metaShouldContainAaiAnnotatedMethods() {
        final ExecutableElement method1 = prepareMethod(METHOD_NAME1);
        final ExecutableElement method2 = prepareMethod(METHOD_NAME2);

        prepareEmptyInterface();

        given(aaiRequired.value()).willReturn(AAI_FIELD_NAME1);
        given(method1.getAnnotation(AaiRequired.class)).willReturn(aaiRequired);

        given(aaiOptional.value()).willReturn(AAI_FIELD_NAME2);
        given(method2.getAnnotation(AaiOptional.class)).willReturn(aaiOptional);

        willReturn(List.of(method1, method2).toJavaList())
                .given(ifElement)
                .getEnclosedElements();

        final ClassMeta classMeta = parseInterface(ifElement, typesUtils, elementUtils);

        assertThat(classMeta.getMethodMeta())
                .extracting(Tuple2::_2)
                .hasSize(2)
                .anySatisfy(meta -> meta.isRequired())
                .anySatisfy(meta -> meta.aaiName().equals(Option.of(AAI_FIELD_NAME1)))
                .anySatisfy(meta -> meta.aaiName().equals(Option.of(AAI_FIELD_NAME2)))
                .anySatisfy(meta -> METHOD_NAME1.equals(meta.methodName()))
                .anySatisfy(meta -> METHOD_NAME2.equals(meta.methodName()));
    }

    @Test
    public void metaShouldContainParentMethods() {
        final ExecutableElement method1 = prepareMethod(METHOD_NAME1);
        final ExecutableElement method2 = prepareMethod(METHOD_NAME2);

        prepareEmptyInterface();

        given(aaiRequired.value()).willReturn(AAI_FIELD_NAME1);
        given(method1.getAnnotation(AaiRequired.class)).willReturn(aaiRequired);

        given(aaiOptional.value()).willReturn(AAI_FIELD_NAME2);
        given(method2.getAnnotation(AaiOptional.class)).willReturn(aaiOptional);

        willReturn(singletonList(method1))
                .given(ifElement)
                .getEnclosedElements();

        given(typesUtils.asElement(mirror))
                .willReturn(parent);

        willReturn(singletonList(mirror))
                .given(ifElement)
                .getInterfaces();

        given(parent.getKind()).willReturn(ElementKind.INTERFACE);
        willReturn(singletonList(method2))
                .given(parent)
                .getEnclosedElements();

        final ClassMeta classMeta = parseInterface(ifElement, typesUtils, elementUtils);

        assertThat(classMeta.getMethodMeta())
                .extracting(Tuple2::_2)
                .hasSize(2)
                .anySatisfy(meta -> meta.isRequired())
                .anySatisfy(meta -> meta.aaiName().equals(Option.of(AAI_FIELD_NAME1)))
                .anySatisfy(meta -> meta.aaiName().equals(Option.of(AAI_FIELD_NAME2)))
                .anySatisfy(meta -> METHOD_NAME1.equals(meta.methodName()))
                .anySatisfy(meta -> METHOD_NAME2.equals(meta.methodName()));
    }
}
