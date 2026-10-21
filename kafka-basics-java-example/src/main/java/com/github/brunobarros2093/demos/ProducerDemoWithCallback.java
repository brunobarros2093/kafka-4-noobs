package com.github.brunobarros2093.demos;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class ProducerDemoWithCallback {

    private static final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class.getSimpleName());

    public static void main(String[] args) {

        logger.info("ProducerDemoWithCallback");

        // properties
        String bootstrapServers = "127.0.0.1:9092";
        Properties properties = createProperties(bootstrapServers);
        Thread.currentThread().setContextClassLoader(null);
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        // 2 lotes de 10 mensagens no tópico demo_java
        sendMessages(producer, "demo_java", 2, 10);

        // flush and close producer
        producer.close();
    }

    /**
     * Configurações do produtor usando as constantes de ProducerConfig (evita erro de digitação nas chaves).
     */
    static Properties createProperties(String bootstrapServers) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }

    /**
     * Envia {@code batches} lotes de {@code messagesPerBatch} mensagens.
     * Chave "id_i" e valor "Hello World_i": a mesma chave sempre cai na mesma partição.
     * O callback é chamado quando o broker responde: loga a partição ou o erro.
     */
    static void sendMessages(Producer<String, String> producer, String topic, int batches, int messagesPerBatch) {
        // create Record
        for (int j = 0; j < batches; j++) {
            // send data
            for (int i = 0; i < messagesPerBatch; i++) {
                String key = "id_" + i;
                String value = "Hello World_" + i;
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
                producer.send(record, (recordMetadata, e) -> {
                    if (e == null) {
                        logger.info("Key: " + key + "| Partition: " + recordMetadata.partition());
                    } else {
                        logger.error("Error while producing message ", e);
                    }
                });
            }
        }
    }
}
