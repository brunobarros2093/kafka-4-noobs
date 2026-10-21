package com.github.brunobarros2093.demos;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProducerDemoTest {

    @Test
    void createProperties_comBootstrapInformado_configuraBootstrapESerializersDeString() {
        Properties properties = ProducerDemo.createProperties("x:1");

        assertEquals("x:1", properties.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class.getName(), properties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(StringSerializer.class.getName(), properties.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals(3, properties.size());
    }

    @Test
    void send_comProdutorAutoComplete_gravaUmRegistroComTopicoChaveEValor() {
        MockProducer<String, String> producer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());

        Future<RecordMetadata> future = ProducerDemo.send(producer, "demo_java", "k", "v");

        List<ProducerRecord<String, String>> history = producer.history();
        assertEquals(1, history.size());
        ProducerRecord<String, String> record = history.get(0);
        assertEquals("demo_java", record.topic());
        assertEquals("k", record.key());
        assertEquals("v", record.value());
        assertTrue(future.isDone());
    }

    @Test
    void send_comChaveVazia_mantemChaveVaziaNoRegistro() throws Exception {
        MockProducer<String, String> producer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());

        RecordMetadata metadata = ProducerDemo.send(producer, "demo_java", "", "hello world").get();

        ProducerRecord<String, String> record = producer.history().get(0);
        assertEquals("", record.key());
        assertEquals("hello world", record.value());
        assertEquals("demo_java", metadata.topic());
    }

    @Test
    void send_quandoBrokerFalha_futureLancaExecutionException() {
        MockProducer<String, String> producer = new MockProducer<>(false, new StringSerializer(), new StringSerializer());
        RuntimeException falha = new RuntimeException("broker indisponível");

        Future<RecordMetadata> future = ProducerDemo.send(producer, "demo_java", "k", "v");
        assertFalse(future.isDone());
        assertTrue(producer.errorNext(falha));

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals("broker indisponível", exception.getCause().getMessage());
    }
}
