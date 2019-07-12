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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor.generator;

import static com.squareup.javapoet.ClassName.bestGuess;
import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.CodeBlock.of;
import static com.squareup.javapoet.MethodSpec.Builder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;
import static io.vavr.collection.Stream.continually;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.generator.AaiUriGenerator.createObjectToUriMapping;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.General.array;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.SpecUtils.asType;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.SpecUtils.createFieldWithComplexInitializer;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.SpecUtils.createSpecialized;

import com.google.gson.TypeAdapterFactory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.concurrent.ConcurrentHashMap;
import javax.lang.model.element.Modifier;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiAddRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiCreateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiDeleteRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiGetRelationAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.actions.AaiUpdateAction;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.factory.AaiAbstractActionFactory;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.interfaces.AaiRelationable;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.GsonAdaptersRelatedToProperty;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.GsonAdaptersRelationship;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.GsonAdaptersRelationshipData;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.models.GsonAdaptersRelationshipList;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ClassMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.FactoryBuildStep;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ImmutableFactoryBuildStep;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ModelFiles;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.SpecUtils;

public final class AaiFactoryBaseGenerator {
    private static final String PACKAGE = AaiAbstractActionFactory.class.getPackage().getName();
    private static final String IF_NAME = "AaiActionFactory";
    private static final String BASE_NAME = "AaiActionFactoryBase";
    private static final String TYPE_ADAPTER_PREFIX = "GsonAdapters";

    private static final TypeVariableName AAI_MODEL_TYPE = TypeVariableName.get("T", AaiModel.class);
    private static final TypeVariableName AAI_RELATIONABLE_TYPE = TypeVariableName.get("T", AaiRelationable.class);

    private static final ParameterizedTypeName MAPPING_KEY_TYPE = ParameterizedTypeName.get(
            get(Class.class),
            WildcardTypeName.subtypeOf(Object.class));

    private static final ParameterizedTypeName MAPPING_VALUE_TYPE = ParameterizedTypeName.get(
            get(java.util.function.Function.class),
            WildcardTypeName.subtypeOf(Object.class),
            get(String.class));

    private static final String URI_MAPPING_FIELD_NAME = "uriMappings";

    private static final ParameterizedTypeName URI_MAPPING_IF_TYPE = ParameterizedTypeName.get(
            get(java.util.Map.class),
            MAPPING_KEY_TYPE,
            MAPPING_VALUE_TYPE);

    private static final ParameterizedTypeName URI_MAPPING_TYPE = ParameterizedTypeName.get(
            get(ConcurrentHashMap.class),
            MAPPING_KEY_TYPE,
            MAPPING_VALUE_TYPE);

    private static final ParameterizedTypeName TYPE_ADAPTERS_IF_TYPE = ParameterizedTypeName.get(
            get(java.util.List.class),
            get(TypeAdapterFactory.class));

    private static final ParameterizedTypeName TYPE_ADAPTERS_TYPE = ParameterizedTypeName.get(
            get(java.util.ArrayList.class),
            get(TypeAdapterFactory.class));

    private static final List<Class<? extends TypeAdapterFactory>> ADDITIONAL_TYPE_ADAPTERS = List.of(
            GsonAdaptersRelationship.class,
            GsonAdaptersRelationshipList.class,
            GsonAdaptersRelationshipData.class,
            GsonAdaptersRelatedToProperty.class);

    private static final String TYPE_ADAPTERS_FIELD_NAME = "typeAdapters";

    private static final ParameterizedTypeName AAI_MAPPING_IF_TYPE = ParameterizedTypeName.get(
            get(java.util.Map.class),
            MAPPING_KEY_TYPE,
            get(String.class));

    private static final ParameterizedTypeName AAI_MAPPING_TYPE = ParameterizedTypeName.get(
            get(ConcurrentHashMap.class),
            MAPPING_KEY_TYPE,
            get(String.class));

    private static final String AAI_MAPPING_FIELD_NAME = "aaiTypeMappings";

    private static final List<FactoryBuildStep> METHODS = List.of(
            ImmutableFactoryBuildStep.builder().prefix("get")
                    .returns(get(AaiGetAction.class))
                    .processTypes(files -> array(asType(files.required()), asType(files.complete())))
                    .processBody((name, type) -> of("return $N($T.class, $T.class)", name, type[0], type[1]))
                    .build(),
            ImmutableFactoryBuildStep.builder().prefix("create")
                    .returns(get(AaiCreateAction.class))
                    .processTypes(files -> array(asType(files.complete())))
                    .processBody((name, type) -> of("return $N($T.class)", name, type[0]))
                    .build(),
            ImmutableFactoryBuildStep.builder().prefix("update")
                    .returns(get(AaiUpdateAction.class))
                    .processTypes(files -> array(asType(files.complete())))
                    .processBody((name, type) -> of("return $N($T.class)", name, type[0]))
                    .build(),
            ImmutableFactoryBuildStep.builder().prefix("delete")
                    .returns(get(AaiDeleteAction.class))
                    .processTypes(files -> array(asType(files.complete())))
                    .processBody((name, type) -> of("return $N($T.class)", name, type[0]))
                    .build(),
            ImmutableFactoryBuildStep.builder().prefix("getRelation")
                    .returns(get(AaiGetRelationAction.class))
                    .processTypes(files -> array(AAI_RELATIONABLE_TYPE, asType(files.required())))
                    .processBody((name, type) ->
                            of("return x -> $N(cls, $T.class).call(x)", name, type[1]))
                    .genericParams(AAI_RELATIONABLE_TYPE)
                    .processArgs((ret, type) -> array(createSpecialized("cls", get(Class.class), type[0])))
                    .build(),
            ImmutableFactoryBuildStep.builder().prefix("addRelation")
                    .returns(get(AaiAddRelationAction.class))
                    .processTypes(files -> array(asType(files.required()), AAI_MODEL_TYPE))
                    .processBody((name, type) ->
                            of("return x -> $N($T.class, cls).call(x)", name, type[0]))
                    .genericParams(AAI_MODEL_TYPE)
                    .processArgs((ret, type) -> array(createSpecialized("cls", get(Class.class), type[0])))
                    .build(),
            ImmutableFactoryBuildStep.builder().prefix("deleteRelation")
                    .returns(get(AaiDeleteRelationAction.class))
                    .processTypes(files -> array(asType(files.complete()), AAI_MODEL_TYPE))
                    .processBody((name, type) ->
                            of("return x -> $N($T.class, cls).call(x)", name, type[0]))
                    .genericParams(AAI_MODEL_TYPE)
                    .processArgs((ret, type) -> array(createSpecialized("cls", get(Class.class), type[0])))
                    .build());

    private AaiFactoryBaseGenerator() {
    }

    private static Map<MethodSpec, CodeBlock> processSteps(ClassMeta meta, ModelFiles files) {
        return METHODS
                .toMap(step -> methodBuilder(step.prefix() + meta.className())
                                .addModifiers(PUBLIC)
                                .addTypeVariables(Stream.of(step.genericParams()))
                                .addAnnotations(Stream.of(step.annotations()))
                                .addParameters(Stream.of(step.generateParams(files)))
                                .returns(ParameterizedTypeName.get(step.returns(), step.generateTypes(files)))
                                .build(),
                    step -> step.generateBody(files));
    }

    private static FieldSpec createUriMap(Map<ClassMeta, ModelFiles> classes) {
        final CodeBlock mappings = classes
                .toMap(pair -> pair._2, pair -> createObjectToUriMapping(pair._1, pair._2))
                .mapKeys(Stream::ofAll)
                .mapKeys(files -> files.map(SpecUtils::asType))
                .flatMap((meta, type) -> meta.zip(continually(type)))
                .map(pair -> of("put($T.class, $L)", pair._1, pair._2))
                .fold(of(""), SpecUtils::joinStatements);

        return createFieldWithComplexInitializer(
                URI_MAPPING_FIELD_NAME,
                URI_MAPPING_IF_TYPE,
                URI_MAPPING_TYPE,
                mappings);
    }

    private static FieldSpec createTypeAdapters(Map<ClassMeta, ModelFiles> classes) {
        final CodeBlock adapters = classes
                .values()
                .flatMap(identity())
                .map(type -> format("%s.%s%s", type.packageName, TYPE_ADAPTER_PREFIX, type.typeSpec.name))
                .map(ClassName::bestGuess)
                .appendAll(ADDITIONAL_TYPE_ADAPTERS.map(ClassName::get))
                .map(type -> of("add(new $T())", type))
                .fold(of(""), SpecUtils::joinStatements);

        return createFieldWithComplexInitializer(
                TYPE_ADAPTERS_FIELD_NAME,
                TYPE_ADAPTERS_IF_TYPE,
                TYPE_ADAPTERS_TYPE,
                adapters);
    }

    private static FieldSpec createAaiTypesMap(Map<ClassMeta, ModelFiles> classes) {
        final CodeBlock mappings = classes
                .mapKeys(ClassMeta::aaiType)
                .mapValues(ModelFiles::required)
                .mapValues(SpecUtils::asType)
                .map(pair -> of("put($T.class, $S)", pair._2, pair._1))
                .fold(of(""), SpecUtils::joinStatements);

        return createFieldWithComplexInitializer(
                AAI_MAPPING_FIELD_NAME,
                AAI_MAPPING_IF_TYPE,
                AAI_MAPPING_TYPE,
                mappings);
    }

    /**
     * @param classes info about AaiPojo annotated class and interfaces generated from it.
     * @return representation of interface for AAI API client and abstract class for
     *     client implementation simplification.
     * @throws NullPointerException if classes is null.
     */
    public static Stream<JavaFile> generateFactoryClasses(Map<ClassMeta, ModelFiles> classes) {

        final Seq<Map<MethodSpec, CodeBlock>> methods = classes.map(pair -> processSteps(pair._1, pair._2));
        final TypeSpec factoryInterface =
                interfaceBuilder(get(PACKAGE, IF_NAME))
                        .addModifiers(PUBLIC)
                        .addMethods(methods
                                .flatMap(Map::keysIterator)
                                .map(MethodSpec::toBuilder)
                                .map(x -> x.addModifiers(ABSTRACT))
                                .map(Builder::build)
                        ).build();

        final Stream<FieldSpec> fields = Stream.of(
                createUriMap(classes),
                createTypeAdapters(classes),
                createAaiTypesMap(classes));

        final TypeSpec factoryAbstractBase =
                classBuilder(get(PACKAGE, BASE_NAME))
                        .addModifiers(PUBLIC, ABSTRACT)
                        .superclass(AaiAbstractActionFactory.class)
                        .addSuperinterface(bestGuess(factoryInterface.name))
                        .addMethods(methods
                                .flatMap(x -> x.mapKeys(MethodSpec::toBuilder))
                                .map(pair -> pair._1.addStatement(pair._2))
                                .map(Builder::build)
                        ).addFields(fields
                                .map(FieldSpec::toBuilder)
                                .map(field -> field.addModifiers(Modifier.PUBLIC, Modifier.FINAL))
                                .map(FieldSpec.Builder::build)
                        ).build();

        return Stream
                .of(factoryInterface, factoryAbstractBase)
                .map(spec -> JavaFile.builder(PACKAGE, spec).build());
    }
}