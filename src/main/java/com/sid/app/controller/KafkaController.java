package com.sid.app.controller;

import com.sid.app.constant.AppConstants;
import com.sid.app.model.Response;
import com.sid.app.model.TopicDetails;
import com.sid.app.service.KafkaHealthCheckService;
import com.sid.app.service.KafkaService;
import com.sid.app.service.KafkaSetupService;
import com.sid.app.utils.ApplicationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Siddhant Patni
 */
@RestController
@Slf4j
@CrossOrigin
public class KafkaController {

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private KafkaSetupService kafkaSetupService;

    @Autowired
    private KafkaHealthCheckService kafkaHealthCheckService;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Health check endpoint to verify Kafka connectivity.
     *
     * @return ResponseEntity<Response> indicating Kafka status.
     */
    @GetMapping(value = AppConstants.START_KAFKA_HEALTH_CHECK_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> checkKafkaHealth() {
        log.info("checkKafkaHealth() : Kafka Health Check - START");
        return kafkaHealthCheckService.checkKafkaHealth()
                .flatMap(response -> {
                    log.info("checkKafkaHealth() : Kafka Health Check Response -> {}",
                            ApplicationUtils.getJSONString(response));
                    return Mono.just(ResponseEntity.ok(response));
                })
                .onErrorResume(ex -> {
                    log.error("checkKafkaHealth() : Kafka Health Check Failed", ex);
                    Response errorResponse = new Response();
                    errorResponse.setStatus("Kafka is DOWN. Error -> " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
                });
    }

    /**
     * API to trigger Kafka setup based on request parameters.
     *
     * @param kafkaAutoSetupRequired       Whether Kafka setup is required.
     * @param kafkaUserDefinedPathRequired Whether user-defined path should be used.
     * @param kafkaUserDefinedPath         The user-defined path if applicable.
     * @return Response indicating setup completion.
     */
    @PostMapping(value = AppConstants.START_KAFKA_SETUP_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> setupKafka(
            @RequestParam boolean kafkaAutoSetupRequired,
            @RequestParam boolean kafkaUserDefinedPathRequired,
            @RequestParam(required = false) String kafkaUserDefinedPath) {

        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            Response response = Response.builder().build();

            // ✅ If auto-setup is disabled, return immediately
            if (!kafkaAutoSetupRequired) {
                log.info("setupKafka() : Kafka auto setup is disabled. Skipping setup.");
                response.setStatus("Kafka setup skipped.");
                return ResponseEntity.ok(response);
            }

            try {
                // ✅ Declare a new local final variable
                final String resolvedKafkaPath;

                if (kafkaUserDefinedPathRequired && kafkaUserDefinedPath != null) {
                    resolvedKafkaPath = URLDecoder.decode(kafkaUserDefinedPath, StandardCharsets.UTF_8);
                    log.info("setupKafka() : Decoded Kafka setup path: {}", resolvedKafkaPath);

                    // ✅ Validate path (optional check)
                    if (resolvedKafkaPath.isBlank()) {
                        response.setErrorMessage("Error: Path is required for manual setup.");
                        return ResponseEntity.badRequest().body(response);
                    }
                } else {
                    resolvedKafkaPath = null; // No user-defined path
                }

                // ✅ Run Kafka setup with the final variable
                kafkaSetupService.kafkaSetupRunner(kafkaAutoSetupRequired, kafkaUserDefinedPathRequired, resolvedKafkaPath).run(null);

                log.info("setupKafka() : Kafka setup completed successfully.");
                response.setStatus("Kafka setup completed successfully.");
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                log.error("setupKafka() : ❌ Unexpected error during Kafka setup: {}", e.getMessage(), e);
                response.setErrorMessage("An unexpected error occurred during Kafka setup.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }, executorService));
    }

    @PostMapping(value = AppConstants.START_KAFKA_SERVERS_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> startServers() {
        if (log.isInfoEnabled()) {
            log.info("startServers() : Start Zookeeper and Kafka Server - START");
        }
        return kafkaService.startServers()
                .flatMap(responseResponseEntity -> {
                    if (log.isInfoEnabled()) {
                        log.info("startServers() : Start Zookeeper and Kafka Server API. Response -> {}",
                                ApplicationUtils.getJSONString(responseResponseEntity));
                        log.info("startServers() : Start Zookeeper and Kafka Server - END");
                    }
                    return Mono.just(responseResponseEntity);
                });
    }

    @PostMapping(value = AppConstants.STOP_KAFKA_SERVERS_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> stopKafkaServers() {
        if (log.isInfoEnabled()) {
            log.info("stopKafkaServers() : Stop Zookeeper and Kafka Server - START");
        }
        return kafkaService.stopKafkaServers()
                .flatMap(responseResponseEntity -> {
                    if (log.isInfoEnabled()) {
                        log.info("stopKafkaServers() : Stop Zookeeper and Kafka Server API. Response -> {}",
                                ApplicationUtils.getJSONString(responseResponseEntity));
                        log.info("stopKafkaServers() : Stop Zookeeper and Kafka Server - END");
                    }
                    return Mono.just(responseResponseEntity);
                });
    }

    @PostMapping(value = AppConstants.KAFKA_CREATE_TOPIC_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> createTopic(@RequestParam(value = "topicName") String topicName,
                                                      @RequestParam(value = "partition", required = false) Integer partition) {
        if (log.isInfoEnabled()) {
            log.info("createTopic() : Create Topic - START");
        }
        return kafkaService.createTopic(topicName, partition)
                .flatMap(responseResponseEntity -> {
                    if (log.isInfoEnabled()) {
                        log.info("createTopic() : Create Topic API. Request param => topicName -> {}, partition -> {} and Response -> {}",
                                topicName, partition, ApplicationUtils.getJSONString(responseResponseEntity));
                        log.info("createTopic() : Create Topic - END");
                    }
                    return Mono.just(responseResponseEntity);
                });
    }

    @GetMapping(value = AppConstants.KAFKA_TOPIC_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Collection<String>>> getAllTopics() {
        if (log.isInfoEnabled()) {
            log.info("getAllTopics() : Get All Topics - START");
        }
        return kafkaService.getAllTopics()
                .flatMap(allTopics -> {
                    if (log.isInfoEnabled()) {
                        log.info("getAllTopics() : Get all Topics API. Response -> {}",
                                ApplicationUtils.getJSONString(allTopics));
                        log.info("getAllTopics() : Get All Topics - END");
                    }
                    return Mono.just(allTopics);
                });

    }

    @GetMapping(value = AppConstants.KAFKA_TOPIC_DETAILS_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TopicDetails>> getTopicDetails(@RequestParam(value = "topicName") String topicName) {
        if (log.isInfoEnabled()) {
            log.info("getTopicDetails() : Get Topic Details - START");
        }
        return kafkaService.getTopicDetailsWithOffsets(topicName)
                .map(ResponseEntity::ok)
                .onErrorResume(KafkaException.class, error -> {
                    log.error("getTopicDetails() : KafkaException occurred while fetching topic details", error);
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
                })
                .onErrorResume(Exception.class, error -> {
                    log.error("getTopicDetails() : Exception occurred while fetching topic details", error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new TopicDetails()));
                });
    }

    @DeleteMapping(value = AppConstants.KAFKA_TOPIC_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> deleteTopic(@RequestParam(value = "topicName") String topicName) {
        if (log.isInfoEnabled()) {
            log.info("deleteTopic() : Delete Topic - START");
        }
        return kafkaService.deleteTopic(topicName)
                .flatMap(responseResponseEntity -> {
                    if (log.isInfoEnabled()) {
                        log.info("deleteTopic() : Delete Topic API. Request param => topicName -> {} and Response -> {}",
                                topicName, ApplicationUtils.getJSONString(responseResponseEntity));
                        log.info("deleteTopic() : Delete Topic - END");
                    }
                    return Mono.just(responseResponseEntity);
                });
    }


    @DeleteMapping(value = AppConstants.DELETE_KAFKA_LOGS_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Response>> deleteKafkaLogs() {
        if (log.isInfoEnabled()) {
            log.info("deleteKafkaLogs() : Delete Kafka Logs - START");
        }
        return kafkaService.deleteKafkaLogs()
                .flatMap(responseResponseEntity -> {
                    if (log.isInfoEnabled()) {
                        log.info("deleteKafkaLogs() : Delete Kafka Logs API. Response -> {}",
                                ApplicationUtils.getJSONString(responseResponseEntity));
                        log.info("deleteKafkaLogs() : Delete Kafka Logs - END");
                    }
                    return Mono.just(responseResponseEntity);
                });
    }

}