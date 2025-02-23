package com.sid.app.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.kafka.common.TopicPartitionInfo;

import java.io.IOException;

/**
 * @author Siddhant Patni
 */
public class TopicPartitionInfoSerializer extends JsonSerializer<TopicPartitionInfo> {

    @Override
    public void serialize(TopicPartitionInfo topicPartitionInfo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("partition", String.valueOf(topicPartitionInfo.partition()));
        jsonGenerator.writeObjectField("leader", topicPartitionInfo.leader());
        jsonGenerator.writeObjectField("replicas", topicPartitionInfo.replicas());
        jsonGenerator.writeObjectField("isr", topicPartitionInfo.isr());
        jsonGenerator.writeEndObject();
    }

}