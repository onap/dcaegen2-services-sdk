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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor.generator;

import static com.squareup.javapoet.AnnotationSpec.builder;
import static com.squareup.javapoet.ClassName.bestGuess;
import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.MethodSpec.Builder;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;
import static io.vavr.collection.List.of;
import static io.vavr.collection.Stream.ofAll;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.immutables.value.Value.Style;

import com.google.gson.annotations.SerializedName;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.vavr.collection.List;
import java.util.function.Predicate;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.Nullable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiDeletable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ClassMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ImmutableModelFiles;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.MethodMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ModelFiles;

public final class AaiModelsGenerator {
    public static final String REQUIRED_MODEL_POSTFIX = "Required";
    public static final String COMPLETE_MODEL_POSTFIX = "Complete";

    private static final String IMMUTABLES_META_INF = "metainfService";
    private static final String IMMUTABLES_STAGE_BUILDER = "stagedBuilder";
    private static final String ANNOTATION_DEFAULT_FIELD = "value";

    private static final String IMMUTABLES_SUPPRESSION = "immutables:subtype";

    private AaiModelsGenerator() {
    }

    private static TypeSpec.Builder addMethods(ClassMeta meta, TypeSpec.Builder builder, Predicate<MethodMeta> filter) {
        return meta
                .getMethodMeta()
                .filterValues(filter)
                .foldLeft(builder, (acc, spec) -> acc.addMethod(generateMethod(spec._1, spec._2)));
    }

    /**
     * @param meta info about AaiPojo annotated class.
     * @return representation of two generated interfaces *Required, *Complete.
     * @throws NullPointerException if meta is null.
     */
    public static ModelFiles generateAaiModelsFromMeta(ClassMeta meta) {

        final TypeSpec reqSpec = addMethods(
            meta,
            interfaceBase(meta, REQUIRED_MODEL_POSTFIX),
            MethodMeta::isRequired
        )   .addSuperinterface(AaiModel.class)
            .build();

        final TypeSpec comSpec = addMethods(
            meta,
            interfaceBase(meta, COMPLETE_MODEL_POSTFIX),
            omit -> true
        )   .addSuperinterface(AaiRelationable.class)
            .addSuperinterface(AaiDeletable.class)
            .addSuperinterface(bestGuess(reqSpec.name))
            .build();

        final List<JavaFile> files =
                of(reqSpec, comSpec)
                    .map(x -> JavaFile.builder(meta.packageName(), x).build());

        return ImmutableModelFiles
                .builder()
                .required(files.get(0))
                .complete(files.get(1))
                .build();
    }

    private static AnnotationSpec annotate(Class<?> type, String fieldName, Object value) {
        return builder(type)
                .addMember(fieldName, (value instanceof String) ? "$S" : "$L", value)
                .build();
    }

    private static TypeSpec.Builder interfaceBase(ClassMeta meta, String postfix) {
        return interfaceBuilder(get(meta.packageName(), meta.className() + postfix))
                .addSuperinterfaces(ofAll(meta.type().getInterfaces()).map(TypeName::get))
                .addModifiers(PUBLIC)
                .addAnnotation(annotate(Gson.TypeAdapters.class, IMMUTABLES_META_INF, false))
                .addAnnotation(Value.Immutable.class)
                .addAnnotation(annotate(Style.class, IMMUTABLES_STAGE_BUILDER, true))
                .addAnnotation(annotate(SuppressWarnings.class, ANNOTATION_DEFAULT_FIELD, IMMUTABLES_SUPPRESSION));
    }

    private static MethodSpec generateMethod(MethodSpec spec, MethodMeta meta) {

        final Builder builder = spec
                .toBuilder()
                .addAnnotations(meta.otherAnnotations());

        if (meta.aaiName().isEmpty()) {
            return builder.build();
        }

        if (!meta.isRequired()) {
            builder.addAnnotation(Nullable.class);
        }

        return builder
                .addAnnotation(annotate(SerializedName.class, ANNOTATION_DEFAULT_FIELD, meta.aaiName().get()))
                .build();
    }
}
