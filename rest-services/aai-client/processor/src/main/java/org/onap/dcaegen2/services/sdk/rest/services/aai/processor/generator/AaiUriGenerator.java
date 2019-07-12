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

import static com.squareup.javapoet.CodeBlock.join;
import static io.vavr.Tuple.of;
import static java.lang.String.format;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.General.asStream;
import static org.onap.dcaegen2.services.sdk.rest.services.aai.processor.utils.SpecUtils.asType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ClassMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.MethodMeta;
import org.onap.dcaegen2.services.sdk.rest.services.aai.processor.models.ModelFiles;

public class AaiUriGenerator {
    private static final String AAI_FIELD_NOT_FOUND_ON_TYPE = "AAI field %s wasn't found on type %s";

    private static final String OBJECT_REF = "x";
    private static final Pattern URI_VARIABLE_REGEX = Pattern.compile("\\/\\$\\{(.+?)\\}");

    private static CodeBlock replaceUriVarWithMethodCall(String prefix, String method) {
        return CodeBlock.of("$S + $L.$L()", prefix + "/", OBJECT_REF, method);
    }

    private static RuntimeException fail(String method, String type) {
        return new RuntimeException(format(AAI_FIELD_NOT_FOUND_ON_TYPE, method, type));
    }

    private static CodeBlock generateTypedLambda(ClassMeta meta) {
        final String endpoint = meta.aaiPath();
        final List<MatchResult> matches = asStream(URI_VARIABLE_REGEX.matcher(endpoint)).toList();
        final Map<String, String> aaiMappings = meta
                .getMethodMeta()
                .values()
                .filter(MethodMeta::isRequired)
                .filter(x -> x.aaiName().isDefined())
                .toMap(x -> x.aaiName().get(), x -> x.methodName());

        return matches
                .map(MatchResult::end)
                .prepend(0)
                .zipWith(matches, (idx, match) -> of(endpoint.substring(idx, match.start()), match.group(1)))
                .map(pair -> pair.map2(method ->
                        aaiMappings
                                .get(method)
                                .getOrElseThrow(() -> fail(method, meta.className())))
                ).map(pair -> replaceUriVarWithMethodCall(pair._1, pair._2))
                 .reduceOption((first, second) -> join(Stream.of(first, second), " + "))
                 .getOrElse(CodeBlock.of("$S", endpoint));
    }

    /**
     * @param meta info about AaiPojo annotated class.
     * @param files representation of three generated interfaces *Required, *Complete, *Deletable from AaiPojo.
     * @return code block representing lambda function which transform *Required object to URL.
     * @throws NullPointerException if meta or files is null.
     */
    public static CodeBlock createObjectToUriMapping(ClassMeta meta, ModelFiles files) {

        final ClassName type = asType(files.required());
        final CodeBlock lambdaBody = generateTypedLambda(meta);

        return CodeBlock.of("($T $L) -> $L", type, OBJECT_REF, lambdaBody);
    }
}
