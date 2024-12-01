package com.github.brunobarros2093.demos;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class ProducerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ProducerDemo.class.getName());

    public static void main(String[] args) {
        logger.info("ProducerDemo");
        // properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        // producer
        properties.setProperty("value.serializer", StringSerializer.class.getName());
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
         // create Record
        ProducerRecord<String, String> record = new ProducerRecord<>("demo_java","","hello world");
        // send data
        producer.send(record);
        // flush and close producer
        producer.close();
    }
}
