package com.sid.app.controller;

import com.sid.app.constant.AppConstants;
import com.sid.app.model.Response;
import com.sid.app.service.KafkaConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Siddhant Patni
 */
@RestController
@Slf4j
@CrossOrigin
public class KafkaConsumerController {

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @GetMapping(value = AppConstants.KAFKA_CONSUME_MESSAGE_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> consumeMessages(@RequestParam(value = "topicName") String topicName) {
        log.info("consumeMessages() : Starting Kafka consumer for topic '{}' - START", topicName);

        try {
            return kafkaConsumerService.consumeMessages(topicName)
                    .flatMap(response -> {
                        log.info("consumeMessages() : Messages consumed successfully from topic '{}'", topicName);
                        return Mono.just(ResponseEntity.ok(response));
                    });
        } catch (Exception ex) {
            log.error("consumeMessages() : Error consuming messages from topic '{}': {}", topicName, ex.getMessage(), ex);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.builder().status("Error consuming messages: " + ex.getMessage()).build()));
        }
    }

    @GetMapping(value = AppConstants.KAFKA_CONSUME_LATEST_MESSAGE_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> consumeLatestMessage(@RequestParam(value = "topicName") String topicName) {
        log.info("consumeLatestMessage() : Starting Kafka consumer for the latest message from topic '{}' - START", topicName);

        try {
            return kafkaConsumerService.consumeLatestMessage(topicName)
                    .flatMap(response -> {
                        log.info("consumeLatestMessage() : Latest message consumed successfully from topic '{}'", topicName);
                        return Mono.just(ResponseEntity.ok(response));
                    });
        } catch (Exception ex) {
            log.error("consumeLatestMessage() : Error consuming latest message from topic '{}': {}", topicName, ex.getMessage(), ex);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.builder().status("Error consuming latest message: " + ex.getMessage()).build()));
        }
    }

    @GetMapping(value = AppConstants.KAFKA_CONSUME_MESSAGE_WITH_OPTIONS_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> consumeMessages(@RequestParam(value = "topicName") String topicName,
                                                          @RequestParam(value = "format", defaultValue = "plain") String format,
                                                          @RequestParam(value = "maxMessages", defaultValue = "1") int maxMessages,
                                                          @RequestParam(value = "fromBeginning", defaultValue = "false") boolean fromBeginning,
                                                          @RequestParam(value = "partition", required = false) Integer partition,
                                                          @RequestParam(value = "group", required = false) String group,
                                                          @RequestParam(value = "timeoutMs", defaultValue = "10000") long timeoutMs) {
        Response finalResponse = Response.builder().build();

        // Log the request details for debugging purposes
        log.info("Consuming messages from Kafka topic '{}' with options: format={}, maxMessages={}, fromBeginning={}, partition={}, group={}, timeoutMs={}",
                topicName, format, maxMessages, fromBeginning, partition, group, timeoutMs);

        // Delegate Kafka consumption logic to the service
        return kafkaConsumerService.consumeMessagesWithOptions(topicName, format, maxMessages, fromBeginning, partition, group, timeoutMs)
                .map(response -> {
                    // Log success finalResponse
                    log.info("Kafka consumer started successfully for topic '{}'. Status: {}", topicName, response.getStatus());

                    // If the consumer started successfully, return 200 OK
                    if (response.getStatus().startsWith("Kafka consumer started")) {
                        return ResponseEntity.ok(response);
                    } else {
                        // If the consumer failed, return 500 Internal Server Error
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                })
                .onErrorResume(e -> {
                    // Handle unexpected errors
                    log.error("Error while consuming messages from Kafka topic '{}': {}", topicName, e.getMessage(), e);
                    finalResponse.setErrorMessage("Error consuming messages: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(finalResponse));
                });
    }

}