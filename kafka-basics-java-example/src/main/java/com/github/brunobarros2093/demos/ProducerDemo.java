package com.github.brunobarros2093.demos;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.Future;


public class ProducerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ProducerDemo.class.getName());

    public static void main(String[] args) {
        logger.info("ProducerDemo");
        // properties
        Properties properties = createProperties("localhost:9092");
        // producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        // send data
        send(producer, "demo_java", "", "hello world");
        // flush: espera as mensagens pendentes serem enviadas de fato ao broker
        producer.flush();
        // close: libera conexões e recursos do produtor
        producer.close();
    }

    /**
     * Monta as configurações mínimas de um produtor:
     * onde está o Kafka (bootstrap.servers) e como transformar chave e valor em bytes (serializers).
     */
    static Properties createProperties(String bootstrapServers) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("value.serializer", StringSerializer.class.getName());
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        return properties;
    }

    /**
     * Cria o record (a mensagem) e envia para o tópico.
     * O envio é assíncrono: o método devolve um Future que completa quando o broker confirma.
     */
    static Future<RecordMetadata> send(Producer<String, String> producer, String topic, String key, String value) {
        // create Record
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        return producer.send(record);
    }
}
