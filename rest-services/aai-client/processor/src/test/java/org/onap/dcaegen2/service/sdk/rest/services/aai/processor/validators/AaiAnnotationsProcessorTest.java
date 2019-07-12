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

import static io.vavr.collection.Stream.continually;
import static io.vavr.collection.Stream.of;
import static io.vavr.collection.Stream.ofAll;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.AAI_FIELD_NOT_FOUND_ON_TYPE_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.AAI_OPTIONAL_FILED_ON_PRIMITIVE_TYPE_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.GENERIC_INTERFACE_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.GENERIC_METHOD_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.INCORRECT_MEMBERS_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.METHOD_ARITY_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.NESTED_TYPE_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.WRONG_ELEMENT_TYPE_ERROR;

import io.vavr.collection.HashMap;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiOptional;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ParseException;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AaiAnnotationsProcessorTest extends AaiAnnotationProcessorTestBase {

    private static Stream<Arguments> getCompilationFailingSources() {
        return HashMap.of(
                "aaiOptionalFieldOnPrimitiveTypeError", AAI_OPTIONAL_FILED_ON_PRIMITIVE_TYPE_ERROR,
                "aaiFieldNotFoundOnTypeError", AAI_FIELD_NOT_FOUND_ON_TYPE_ERROR,
                "genericMethodError", GENERIC_METHOD_ERROR,
                "genericInterfaceError", GENERIC_INTERFACE_ERROR,
                "nestedTypeError", NESTED_TYPE_ERROR,
                "wrongElementTypeError", WRONG_ELEMENT_TYPE_ERROR,
                "incorrectMembersError", INCORRECT_MEMBERS_ERROR,
                "methodArityError", METHOD_ARITY_ERROR
        ).mapKeys(BASE_PATH::resolve)
                .mapKeys(Path::toFile)
                .mapKeys(File::listFiles)
                .flatMap((key, value) -> of(key).zip(continually(value)))
                .toStream()
                .map(pair -> Arguments.of(pair._1, pair._2));
    }


    @ParameterizedTest(name = "Compilation of {0} should throw ParseException with {1} error")
    @MethodSource("getCompilationFailingSources")
    void compilationShouldFail(File source, ExceptionCode expectedError) throws IOException {

        //when
        final Throwable compileException = assertThrows(RuntimeException.class, () -> compileWithCut(source));

        //then
        assertThat(compileException.getCause())
                .isNotNull()
                .isInstanceOfSatisfying(
                    ParseException.class,
                    e -> assertThat(e.getExceptionCode()).isEqualTo(expectedError));

    }

    private Stream<Arguments> getCompilationPassingSources() {
        return Stream
                .of("compilingSources")
                .map(BASE_PATH::resolve)
                .map(Path::toFile)
                .map(File::listFiles)
                .toMap(files -> Try.of(()->compileWithCut(files)).get(), Stream::of)
                .mapKeys(files -> of(files.sourceFiles(), files.generatedFiles()).flatMap(identity()))
                .mapKeys(files -> createAst(files.toJavaArray(JavaFileObject[]::new)))
                .mapValues(files -> files.map(AaiAnnotationProcessorTestBase::getNameWithoutExtension))
                .flatMap(pair -> pair._2.zip(continually(pair._1)))
                .map(pair -> Arguments.of(pair._1, pair._2.get()))
                .toStream();
    }


    @ParameterizedTest
    @MethodSource("getCompilationPassingSources")
    void requiredAndCompleteInterfacesShouldBeCreatedFromBaseInterface(String className, CtModel model) {

        final Try<CtType<?>> requiredInterface = Try.of(() -> getRequiredIfAst(model, className));
        final Try<CtType<?>> completeInterface = Try.of(() -> getCompleteIfAst(model, className));

        assertThat(requiredInterface).isNotEmpty();
        assertThat(completeInterface).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("getCompilationPassingSources")
    void generatedInterfacesShouldPreserveOtherAnnotationThatAaiPojo(String className, CtModel model) {

        final CtType<?> baseInterface = getBaseIfAst(model, className);
        final CtType<?> requiredInterface = getRequiredIfAst(model, className);
        final CtType<?> completeInterface = getCompleteIfAst(model, className);

        final Stream<CtAnnotation<? extends Annotation>> parentAnnotations =
                ofAll(baseInterface.getAnnotations()).filter(ant -> !isOfType(ant, AaiPojo.class));

        assertThat(requiredInterface.getAnnotations()).containsAll(parentAnnotations);
        assertThat(completeInterface.getAnnotations()).containsAll(parentAnnotations);
    }

    @ParameterizedTest
    @MethodSource("getCompilationPassingSources")
    void generatedInterfacesShouldPreserveNoAaiAnnotationsOnMethods(String className, CtModel model) {

        final CtType<?> baseInterface = getBaseIfAst(model, className);

        for (final CtType<?> type : of(getRequiredIfAst(model, className), getCompleteIfAst(model, className))) {

            for (final CtMethod<?> method : type.getMethods()) {

                final Option<? extends CtMethod<?>> sameSignature = findMethodWithSameSignature(baseInterface, method);

                assertThat(sameSignature).isNotEmpty();
                assertThat(method.getAnnotations())
                        .containsAll(
                                removeAnnotation(
                                        sameSignature.get(),
                                        AaiRequired.class,
                                        AaiOptional.class).getAnnotations());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getCompilationPassingSources")
    void requiredInterfaceShouldContainRequiredFieldsFromBaseInterface(String className, CtModel model) {

        final CtType<?> baseInterface = getBaseIfAst(model, className);
        final CtType<?> requiredInterface = getRequiredIfAst(model, className);

        assertThat(getMethods(requiredInterface).map(AaiAnnotationProcessorTestBase::removeAnnotations))
                .containsExactlyInAnyOrderElementsOf(
                        getAllMethods(baseInterface)
                                .filter(x -> x.hasAnnotation(AaiRequired.class))
                                .map(AaiAnnotationProcessorTestBase::removeAnnotations));
    }

    @ParameterizedTest
    @MethodSource("getCompilationPassingSources")
    void completeInterfaceShouldContainRequiredFieldsFromBaseInterface(String className, CtModel model) {

        final CtType<?> baseInterface = getBaseIfAst(model, className);
        final CtType<?> completeInterface = getCompleteIfAst(model, className);

        assertThat(getMethods(completeInterface).map(AaiAnnotationProcessorTestBase::removeAnnotations))
                .containsAll(
                        getAllMethods(baseInterface)
                                .filter(x -> x.hasAnnotation(AaiRequired.class))
                                .map(AaiAnnotationProcessorTestBase::removeAnnotations));
    }

    @ParameterizedTest
    @MethodSource("getCompilationPassingSources")
    void completeInterfaceShouldContainOptionalFieldsFromBaseInterface(String className, CtModel model) {

        final CtType<?> baseInterface = getBaseIfAst(model, className);
        final CtType<?> completeInterface = getCompleteIfAst(model, className);

        assertThat(getMethods(completeInterface).map(AaiAnnotationProcessorTestBase::removeAnnotations))
                .containsAll(
                        getAllMethods(baseInterface)
                                .filter(x -> x.hasAnnotation(AaiOptional.class))
                                .map(AaiAnnotationProcessorTestBase::removeAnnotations));
    }

    @ParameterizedTest
    @MethodSource("getCompilationPassingSources")
    void completeInterfaceShouldContainFieldsOfRequiredInterface(String className, CtModel model) {

        final CtType<?> requiredInterface = getRequiredIfAst(model, className);
        final CtType<?> completeInterface = getCompleteIfAst(model, className);

        assertThat(getMethods(completeInterface)).containsAll(getMethods(requiredInterface));
    }
}
