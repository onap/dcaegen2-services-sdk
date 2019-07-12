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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor.parsers;

import static io.vavr.Predicates.not;
import static io.vavr.collection.Stream.iterate;
import static io.vavr.collection.Stream.ofAll;
import static io.vavr.control.Option.of;
import static java.util.function.Function.identity;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ExceptionCode.AAI_OPTIONAL_FILED_ON_PRIMITIVE_TYPE_ERROR;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.exceptions.ParseException.create;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.SpecUtils.copySignature;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.SpecUtils.toAnnotationsSpec;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiOptional;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ClassMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ImmutableClassMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ImmutableMethodMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.MethodMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.validators.Validator;

public final class AaiPojoParser {
    private static final Set<String> annotations = HashSet
            .of(AaiPojo.class, AaiRequired.class, AaiOptional.class)
            .map(Class::getCanonicalName);

    private AaiPojoParser() {
    }

    private static boolean checkIfAaiAnnotation(AnnotationSpec spec) {
        return annotations.contains(spec.type.toString());
    }

    private static List<AnnotationSpec> getNonAaiAnnotations(Element element) {
        return toAnnotationsSpec(element)
                .filter(not(AaiPojoParser::checkIfAaiAnnotation))
                .toList();
    }

    private static Stream<Tuple2<MethodSpec, MethodMeta>> processMethods(TypeElement type) {
        return ofAll(type.getEnclosedElements())
                .map(x -> AaiPojoParser.parseMethod(type, x));
    }

    /**
     * @param element      AaiPojo annotated interface.
     * @param typesUtils   utils for processing types.
     * @param elementUtils utils for processing element tree.
     * @return parsed mata-data obtained from Aai* annotations.
     * @throws RuntimeException if element isn't an interface, is nested inside other type,
     *                          contains generic method, etc.
     */
    public static ClassMeta parseInterface(
            Element element,
            Types typesUtils,
            Elements elementUtils) {

        Validator.validateIfTypeIsNotNested(element);

        final TypeElement type = Validator.validateTypeAnnotatedWithAaiModel(element);
        final AaiPojo annotation = type.getAnnotation(AaiPojo.class);

        final Name className = type.getSimpleName();
        final Name packageName = elementUtils.getPackageOf(type).getQualifiedName();

        return ImmutableClassMeta
                .builder()
                .type(type)
                .aaiType(annotation.type())
                .aaiPath(annotation.path())
                .packageName(packageName.toString())
                .className(className.toString())
                .otherAnnotations(getNonAaiAnnotations(type))
                .methodMeta(
                        iterate(
                                Stream.of(type), tpe -> tpe
                                        .flatMap(TypeElement::getInterfaces)
                                        .map(typesUtils::asElement)
                                        .map(Validator::validateTypeAnnotatedWithAaiModel)
                        ).drop(1)
                                .takeWhile(not(Stream::isEmpty))
                                .flatMap(identity())
                                .flatMap(AaiPojoParser::processMethods)
                                .appendAll(processMethods(type))
                                .toMap(identity())
                ).build();
    }

    private static Tuple2<MethodSpec, MethodMeta> parseMethod(TypeElement parent, Element element) {
        final ExecutableElement method = Validator.validateMemberOfTypeAnnotatedWithAaiModel(parent, element);
        final Option<String> required = of(method.getAnnotation(AaiRequired.class)).map(AaiRequired::value);
        final Option<String> optional = of(method.getAnnotation(AaiOptional.class)).map(AaiOptional::value);

        if (optional.isDefined() && method.getReturnType().getKind().isPrimitive()) {
            throw create(
                    AAI_OPTIONAL_FILED_ON_PRIMITIVE_TYPE_ERROR,
                    method.getSimpleName().toString(),
                    parent.getQualifiedName().toString());
        }

        return Tuple.of(
                copySignature(method).build(),
                ImmutableMethodMeta
                        .builder()
                        .methodName(method.getSimpleName().toString())
                        .aaiName(required.orElse(optional))
                        .isRequired(required.isDefined())
                        .otherAnnotations(getNonAaiAnnotations(method))
                        .build());
    }
}
