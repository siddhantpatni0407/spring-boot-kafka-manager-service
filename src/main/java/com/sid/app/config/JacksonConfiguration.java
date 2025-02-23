package com.sid.app.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sid.app.utils.TopicPartitionInfoSerializer;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Siddhant Patni
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public SimpleModule customSerializerModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(TopicPartitionInfo.class, new TopicPartitionInfoSerializer());
        return module;
    }

}