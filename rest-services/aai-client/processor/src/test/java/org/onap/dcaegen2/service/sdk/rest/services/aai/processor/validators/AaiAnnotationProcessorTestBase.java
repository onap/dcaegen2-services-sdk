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

import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static io.vavr.Predicates.not;
import static io.vavr.collection.Stream.iterate;
import static io.vavr.collection.Stream.of;
import static io.vavr.collection.Stream.ofAll;
import static java.nio.file.Files.lines;
import static java.util.function.Function.identity;

import com.google.testing.compile.Compilation;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.tools.JavaFileObject;
import org.immutables.processor.ProxyProcessor;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.AaiAnnotationsProcessor;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.compiler.VirtualFile;

public class AaiAnnotationProcessorTestBase {
    private static final String JAVA_EXTENSION = ".java";
    protected static final String REQUIRED_PREFIX = "Required";
    protected static final String COMPLETE_PREFIX = "Complete";
    protected static final Path BASE_PATH = Paths.get("src", "test", "resources");

    protected static String readFile(File file) throws IOException {

        try (java.util.stream.Stream<String> lines = lines(file.toPath())) {
            return lines.reduce("", (prev, next) -> prev + "\n" + next);
        }
    }

    protected static String readFile(InputStream source) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source))) {
            return reader.lines().reduce("", (prev, next) -> prev + "\n" + next);
        } finally {
            source.close();
        }
    }

    private static Launcher createLauncher(JavaFileObject... files) throws IOException {
        final Launcher launcher = new Launcher();

        for (final JavaFileObject file : of(files).filter(file -> file.getName().endsWith(JAVA_EXTENSION))) {
            final String contents = readFile(file.openInputStream());

            launcher.addInputResource(new VirtualFile(contents, file.getName()));
        }

        return launcher;
    }

    protected static Try<CtModel> createAst(JavaFileObject... files) {
        return Try.of(() -> createLauncher(files).buildModel());
    }

    protected static String getNameWithoutExtension(File file) {
        return getNameWithoutExtension(file.getName());
    }

    protected static String getNameWithoutExtension(String file) {
        return file.replaceFirst("[.][^.]+$", "");
    }

    protected Compilation compileWithCut(File... sourceFiles) throws IOException {
        final Map<String, String> files = of(sourceFiles)
                .toMap(file -> getNameWithoutExtension(file), file -> Try.of(() -> readFile(file)))
                .mapValues(Try::get);

        final JavaFileObject[] objects = files
                .map(pair -> forSourceString(pair._1, pair._2))
                .toJavaArray(JavaFileObject[]::new);

        return javac()
                .withOptions("-proc:only")
                .withProcessors(new AaiAnnotationsProcessor(), new ProxyProcessor())
                .compile(objects);
    }

    protected static Option<CtType<?>> getClassAst(CtModel model, String fileName) {
        return ofAll(model.getAllTypes())
                .filter(x -> getNameWithoutExtension(fileName).equals(x.getSimpleName()))
                .headOption();
    }

    protected static CtMethod removeAnnotations(CtMethod<?> meta) {
        CtMethod<?> cloned = meta.clone();

        for (final CtAnnotation<?> annotation : meta.getAnnotations()) {
            cloned.removeAnnotation(annotation);
        }

        return cloned;
    }

    @SafeVarargs
    protected static CtMethod removeAnnotation(CtMethod<?> method, Class<? extends Annotation>... annotationTypes) {
        final CtMethod<?> cloned = method.clone();

        for (final Class<? extends Annotation> clazz : annotationTypes) {
            final Option<CtAnnotation<?>> maybeAnnotation =
                    ofAll(cloned.getAnnotations())
                            .find(anot -> isOfType(anot, clazz));

            maybeAnnotation.peek(cloned::removeAnnotation);
        }

        return cloned;
    }

    protected static boolean isOfType(CtAnnotation<? extends Annotation> annotation, Class<?> clazz) {
        return annotation
                .getActualAnnotation()
                .annotationType()
                .equals(clazz);
    }

    private static String fileWithPostfix(String className, String postfix) {
        return className + postfix + JAVA_EXTENSION;
    }

    protected static CtType<?> getBaseIfAst(CtModel model, String className) throws RuntimeException {
        return getClassAst(model, fileWithPostfix(className, "")).get();
    }

    protected static CtType<?> getRequiredIfAst(CtModel model, String className) throws RuntimeException {
        return getClassAst(model, fileWithPostfix(className, REQUIRED_PREFIX)).get();
    }

    protected static CtType<?> getCompleteIfAst(CtModel model, String className) throws RuntimeException {
        return getClassAst(model, fileWithPostfix(className, COMPLETE_PREFIX)).get();
    }

    protected static Option<? extends CtMethod<?>> findMethodWithSameSignature(CtType<?> type, CtMethod<?> method) {
        final Stream<? extends CtType<?>> types = iterate(
                Stream.<CtType<?>>of(type), prev -> of(
                        prev
                                .flatMap(CtType::getSuperInterfaces)
                                .<CtType<?>>map(CtTypeReference::getDeclaration),
                        prev
                                .map(CtType::getSuperclass)
                                .flatMap(Option::of)
                                .<CtType<?>>map(CtTypeReference::getDeclaration)
                ).flatMap(identity())
        )
                .takeWhile(not(Stream::isEmpty))
                .flatMap(identity());

        return types
                .map(tpe ->
                        tpe.getMethod(
                                method.getType(),
                                method.getSimpleName(),
                                ofAll(method.getFormalCtTypeParameters())
                                        .map(CtTypeParameter::getReference)
                                        .toJavaArray(CtTypeParameterReference[]::new)))
                .flatMap(Option::of)
                .headOption();
    }

    protected static Stream<CtMethod<?>> getMethods(CtType<?> type) {
        return Stream.ofAll(type.getMethods());
    }

    protected static Stream<CtMethod<?>> getAllMethods(CtType<?> type) {
        return Stream.ofAll(type.getAllMethods());
    }
}