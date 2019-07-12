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

import static io.vavr.collection.Stream.of;
import static io.vavr.collection.Stream.ofAll;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public final class SpecUtils {
    private SpecUtils() {
    }

    /**
     * @param element from which annotations should be obtained.
     * @return all annotations converted to AnnotationSpec.
     * @throws NullPointerException if element is null.
     */
    public static Stream<AnnotationSpec> toAnnotationsSpec(Element element) {
        return ofAll(element.getAnnotationMirrors())
                .map(AnnotationSpec::get);
    }

    /**
     * @param file JavaFile object from which topmost type ClassName will be generated.
     * @return topmost type ClassName.
     * @throws NullPointerException if file is null.
     */
    public static ClassName asType(JavaFile file) {
        return ClassName.get(file.packageName, file.typeSpec.name);
    }

    /**
     * @param element that represents a method.
     * @return method builder presetted with signature of element.
     * @throws NullPointerException if element is null.
     */
    public static MethodSpec.Builder copySignature(ExecutableElement element) {
        return MethodSpec
                .methodBuilder(element.getSimpleName().toString())
                .returns(TypeName.get(element.getReturnType()))
                .addModifiers(element.getModifiers())
                .addExceptions(ofAll(element.getThrownTypes()).map(TypeName::get))
                .addTypeVariables(ofAll(element.getTypeParameters()).map(TypeVariableName::get))
                .addParameters(ofAll(element.getParameters()).map(ParameterSpec::get));
    }

    /**
     * @param blocks are a part of method.
     * @return blocks separated with ';' and a new line.
     * @throws NullPointerException if one of blocks is null.
     */
    public static Option<CodeBlock> reduceStatements(CodeBlock ...blocks) {
        return of(blocks).reduceOption((previous, next) -> previous.toBuilder().addStatement(next).build());
    }

    /**
     * @param first is the first line of code.
     * @param second is the second line of code.
     * @return block x and y separated with ';' and a new line.
     * @throws NullPointerException if x or y is null.
     */
    public static CodeBlock joinStatements(CodeBlock first, CodeBlock second) {
        return reduceStatements(first, second).get();
    }

    /**
     * @param fieldName name of the field to be created.
     * @param decType type declaration of the field.
     * @param impType type with which the field will be initialized.
     * @param initializer the code which will be called during field construction (new impType(){{ initializer }}).
     * @param modifiers field modifiers (FINAL, PUBLIC, PRIVATE, etc.).
     * @return field specification.
     * @throws NullPointerException if one of params is null.
     */
    public static FieldSpec createFieldWithComplexInitializer(
            String fieldName,
            TypeName decType,
            TypeName impType,
            CodeBlock initializer,
            Modifier ...modifiers) {

        return FieldSpec.builder(
                decType,
                fieldName,
                modifiers
        ).initializer(CodeBlock
                .builder()
                .add("new $T()", impType)
                .add("{{\n")
                .indent()
                .add("$L", initializer)
                .unindent()
                .add("\n}}")
                .build()
        ).build();
    }

    public static ParameterSpec createSpecialized(String name, ClassName type, TypeName... args) {
        return ParameterSpec
                .builder(ParameterizedTypeName.get(type, args), name)
                .build();
    }
}
