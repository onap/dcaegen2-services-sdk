/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019-2021 Nokia. All rights reserved.
 * Copyright (C) 2023 Deutsche Telekom AG. All rights reserved.
 * =========================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================
 */

package org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;

import org.apache.kafka.clients.admin.AdminClient;
import org.onap.dcaegen2.services.sdk.model.streams.AafCredentials;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since April 2019
 */
public final class Commons {
    static String commonInURL = "/events/";
    static String KAFKA_PROPS_PREFIX = "kafka.";

    private static final Logger LOGGER = LoggerFactory.getLogger(Commons.class);
    private static AdminClient kafkaAdminClient;
    private static Map<String,Object> map = new HashMap<>();

    static {
        map.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        map.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        map.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        map.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        map.put("max.poll.interval.ms", 300000);
        map.put("heartbeat.interval.ms", 60000);
        map.put("session.timeout.ms", 240000);
        map.put("max.poll.records", 1000);
    }
    private Commons() {
    }

    static String extractFailReason(HttpResponse httpResponse) {
        return String.format("%d %s%n%s", httpResponse.statusCode(), httpResponse.statusReason(),
                httpResponse.bodyAsString());
    }

    static Tuple2<String, String> basicAuthHeader(AafCredentials credentials) {
        Objects.requireNonNull(credentials, "aafCredentials");
        String basicAuthFormat = basicAuthFormat(credentials);
        byte[] utf8 = bytesUTF8(basicAuthFormat);
        String userCredentials = Base64.getEncoder().encodeToString(utf8);
        return Tuple.of("Authorization", "Basic " + userCredentials);
    }

    private static String basicAuthFormat(AafCredentials credentials) {
        String username = getOrEmpty(credentials.username());
        String password = getOrEmpty(credentials.password());
        return username.concat(":").concat(password);
    }

    private static String getOrEmpty(String text) {
        return Option.of(text).getOrElse("");
    }

    private static byte[] bytesUTF8(String text) {
        return Option.of(text)
                .map(s -> s.getBytes(StandardCharsets.UTF_8))
                .getOrElse(new byte[0]);
    }
    /**
     * Extracts the topic name from the topicUrl.
     * 
     * <p>Condition for extracting topic name : Substring after '/events/' in the topicUrl</p>
     * 
     * @param topicUrl
     * @return topic
     */
    public static String getTopicFromTopicUrl(String topicUrl) {
        if(topicUrl.endsWith("/")) {
            return topicUrl.substring(topicUrl.indexOf(commonInURL)+commonInURL.length(), topicUrl.lastIndexOf("/"));
        }
        return  topicUrl.substring(topicUrl.indexOf(commonInURL)+commonInURL.length());
    }
    
    public static Properties setKafkaPropertiesFromSystemEnv(Map<String, String> envs) {
        Map<String, Object> propMap= getKafkaPropertiesMap(envs);
        Properties props = new Properties();
        propMap.forEach((k ,v) -> props.put(k, v));
        map.forEach((k,v) -> {
            if(!propMap.containsKey(k)) {
                props.put(k, v);
            }
        });

        return props;
    }
    
    static Map<String, Object> getKafkaPropertiesMap(Map<String, String> envs){
        Map<String, Object> propMap = new HashMap<>();
        envs.forEach((k ,v) -> {
            if(k.startsWith(KAFKA_PROPS_PREFIX)){
                String key = k.substring(KAFKA_PROPS_PREFIX.length());
                propMap.put(key, v);
            }
        });
        return propMap;
    }

    public static void closeKafkaAdminClient() {
        if(kafkaAdminClient != null) {
            LOGGER.info("Closing the Kafka AdminClient.");
            kafkaAdminClient.close();
            kafkaAdminClient=null;
        }
    }

    public static boolean checkIfTopicIsPresentInKafka(String topic, Properties adminProps) {
        if(kafkaAdminClient == null) {
            kafkaAdminClient = AdminClient.create(adminProps);
        }
        try {
            for (String name : kafkaAdminClient.listTopics().names().get()) {
                if (name.equals(topic)) {
                    LOGGER.debug("TOPIC_NAME: {} is equal to : {}", name, topic);
                    return true;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("GetTopicFromKafka: Failed to retrieve topic list from kafka.", e);
            return false;
        }
        return false;
    }
    
}
