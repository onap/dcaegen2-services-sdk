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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(stagedBuilder = true)
public interface FactoryBuildStep {
    String prefix();

    ClassName returns();

    Function<ModelFiles, TypeName[]> processTypes();

    BiFunction<String, TypeName[], CodeBlock> processBody();

    @Value.Default
    default BiFunction<TypeName[], TypeVariableName[], ParameterSpec[]> processArgs() {
        return (omit1, omit2) -> new ParameterSpec[]{};
    }

    @Value.Default
    default AnnotationSpec[] annotations() {
        return new AnnotationSpec[]{};
    }

    @Value.Default
    default TypeVariableName[] genericParams() {
        return new TypeVariableName[]{};
    }

    default TypeName[] generateTypes(ModelFiles files) {
        return processTypes().apply(files);
    }

    default CodeBlock generateBody(ModelFiles files) {
        return processBody().apply(prefix(), processTypes().apply(files));
    }

    default ParameterSpec[] generateParams(ModelFiles files) {
        return processArgs().apply(generateTypes(files), genericParams());
    }
}
