package com.github.brunobarros2093.demos;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;


public class ConsumerDemo {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemo.class.getSimpleName());

    public static void main(String[] args) {

        logger.info("ConsumerDemo");

        // properties
        String bootstrapServers = "127.0.0.1:9092";
        String groupId = "my-java-application";
        Properties properties = createProperties(bootstrapServers, groupId);

        // consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Ao apertar Ctrl+C a JVM roda este hook numa outra thread.
        // wakeup() faz o poll() da thread principal lançar WakeupException,
        // assim o loop termina e o consumer é fechado direitinho (shutdown gracioso).
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Detectado um shutdown, chamando consumer.wakeup()...");
            consumer.wakeup();
            // espera a thread principal terminar de fechar o consumer
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        consume(consumer, "demo_java");
    }

    /**
     * Configurações mínimas de um consumidor.
     * - group.id: nome do consumer group; o Kafka guarda até onde esse grupo já leu (offset).
     * - auto.offset.reset=earliest: se o grupo ainda não leu nada, começa do início do tópico,
     *   então você também recebe as mensagens enviadas antes de o consumidor subir.
     */
    static Properties createProperties(String bootstrapServers, String groupId) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

    /**
     * Texto exibido no log para cada mensagem recebida.
     */
    static String format(ConsumerRecord<String, String> record) {
        return "Key: " + record.key()
                + " | Value: " + record.value()
                + " | Partition: " + record.partition()
                + " | Offset: " + record.offset();
    }

    /**
     * Assina o tópico e fica buscando mensagens até receber um wakeup().
     * Sempre fecha o consumer no final e devolve quantas mensagens foram recebidas.
     */
    static int consume(Consumer<String, String> consumer, String topic) {
        int total = 0;
        try {
            // subscribe: diz ao Kafka qual(is) tópico(s) queremos ler
            consumer.subscribe(List.of(topic));

            while (true) {
                // poll: busca as mensagens disponíveis, esperando até 1 segundo se não houver nenhuma
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    logger.info(format(record));
                    total++;
                }
            }
        } catch (WakeupException e) {
            // esperado: foi o wakeup() chamado no shutdown
            logger.info("Consumer está desligando...");
        } catch (Exception e) {
            logger.error("Erro inesperado no consumer", e);
        } finally {
            // close: faz commit dos offsets e avisa o grupo que este consumidor saiu
            consumer.close();
            logger.info("Consumer foi fechado com sucesso");
        }
        return total;
    }
}
