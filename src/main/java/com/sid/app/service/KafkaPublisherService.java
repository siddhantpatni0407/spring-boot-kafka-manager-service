package com.sid.app.service;

import com.sid.app.config.AppProperties;
import com.sid.app.model.Customer;
import com.sid.app.model.Response;
import com.sid.app.utils.ApplicationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Message Publisher Service
 * Handles publishing messages to Kafka topics asynchronously.
 */
@Service
@Slf4j
public class KafkaPublisherService {

    @Autowired
    private AppProperties properties;

    @Autowired
    private KafkaTemplate<String, Object> template;

    /**
     * Sends a message to the specified Kafka topic asynchronously.
     *
     * @param topicName The Kafka topic to send the message to.
     * @param message   The message to send.
     * @return a Mono<Response> indicating success or failure.
     */
    public Mono<Response> sendMessageToTopic(String topicName, String message) {
        Response response = Response.builder().build();

        log.info("sendMessageToTopic() : Sending message to topic '{}' -> {}", topicName, message);

        // KafkaTemplate.send() returns a CompletableFuture in Spring Boot 6+
        CompletableFuture<SendResult<String, Object>> future = template.send(topicName, message);

        return Mono.fromFuture(future.thenApply(result -> {
            log.info("sendMessageToTopic() : Message sent to Topic -> {} with Partition -> {} and Offset ->{}", topicName, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            response.setStatus("Message sent to Topic -> " + topicName + " with Partition -> " + result.getRecordMetadata().partition() + " and Offset -> " + result.getRecordMetadata().offset());
            return response;
        }).exceptionally(exception -> {
            log.error("sendMessageToTopic() : Failed to send message to topic '{}' due to {}", topicName, exception.getMessage(), exception);
            response.setStatus("Unable to send message due to " + exception.getMessage());
            return response;
        }));
    }

    /**
     * Sends a simple message to Kafka asynchronously.
     *
     * @param message the message to send
     * @return a Mono<Response> indicating success or failure
     */
    public Mono<Response> sendMessageToTopic(String message) {
        Response response = Response.builder().build();

        // Spring Boot 6+ KafkaTemplate.send() returns a CompletableFuture
        CompletableFuture<SendResult<String, Object>> future = template.send(properties.getKafkaTopic(), message);

        return Mono.fromFuture(future.thenApply(result -> {
            log.info("Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
            response.setStatus("Message sent to Kafka successfully. Offset -> " + result.getRecordMetadata().offset());
            return response;
        }).exceptionally(exception -> {
            log.error("Unable to send message=[{}] due to {}", message, exception.getMessage());
            response.setStatus("Unable to send message due to " + exception.getMessage());
            return response;
        }));
    }

    /**
     * Sends a customer event to Kafka asynchronously.
     *
     * @param customer the customer event data
     * @return a Mono<Response> indicating success or failure
     */
    public Mono<Response> sendEventsToTopic(Customer customer) {
        Response response = Response.builder().build();

        CompletableFuture<SendResult<String, Object>> future = template.send(properties.getKafkaTopic(), customer);

        return Mono.fromFuture(future.thenApply(result -> {
            log.info("Sent event=[{}] with offset=[{}]", ApplicationUtils.getJSONString(customer), result.getRecordMetadata().offset());
            response.setStatus("Event published successfully. Offset -> " + result.getRecordMetadata().offset());
            return response;
        }).exceptionally(exception -> {
            log.error("Unable to send event=[{}] due to {}", customer, exception.getMessage());
            response.setStatus("Unable to send event due to " + exception.getMessage());
            return response;
        }));
    }

}