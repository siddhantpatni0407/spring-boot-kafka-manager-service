# 📌 spring-boot-kafka-manager-service
Spring Boot Kafka Manager Service

# Table of Contents

1. [Introduction](#introduction)
2. [Description](#description)
3. [Features](#features)
   [Usage](#usage)
5. [Key Concepts](#key-concepts)
    - [Kafka Cluster](#kafka-cluster)
    - [Kafka Broker](#kafka-broker)
    - [Kafka Producer](#kafka-producer)
    - [Kafka Consumer](#kafka-consumer)
    - [Kafka Topics](#kafka-topics)
    - [Partitions](#partitions)
    - [Offsets](#offsets)
    - [Consumer Groups](#consumer-groups)
    - [Serialization](#serialization)
6. [Demo Application](#demo-application)
7. [Diagrams](#diagrams)
    - [Kafka - Sequence Diagram](#kafka---sequence-diagram)
    - [Kafka - Sequence Diagram - Error Handling Implementation](#kafka---sequence-diagram---error-handling-implementation)
    - [Kafka - Flow Diagram - 1](#kafka---flow-diagram---1)
    - [Kafka - Flow Diagram - 2](#kafka---flow-diagram---2)
    - [Kafka - Flow Diagram - 3](#kafka---flow-diagram---3)
    - [Kafka - Flow Diagram - 4](#kafka---flow-diagram---4)
    - [Kafka - Block Diagram](#kafka---block-diagram)
8. [Apache Kafka Commands](#apache-kafka-commands)
    - [Prerequisites](#prerequisites)
    - [Steps - For Open Source Kafka Server](#steps---for-open-source-kafka-server)
    - [API Details](#api-details)
        - [1. Setup Kafka Server](#1-setup-kafka-server)
        - [2. Kafka Health Check](#2-kafka-health-check)
        - [3. Start Server](#2-start-server)
        - [4. Stop Server](#3-stop-server)
        - [5. Create Topic](#4-create-topic)
        - [6. Get All Topics](#5-get-all-topics)
        - [7. Get Topic Details](#6-get-topic-details)
        - [8. Delete Topic](#7-delete-topic)
        - [9. Delete Kafka Logs](#8-delete-kafka-logs)
        - [10. Publish Message](#9-publish-message)
        - [11. Send Events](#10-send-events)
        - [12. Publish Events (Error Handling)](#11-publish-events-error-handling)
    - [API Summary](#api-summary)
        - [Overview](#overview)
        - [API Endpoints](#api-endpoints)
9. [Front End App Details](#Front-End-App-Details)
10. [Postman Collection](#Postman-Collection)

## Introduction

Spring Boot application for interacting with Apache Kafka.

## Description

This service enables communication with Apache Kafka through RESTful APIs.

## Features

- Sequence and flow diagrams illustrating Kafka message flow.
- Apache Kafka command usage for various operations.
- RESTful APIs for managing Kafka topics and messages.
- Detailed descriptions and examples for each API endpoint.

## Kafka Auto Setup

This service can **automatically set up Kafka** based on system properties.

## Usage

Users can interact with the service via its RESTful API endpoints to manage Kafka topics and messages effectively.

For detailed usage instructions and examples, refer to the API documentation in the respective service modules.

## Key Concepts

Apache Kafka is a distributed, fault-tolerant, and highly scalable message broker and stream processing platform. It was
originally developed by LinkedIn and later open-sourced as an Apache Software Foundation project. Kafka is designed to
handle large volumes of data streams in a real-time and fault-tolerant manner.

Key Components of Kafka:
Producer: An application that publishes messages to Kafka topics.

- **Consumer**: An application that subscribes to topics and processes the published messages.
- **Broker**: Kafka brokers form the core of the Kafka cluster, storing and managing the streams of records.
- **Topic**: A category or feed name to which records are published. Topics in Kafka are used to categorize messages.
- **Partition**: Topics are divided into partitions, allowing Kafka to parallelize processing and scale horizontally.
- **Zookeeper**: Kafka relies on Apache ZooKeeper for distributed coordination and management of the Kafka cluster.
  ![Kafka topics](img/kafka_overview.png)

### Kafka Cluster

A Kafka cluster is a group of one or more Kafka brokers (servers) working together to manage the storage and exchange of
data in the form of messages. Kafka is designed to operate as a distributed system, and a cluster allows it to scale
horizontally, provide fault tolerance, and handle large volumes of data across multiple nodes.
![Kafka topics](img/kafka_cluster.png)

### Kafka Broker

Kafka brokers form the core of the Kafka cluster, storing and managing the streams of records.
![kafka_broker.png](src%2Fmain%2Fresources%2Fartifacts%2Fkafka_broker.png)

### Kafka Producer

Producers are applications or systems that publish messages to Kafka topics. Producers determine which partition within
a topic a message should be sent to.
![kafka_producer.png](src%2Fmain%2Fresources%2Fartifacts%2Fkafka_producer.png)

### Kafka Consumer

Consumers are applications or systems that subscribe to Kafka topics and process the messages. Consumers can be part of
a consumer group, allowing them to parallelize the processing of messages.
![kafka_consumer.png](src%2Fmain%2Fresources%2Fartifacts%2Fkafka_consumer.png)

### Kafka Topics

A Kafka topic is a category or feed name to which records (messages) are published. Topics help organize and categorize
messages.
![kafka_topic.png](src%2Fmain%2Fresources%2Fartifacts%2Fkafka_topic.png)
---

### Partitions

Topics are divided into partitions, allowing for parallel processing and scalability. Each partition is assigned to a
specific broker.
![kafka_partitions.png](src%2Fmain%2Fresources%2Fartifacts%2Fkafka_partitions.png)
---

### Offsets

Offsets represent the position of a message within a partition. Consumers use offsets to track their progress and resume
processing from the last committed offset.
![kafka_offset.png](src%2Fmain%2Fresources%2Fartifacts%2Fkafka_offset.png)
---

### Consumer Groups

Consumer groups allow for parallel and scalable message processing. Consumers within a group coordinate to consume
messages from one or more partitions.
![kafka_consumer_groups.png](src%2Fmain%2Fresources%2Fartifacts%2Fkafka_consumer_groups.png)
---

### Serialization

Serialization is the process of converting data to a format suitable for transmission. Kafka allows custom serializers
for key and value objects.

## Demo Application

![kafka_demo_application.png](src%2Fmain%2Fresources%2Fartifacts%2Fkafka_demo_application.png)

## Diagrams

### Kafka - Sequence Diagram

```mermaid
sequenceDiagram
    participant Producer
    participant Broker
    participant Topic
    participant Partition
    participant Consumer
    Producer ->> Broker: Connect to Broker
    Broker ->> Producer: Acknowledge Connection

    loop Produce Messages
        Producer ->> Topic: Send Message
        Topic ->> Partition: Store Message
    end

    Broker ->> Consumer: Connect to Consumer
    Consumer ->> Broker: Subscribe to Topic

    loop Consume Messages
        Broker ->> Partition: Fetch Message
        Partition ->> Consumer: Send Message
        Consumer ->> Partition: Acknowledge Message
    end   
```

### Kafka - Sequence Diagram - Error Handling Implementation

```mermaid
sequenceDiagram
    participant Producer
    participant RetryableTopic
    participant Consumer
    participant DltHandler
    Producer ->> RetryableTopic: Send message
    alt Message processed successfully
        RetryableTopic ->> Consumer: Deliver message
        Consumer ->> RetryableTopic: Acknowledge
    else Processing fails, max retries not reached
        RetryableTopic ->> Consumer: Retry message
        Note over Consumer: Process message again
    else Processing fails, max retries reached
        RetryableTopic ->> DltHandler: Send to DLT
        DltHandler ->> RetryableTopic: Handle failure
        Note right of DltHandler: Log or take action
    end
```

### Kafka - Flow Diagram - 1

```mermaid
flowchart TD
    subgraph Kafka Cluster
        Zookeeper -- Manages --> KafkaBrokers
        KafkaBrokers -- Stores --> Topics
        KafkaBrokers -- Replicates --> KafkaBrokers2
    end
    KafkaProducers -->|Produce| KafkaBrokers
    KafkaBrokers -->|Consume| KafkaConsumers

```

### Kafka - Flow Diagram - 2

```mermaid
flowchart LR
    subgraph Cluster1[Cluster 1]
        BrokerA1[Broker 1]
        BrokerA2[Broker 2]
        BrokerA3[Broker 3]
    end

    subgraph Cluster2[Cluster 2]
        BrokerB1[Broker 1]
        BrokerB2[Broker 2]
        BrokerB3[Broker 3]
    end

    Producers[Producers] -->|Publish messages| Topics
    Consumers[Consumers] -->|Subscribe to Topics| ConsumerGroups[Consumer Groups]
    Topics -->|Stored in| BrokerA1 & BrokerA2 & BrokerA3
    Topics -->|Stored in| BrokerB1 & BrokerB2 & BrokerB3
    BrokerA1 & BrokerA2 & BrokerA3 .->|Replicate data| Sync[Sync replication]
    BrokerB1 & BrokerB2 & BrokerB3 .->|Replicate data| Sync
    Zookeeper[Zookeeper] -->|Manage Brokers| Cluster1 & Cluster2
    SchemaRegistry[Schema Registry] -->|Manage Schema| Topics
    Connect[Kafka Connect] -->|Import/Export Data| ExternalSystems[External Systems]
    StreamProcessing[Kafka Streams] -->|Process & Transform| Topics
```

### Kafka - Flow Diagram - 3

```mermaid
flowchart TB
    subgraph Client
        Producers[Producers] -->|publish| KafkaCluster[Kafka Cluster]
        Consumers[Consumers] -->|subscribe| KafkaCluster[Kafka Cluster]
    end

    subgraph KafkaCluster[Kafka Cluster]
        Broker1[Broker 1] -.->|replicate| Broker2[Broker 2]
        Broker2[Broker 2] -.->|replicate| Broker3[Broker 3]
        Broker3[Broker 3] -.->|replicate| Broker1[Broker 1]
        Broker1 -->|fetch| ConsumerGroup1[Consumer Group 1]
        Broker2 -->|fetch| ConsumerGroup2[Consumer Group 2]
        Broker3 -->|fetch| ConsumerGroup1
        Zookeeper[Zookeeper] -->|manage| Broker1
        Zookeeper -->|manage| Broker2
        Zookeeper -->|manage| Broker3
    end

    subgraph Topics
        Topic1[Topic 1] --> Partition1[Partition 1]
        Topic1 --> Partition2[Partition 2]
        Partition1 -.-> Broker1
        Partition2 -.-> Broker2
    end

    KafkaCluster --> Topics
```

### Kafka - Flow Diagram - 4

```mermaid
flowchart TD
    subgraph Cluster [Kafka Cluster]
        direction TB
        subgraph BrokerA [Broker 1]
            TopicA1[Topic A Partition 1]
            TopicA2[Topic A Partition 2]
            TopicB1[Topic B Partition 1]
        end
        subgraph BrokerB [Broker 2]
            TopicA3[Topic A Partition 3]
            TopicB2[Topic B Partition 2]
            TopicC1[Topic C Partition 1]
        end
        subgraph BrokerC [Broker 3]
            TopicB3[Topic B Partition 3]
            TopicC2[Topic C Partition 2]
            TopicC3[Topic C Partition 3]
        end
    end

    ProducerA[Producer A] -->|Publish| TopicA1
    ProducerA -->|Publish| TopicA3
    ProducerB[Producer B] -->|Publish| TopicB1
    ProducerB -->|Publish| TopicB3
    ConsumerGroupA[Consumer Group A] -->|Subscribe| TopicA1
    ConsumerGroupA -->|Subscribe| TopicA3
    ConsumerGroupB[Consumer Group B] -->|Subscribe| TopicB1
    ConsumerGroupB -->|Subscribe| TopicB2
    ConsumerGroupB -->|Subscribe| TopicB3
    Zookeeper[Zookeeper] -->|Manages| BrokerA
    Zookeeper -->|Manages| BrokerB
    Zookeeper -->|Manages| BrokerC
```

### Kafka - Block Diagram

```mermaid
graph TD
    A[Apache Kafka] --> B[Producer API]
    A --> C[Consumer API]
    A --> D[Streams API]
    A --> E[Connector API]
    B --> F[Topic]
    C --> F
    D --> F
    E --> H[External Systems]
    F --> G[Broker]
    G --> I[Partition]
    G --> J[Replica]
    I --> J
```

# Apache Kafka Commands

This README provides a guide on how to use Apache Kafka with various commands.

## Prerequisites

Before running Kafka commands, make sure you have Kafka installed on your system. If not, download and install it from
the [official website](https://kafka.apache.org/downloads).

## Steps - For Open Source Kafka Server

1. **Go to Kafka Installation Directory**: Navigate to the Kafka server installation directory on your system. For
   example:
    ```bash
    cd C:\Softwares\Kafka\kafka\bin\windows
    ```

2. **Start Zookeeper Server**: Open a command prompt and run the following command to start the Zookeeper server:
    ```bash
    ./zookeeper-server-start.bat ../../config/zookeeper.properties
    ```

3. **Start Kafka Server**: Open another command prompt and run the following command to start the Kafka server:
    ```bash
    ./kafka-server-start.bat ../../config/server.properties
    ```

4. **Create a Topic**: Use the following command to create a topic named `my-topic`:
    ```bash
    ./kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic my-topic
    ```

5. **List All Topics**: Run the following command to list all topics available in your Kafka cluster:
    ```bash
    ./kafka-topics.bat --list --bootstrap-server localhost:9092
    ```

6. **Delete a Topic**: To delete a topic, replace `<topic_name>` with the name of the topic you want to delete and run
   the following command:
    ```bash
    ./kafka-topics.bat --delete --topic <topic_name> --bootstrap-server localhost:9092
    ```

7. **Start a Producer**: Use the following command to start a Kafka producer for the `my-topic` topic:
    ```bash
    ./kafka-console-producer.bat --broker-list localhost:9092 --topic my-topic
    ```

8. **Start a Consumer**: Run the following command to start a Kafka consumer for the `my-topic` topic, consuming
   messages from the beginning:
    ```bash
    ./kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic my-topic --from-beginning
    ```

9. **Stop Kafka Server**: To stop the Kafka server, run the following command:
    ```bash
    ./kafka-server-stop.bat
    ```

10. **Stop Zookeeper Server**: Finally, stop the Zookeeper server by running the following command:
    ```bash
    ./zookeeper-server-stop.bat
    ```

## API Details

### 1. Start Server

- **URL** - http://localhost:8081/api/v1/kafka-manager-service/kafka/start-server
- **HTTP Method** - POST
- **Description:** Start the Kafka server.

- **Response**

````
{
    "status": "Zookeeper and Kafka servers started successfully."
}
````

### 2. Stop Server

- **URL** - http://localhost:8081/api/v1/kafka-manager-service/kafka/stop-server
- **HTTP Method** - POST
- **Description:** Stop the Kafka server.

- **Response**

````
{
    "status": "Zookeeper and Kafka servers stopped successfully."
}
````

### 3. Create Topic

- **URL** - http://localhost:8081/api/v1/kafka-manager-service/kafka/create-topic?topicName={topic-name}&partition={partition}
- **HTTP Method** - POST
- **Description:** Creates a new Kafka topic with the specified name and partition.
- **Request Parameters:**
    - `topicName`: Name of the Kafka topic to create.
    - `partition`: Number of partitions for the new topic.

- **Response**

````
{
    "status": "Topic -[topic-name]  created successfully"
}
````

### 4. Get All Topics

- **URL** - http://localhost:8081/api/v1/kafka-manager-service/kafka/topic
- **HTTP Method** - GET
- **Description:**  Retrieves a list of all existing Kafka topics.

- **Response**

````
[
  "topic1",
  "topic2",
  "topic3"
]
````

### 5. Get Topic Details

- **URL** - http://localhost:8081/api/v1/kafka-manager-service/kafka/topic/details?topicName={topic-name}
- **HTTP Method** - GET
- **Description:**  Retrieves a list of all existing Kafka topics.
- **Request Parameters:**
    - `topicName`: Name of the Kafka topic to create.

- **Response**

````
{
    "topicName": "siya",
    "partitionCount": 4,
    "totalMessages": 5,
    "totalLag": 0
}
````

### 6. Delete Topic

- **URL** - http://localhost:8081/api/v1/kafka-manager-service/kafka/topic?topicName={topic-name}
- **HTTP Method** - DELETE
- **Description:**  Deletes the specified Kafka topic.
- **Request Parameters:**
    - `topicName`: Name of the Kafka topic to create.

- **Response**

````
{
  "status": "Topic deleted successfully",
  "errorMessage": null
}
````

### 7. Delete Kafka Logs

- **URL** - http://localhost:8081/api/v1/kafka-manager-service/kafka/logs
- **HTTP Method** - DELETE
- **Description:**  Deletes the kafka logs.

- **Response**

````
{
    "status": "Kafka and Zookeeper logs deleted successfully"
}
````

### 8. Publish Message

- **URL** - http://localhost:8082/api/v1/kafka-manager-service/publish
- **HTTP Method** - GET
- **Description:**  Publishes a message to a Kafka topic.

- **Response**

````
{
  "status": "Message published successfully",
  "errorMessage": null
}
````

### 9. Send Events

- **URL** - http://localhost:8082/api/v1/kafka-manager-service/publish
- **HTTP Method** - POST
- **Description:**  Publishes events to a Kafka topic.
- **Request Body:** Customer object containing event details.

- **Request**

````
{
  "id": 123,
  "name": "Siddhant Patni",
  "email": "siddhant4patni@gmail.com"
}
````

- **Response**

````
{
  "status": "Events published successfully",
  "errorMessage": null
}
````

### 10. Publish Events (Error Handling)

- **URL** - http://localhost:8084/api/v1/kafka-manager-service/error-handling/producer
- **HTTP Method** - POST
- **Description:**  Publishes events to a Kafka topic for Error handling.

- **Response**

````
{
    "status": "Message published successfully"
}
````

## API Summary

### Overview

This document outlines the API endpoints provided by the Kafka Service for managing Kafka topics, publishing messages,
and handling errors.

### API Endpoints

### 1. Setup Kafka Server

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/setup
- **Method:** POST
- **Description:** The **Kafka Setup Endpoint** in this Spring Boot service allows users to dynamically set up Kafka. Users can choose between automatic setup or specifying a custom directory for Kafka installation.

### **Request Parameters**

| Parameter                    | Type    | Required | Description                                                                                   |
|------------------------------|---------|----------|-----------------------------------------------------------------------------------------------|
| kafkaAutoSetupRequired       | boolean | Yes      | Enables or disables automatic Kafka setup.                                                    |
| kafkaUserDefinedPathRequired | boolean | Yes      | Specifies if a custom Kafka installation path is required.                                    |
| kafkaUserDefinedPath         | String  | No       | Custom path where Kafka should be installed. Required if `kafkaUserDefinedPathRequired=true`. |

### **Example Request**

#### **1. Automatic Kafka Setup**

```
POST http://localhost:8081/api/v1/kafka-manager-service/kafka/setup?kafkaAutoSetupRequired=true&kafkaUserDefinedPathRequired=false
```

#### **2. User Defined Kafka Path Setup**

```
POST http://localhost:8081/api/v1/kafka-manager-service/kafka/setup?kafkaAutoSetupRequired=true&kafkaUserDefinedPathRequired=true&kafkaUserDefinedPath=C:\Users\Kafka\Setup
```

## Response Structure

### **Success Response**

```json
{
  "status": "Kafka setup completed successfully."
}
```

### **Setup Skipped Response**

```json
{
  "status": "Kafka setup skipped."
}
```

### **Error Response**

```json
{
  "errorMessage": "An unexpected error occurred during Kafka setup."
}
```

### 2. Kafka Health Check

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/health
- **Method:** GET
- **Description:** Kafka Server Health check.
- **Response:**
    - Status: ✅ Kafka is UP! Message Hi sent with offset 0

### 3. Start Server

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/start-server
- **Method:** POST
- **Description:** Starts the Kafka server.
- **Response:**
    - Status: Zookeeper and Kafka servers started successfully.

### 4. Stop Server

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/stop-server
- **Method:** POST
- **Description:** Stops the Kafka server.
- **Response:**
    - Status: Zookeeper and Kafka servers stopped successfully.

### 5. Create Topic

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/topic
- **Method:** POST
- **Description:** Creates a new Kafka topic.
- **Request Parameters:**
    - topicName: Name of the Kafka topic.
    - partition: Number of partitions for the new topic.
- **Response:**
    - Status: Topic created successfully.

### 6. Get All Topics

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/topic
- **Method:** GET
- **Description:** Retrieves a list of all existing Kafka topics.
- **Response:**
    - List of topic names.

### 7. Get Topic Details

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/topic/details?topicName={topic-name}
- **Method:** GET
- **Description:** Retrieves details of a specific Kafka topic.
- **Request Parameters:**
    - topicName: Name of the Kafka topic.
- **Response:**
    - Details of the specified Kafka topic.

### 8. Delete Topic

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/topic?topicName={topic-name}
- **Method:** DELETE
- **Description:** Deletes a Kafka topic.
- **Request Parameters:**
    - topicName: Name of the Kafka topic.
- **Response:**
    - Status: Topic deleted successfully.

### 9. Delete Kafka Logs

- **URL:** http://localhost:8081/api/v1/kafka-manager-service/kafka/logs
- **Method:** DELETE
- **Description:** Deletes Kafka logs.
- **Response:**
    - Status: Kafka and Zookeeper logs deleted successfully.

### 10. Publish Message

- **URL:** http://localhost:8082/api/v1/kafka-manager-service/publish
- **Method:** GET
- **Description:** Publishes a message to a Kafka topic.
- **Response:**
    - Status: Message published successfully.

### 11. Send Events

- **URL:** http://localhost:8082/api/v1/kafka-manager-service/publish
- **Method:** POST
- **Description:** Publishes events to a Kafka topic.
- **Request Body:** Customer object containing event details.
- **Response:**
    - Status: Events published successfully.

### 12. Publish Events (Error Handling)

- **URL:** http://localhost:8084/api/v1/kafka-manager-service/error-handling/producer
- **Method:** POST
- **Description:** Publishes events to a Kafka topic for error handling.
- **Response:**
    - Status: Message published successfully.

### Front End App Details

- App Name - react-kafka-manager-ui
- Platform - ReactJS
- repo URL - https://github.com/siddhantpatni0407/react-kafka-manager-ui.git
- URL - http://localhost:3000

## Postman Collection

```
https://github.com/siddhantpatni0407/spring-boot-kafka-manager-service/tree/master/postman_collection/collection/spring-boot-kafka-manager-service.postman_collection.json
```