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

package org.onap.dcaegen2.services.sdk.rest.services.aai.processor;

import static io.vavr.collection.HashSet.of;
import static io.vavr.collection.HashSet.ofAll;
import static java.util.function.Function.identity;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import java.io.IOException;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiOptional;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiPojo;
import org.onap.dcaegen2.services.sdk.rest.services.aai.common.annotations.AaiRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.generator.AaiFactoryBaseGenerator;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.generator.AaiModelsGenerator;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ClassMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ModelFiles;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.parsers.AaiPojoParser;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class AaiAnnotationsProcessor extends AbstractProcessor {
    private final Set<Class<?>> annotations = of(AaiPojo.class, AaiRequired.class, AaiOptional.class);
    private Filer filer;
    private Types typesUtils;
    private Elements elementUtils;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        filer = processingEnv.getFiler();
        typesUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public java.util.Set<String> getSupportedAnnotationTypes() {
        return annotations
                .map(Class::getCanonicalName)
                .toJavaSet();
    }

    @Override
    public boolean process(java.util.Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        final Set<ClassMeta> classes = ofAll(set)
                .filter(x -> x.getQualifiedName().contentEquals(AaiPojo.class.getCanonicalName()))
                .flatMap(roundEnvironment::getElementsAnnotatedWith)
                .map(x -> AaiPojoParser.parseInterface(x, typesUtils, elementUtils));

        if (classes.isEmpty()) {
            return true;
        }

        final Map<ClassMeta, ModelFiles> generated =
                classes.toMap(identity(), AaiModelsGenerator::generateAaiModelsFromMeta);

        for (final JavaFile spec :
                generated
                        .values()
                        .flatMap(identity())
                        .appendAll(AaiFactoryBaseGenerator.generateFactoryClasses(generated))) {
            try {
                spec.writeTo(filer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }
}