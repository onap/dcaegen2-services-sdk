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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.handler.timeout.ReadTimeoutException;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.scram.internals.ScramMechanism;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.model.streams.dmaap.KafkaSource;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.exceptions.RetryableException;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.api.MessageRouterSubscriber;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReason;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReasonPresenter;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.error.ClientErrorReasons;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterPublishResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.ImmutableMessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeRequest;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.MessageRouterSubscribeResponse;
import org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.model.config.DmaapTimeoutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Supplier;

import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.Commons.extractFailReason;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.Commons.getTopicFromTopicUrl;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.Commons.checkIfTopicIsPresentInKafka;
import static org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.impl.Commons.setProps;

/**
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since March 2019
 */
public class MessageRouterSubscriberImpl implements MessageRouterSubscriber {
    private final RxHttpClient httpClient;
    private final Gson gson;
    private final ClientErrorReasonPresenter clientErrorReasonPresenter;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRouterSubscriberImpl.class);
    private static Properties props;
    private static final String kafkaBootstrapServers = "BOOTSTRAP_SERVERS";
    private static Consumer<String, String> consumer;
    
    public MessageRouterSubscriberImpl(RxHttpClient httpClient, Gson gson,
                                       ClientErrorReasonPresenter clientErrorReasonPresenter) throws Exception {
        this.httpClient = httpClient;
        this.gson = gson;
        this.clientErrorReasonPresenter = clientErrorReasonPresenter;
        props = setProps(System.getenv());
       
        if(System.getenv(kafkaBootstrapServers) == null) { 
        	LOGGER.error("Environment Variable "+ kafkaBootstrapServers+" is missing");
        	throw new Exception("Environment Variable "+ kafkaBootstrapServers+" is missing");
        }else {
            props.put("bootstrap.servers", System.getenv(kafkaBootstrapServers));
        }
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,false);
//        
//
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,SecurityProtocol.SASL_PLAINTEXT.name);
        props.put(SaslConfigs.SASL_MECHANISM, ScramMechanism.SCRAM_SHA_512.mechanismName());
        props.put(SaslConfigs.SASL_JAAS_CONFIG, System.getenv("JAAS_CONFIG"));
//        
    }
    
    @Override
    public Mono<MessageRouterSubscribeResponse> get(MessageRouterSubscribeRequest request) {
        LOGGER.info("Requesting new items from DMaaP MR: {}", request);
        String topic = getTopicFromTopicUrl(request.sourceDefinition().topicUrl());
       
        String fakeGroupName = request.consumerGroup(); 
        props.put("client.id", request.consumerId());
        props.put("group.id", fakeGroupName);
        
        try{
        	if (consumer == null) {
        		if(!checkIfTopicIsPresentInKafka(topic,getAdminProps())) {
                	LOGGER.error("No such topic exists, TOPIC_NAME : {}", topic);
        			return Mono.just(ImmutableMessageRouterSubscribeResponse.builder()
        	                .failReason("404 Topic Not Found")
        	                .build());
                }
        		consumer = getKafkaConsumer(props);
        		consumer.subscribe(Arrays.asList(topic));
        	}
		ArrayList<String> msgs = new ArrayList<>();
        
        	ConsumerRecords<String, String> records = null;
			synchronized (consumer) {
				records = consumer.poll(Duration.ofMillis(500));
			}
				for (ConsumerRecord<String, String> rec : records) {
		            msgs.add(rec.value());
		        }
			List<JsonElement> list = List.ofAll(msgs).map(r -> JsonParser.parseString(r));
			return Mono.just(ImmutableMessageRouterSubscribeResponse.builder()
					.items(list)
					.build());
    	} catch(Exception e) {
			LOGGER.error("Error while consuming the messages : {}",e.getMessage());
			return Mono.just(ImmutableMessageRouterSubscribeResponse.builder()
	                .failReason(e.getMessage())
	                .build());
		}
    }
    
    @Override
    public void setConsumer(Consumer<String, String> consumer) {
		this.consumer = consumer;
	}
    
	public static KafkaConsumer<String, String> getKafkaConsumer(Properties props){
    	return new KafkaConsumer<>(props);
    }
    
	public static Properties getProps() {
		return props;
	}
    @Override
    public void close(){
    	if(consumer != null) {
    		LOGGER.info("Closing the Kafka Consumer");
    		consumer.close();
    		consumer = null;
    	}
    	Commons.closeKafkaAdminClient();
    }
      
//    @Override
//    public Mono<MessageRouterSubscribeResponse> get(MessageRouterSubscribeRequest request) {
//        LOGGER.debug("Requesting new items from DMaaP MR: {}", request);
//        return httpClient.call(buildGetHttpRequest(request))
//                .map(this::buildGetResponse)
//                .doOnError(ReadTimeoutException.class,
//                        e -> LOGGER.error("Timeout exception occurred when subscribe items from DMaaP MR", e))
//                .onErrorResume(ReadTimeoutException.class, e -> buildErrorResponse(ClientErrorReasons.TIMEOUT))
//                .doOnError(ConnectException.class, e -> LOGGER.error("DMaaP MR is unavailable, {}", e.getMessage()))
//                .onErrorResume(ConnectException.class, e -> buildErrorResponse(ClientErrorReasons.SERVICE_UNAVAILABLE))
//                .onErrorResume(RetryableException.class, e -> Mono.just(buildGetResponse(e.getResponse())));
//    }
    public static Properties getAdminProps() {
    	Properties adminProps = new Properties();
        adminProps.put("bootstrap.servers", System.getenv(kafkaBootstrapServers));
        adminProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,SecurityProtocol.SASL_PLAINTEXT.name);
        adminProps.put(SaslConfigs.SASL_MECHANISM, ScramMechanism.SCRAM_SHA_512.mechanismName());
        adminProps.put(SaslConfigs.SASL_JAAS_CONFIG, System.getenv("JAAS_CONFIG"));
        return adminProps;
    }
    
    private @NotNull HttpRequest buildGetHttpRequest(MessageRouterSubscribeRequest request) {
        return ImmutableHttpRequest.builder()
                .method(HttpMethod.GET)
                .url(buildSubscribeUrl(request))
                .diagnosticContext(request.diagnosticContext().withNewInvocationId())
                .customHeaders(headers(request))
                .timeout(timeout(request).getOrNull())
                .build();
    }

    private @NotNull MessageRouterSubscribeResponse buildGetResponse(HttpResponse httpResponse) {
        final ImmutableMessageRouterSubscribeResponse.Builder builder =
                ImmutableMessageRouterSubscribeResponse.builder();
        return httpResponse.successful()
                ? builder.items(getAsJsonElements(httpResponse)).build()
                : builder.failReason(extractFailReason(httpResponse)).build();
    }

    private List<JsonElement> getAsJsonElements(HttpResponse httpResponse) {
        JsonArray bodyAsJsonArray = httpResponse
                .bodyAsJson(StandardCharsets.UTF_8, gson, JsonArray.class);

        return List.ofAll(bodyAsJsonArray).map(arrayElement -> JsonParser.parseString(arrayElement.getAsString()));
    }

    private String buildSubscribeUrl(MessageRouterSubscribeRequest request) {
        return String.format("%s/%s/%s", request.sourceDefinition().topicUrl(), request.consumerGroup(),
                request.consumerId());
    }

    private Mono<MessageRouterSubscribeResponse> buildErrorResponse(ClientErrorReason clientErrorReason) {
        String failReason = clientErrorReasonPresenter.present(clientErrorReason);
        return Mono.just(ImmutableMessageRouterSubscribeResponse.builder()
                .failReason(failReason)
                .build());
    }

    private Option<Duration> timeout(MessageRouterSubscribeRequest request) {
        return Option.of(request.timeoutConfig())
                .map(DmaapTimeoutConfig::getTimeout);
    }

    private Map<String, String> headers(MessageRouterSubscribeRequest request) {
        return Option.of(request.sourceDefinition().aafCredentials())
                .map(Commons::basicAuthHeader)
                .map(HashMap::of)
                .getOrElse(HashMap.empty());
    }

	
	
}
