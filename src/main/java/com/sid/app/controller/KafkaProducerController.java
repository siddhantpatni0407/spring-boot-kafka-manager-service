package com.sid.app.controller;

import com.sid.app.constant.AppConstants;
import com.sid.app.model.Response;
import com.sid.app.service.KafkaPublisherService;
import com.sid.app.utils.ApplicationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Kafka Producer Controller
 * Handles API requests for publishing messages to Kafka topics.
 * Allows publishing messages dynamically to a specified topic.
 *
 * @author Siddhant Patni
 */
@RestController
@Slf4j
public class KafkaProducerController {

    @Autowired
    private KafkaPublisherService publisher;

    /**
     * Publishes a message to a given Kafka topic.
     *
     * @param topicName The Kafka topic name.
     * @param message   The message to publish.
     * @return ResponseEntity containing success or failure response.
     */
    @GetMapping(value = AppConstants.KAFKA_PUBLISH_MESSAGE_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> publishMessage(@RequestParam(value = "topicName") String topicName,
                                                         @RequestParam(value = "message") String message) {

        log.info("publishMessage() : Publishing message to topic '{}' - START", topicName);

        try {
            return publisher.sendMessageToTopic(topicName, message)
                    .flatMap(response -> {
                        log.info("publishMessage() : Message sent successfully. Topic -> {}, Message -> {}, Response -> {}",
                                topicName, message, ApplicationUtils.getJSONString(response));
                        log.info("publishMessage() : Publishing message - END");
                        return Mono.just(ResponseEntity.ok(response));
                    });
        } catch (Exception ex) {
            log.error("publishMessage() : Error publishing message to topic '{}': {}", topicName, ex.getMessage(), ex);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.builder().status("Error publishing message: " + ex.getMessage()).build()));
        }
    }

}