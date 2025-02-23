package com.sid.app.service;

import com.sid.app.config.AppProperties;
import com.sid.app.constant.AppConstants;
import com.sid.app.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.BrokerNotAvailableException;
import org.apache.kafka.common.errors.NetworkException;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka Health Check Service
 * Ensures Kafka is available by sending a test message.
 */
@Service
@Slf4j
public class KafkaHealthCheckService {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Checks Kafka health by sending a lightweight message.
     *
     * @return a Mono<Response> indicating Kafka health status.
     */
    public Mono<Response> checkKafkaHealth() {
        Response response = new Response();
        String testMessage = AppConstants.HEALTH_CHECK_TEXT_MESSAGE + UUID.randomUUID();

        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(AppConstants.HEALTH_CHECK_TOPIC, testMessage);

            return Mono.fromFuture(future.thenApply(result -> {
                log.info("✅ Kafka is UP! Message [{}] sent with offset [{}]",
                        testMessage, result.getRecordMetadata().offset());
                response.setStatus("✅ Kafka is UP! Offset -> " + result.getRecordMetadata().offset());
                return response;
            })).onErrorResume(ex -> {
                String errorMsg = detectKafkaError(ex);
                log.error("❌ Kafka Health Check Failed - {}", errorMsg);
                response.setStatus(errorMsg);
                return Mono.just(response);
            });
        } catch (Exception ex) {
            String errorMsg = detectKafkaError(ex);
            log.error("❌ Kafka Health Check Exception - {}", errorMsg);
            response.setStatus(errorMsg);
            return Mono.just(response);
        }
    }

    /**
     * Detects and categorizes Kafka-related errors.
     *
     * @param ex the thrown exception
     * @return a meaningful error message
     */
    private String detectKafkaError(Throwable ex) {
        return switch (ex) {
            case TimeoutException timeoutException -> "⏳ Kafka is DOWN (Timeout while waiting for broker response)";
            case BrokerNotAvailableException brokerNotAvailableException -> "❌ Kafka is DOWN (Broker not available)";
            case NetworkException networkException -> "🚫 Kafka is UNREACHABLE (Network failure)";
            case null, default -> {
                assert ex != null;
                yield "⚠️ Kafka Health Check Failed - " + ex.getMessage();
            }
        };
    }

}