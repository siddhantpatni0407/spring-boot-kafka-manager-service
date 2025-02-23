package com.sid.app.service;

import com.sid.app.config.AppProperties;
import com.sid.app.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Siddhant Patni
 * Service for consuming Kafka messages with various options.
 */
@Service
@Slf4j
public class KafkaConsumerService {

    @Autowired
    private AppProperties appProperties;

    /**
     * Consumes messages from the specified Kafka topic.
     *
     * @param topicName the name of the Kafka topic
     * @return a Mono containing the response status
     */
    public Mono<Response> consumeMessages(String topicName) {
        return executeKafkaConsumerCommand(topicName, "--from-beginning");
    }

    /**
     * Consumes the latest message from the specified Kafka topic.
     *
     * @param topicName the name of the Kafka topic
     * @return a Mono containing the response status
     */
    public Mono<Response> consumeLatestMessage(String topicName) {
        return executeKafkaConsumerCommand(topicName, "--max-messages 1");
    }

    /**
     * Consumes messages from the specified Kafka topic with additional options.
     *
     * @param topicName     the name of the Kafka topic
     * @param format        the message format (e.g., "plain" or "json")
     * @param maxMessages   the maximum number of messages to consume
     * @param fromBeginning whether to consume from the beginning
     * @param partition     the partition number (optional)
     * @param group         the consumer group (optional)
     * @param timeoutMs     the timeout in milliseconds
     * @return a Mono containing the response status
     */
    public Mono<Response> consumeMessagesWithOptions(String topicName, String format, int maxMessages,
                                                     boolean fromBeginning, Integer partition, String group,
                                                     long timeoutMs) {
        List<String> commandArgs = new ArrayList<>();

        if ("json".equalsIgnoreCase(format)) {
            commandArgs.add("--property");
            commandArgs.add("print.value=true");
        } else {
            commandArgs.add("--max-messages");
            commandArgs.add(String.valueOf(maxMessages));
        }

        if (fromBeginning) {
            commandArgs.add("--from-beginning");
        }

        if (partition != null) {
            commandArgs.add("--partition");
            commandArgs.add(String.valueOf(partition));
        }

        if (group != null && !group.isEmpty()) {
            commandArgs.add("--group");
            commandArgs.add(group);
        }

        commandArgs.add("--timeout-ms");
        commandArgs.add(String.valueOf(timeoutMs));

        return executeKafkaConsumerCommand(topicName, String.join(" ", commandArgs));
    }

    /**
     * Executes the Kafka consumer command with the specified options.
     *
     * @param topicName         the name of the Kafka topic
     * @param additionalOptions additional command-line options
     * @return a Mono containing the response status
     */
    private Mono<Response> executeKafkaConsumerCommand(String topicName, String additionalOptions) {
        Response response = Response.builder().build();
        List<String> command = buildKafkaConsumerCommand(topicName, additionalOptions);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Capture output to log errors properly
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.info(line);
            }

            int exitCode = process.waitFor(); // Ensure process completes

            if (exitCode == 0) {
                log.info("Kafka consumer started successfully for topic '{}'", topicName);
                response.setStatus("Kafka consumer started successfully for topic: " + topicName);
            } else {
                log.error("Kafka consumer failed with exit code {}", exitCode);
                response.setErrorMessage("Kafka consumer failed. Output: " + output);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to start consumer for topic '{}' due to {}", topicName, e.getMessage(), e);
            response.setErrorMessage("Unable to start consumer: " + e.getMessage());
        }

        return Mono.just(response);
    }

    /**
     * Builds the Kafka consumer command with the specified options.
     *
     * @param topicName         the name of the Kafka topic
     * @param additionalOptions additional command-line options
     * @return the complete Kafka consumer command as a List<String>
     */
    private List<String> buildKafkaConsumerCommand(String topicName, String additionalOptions) {
        List<String> command = new ArrayList<>();
        command.add("cmd.exe");
        command.add("/c");
        command.add("start");
        command.add("cmd.exe");
        command.add("/k");
        command.add("call");
        command.add(appProperties.getKafkaInstallationDirectory() + "\\kafka-console-consumer.bat");
        command.add("--bootstrap-server");
        command.add(appProperties.getBootstrapServers());
        command.add("--topic");
        command.add(topicName);

        // Add additional options (already formatted)
        if (!additionalOptions.isEmpty()) {
            String[] options = additionalOptions.split(" ");
            for (String opt : options) {
                command.add(opt);
            }
        }

        return command;
    }

}