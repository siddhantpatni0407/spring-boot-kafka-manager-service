package com.sid.app.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Siddhant Patni
 */
@Component
@Data
public class AppProperties {

    @Value("${spring.kafka.topic}")
    private String kafkaTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.json.trusted.packages}")
    private String jsonTrustedPackage;

    @Value("${spring.kafka.installation-directory}")
    private String kafkaInstallationDirectory;

    @Value("${spring.kafka.zookeeper-config-path}")
    private String zookeeperConfigPath;

    @Value("${spring.kafka.kafka-config-path}")
    private String kafkaConfigPath;

    @Value("${spring.kafka.logs.kafka-logs-path}")
    private String kafkaLogsPath;

    @Value("${spring.kafka.logs.zookeeper-logs-path}")
    private String zookeeperLogsPath;

    @Value("${spring.kafka.setup.kafka-auto-setup-required}")
    private boolean kafkaAutoSetupRequired;

    @Value("${spring.kafka.setup.kafka-user-defined-path-required}")
    private boolean kafkaUserDefinedPathRequired;

    @Value("${spring.kafka.setup.kafka-user-defined-path}")
    private String kafkaUserDefinedPath;

}