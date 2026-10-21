package com.github.brunobarros2093.demos;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProducerDemoWithCallbackTest {

    private static MockProducer<String, String> mockProducer(boolean autoComplete) {
        return new MockProducer<>(autoComplete, new StringSerializer(), new StringSerializer());
    }

    private static void assertRecord(ProducerRecord<String, String> record, String key, String value) {
        assertEquals("demo_java", record.topic());
        assertEquals(key, record.key());
        assertEquals(value, record.value());
    }

    @Test
    void createProperties_comBootstrapInformado_usaConstantesDoProducerConfig() {
        Properties properties = ProducerDemoWithCallback.createProperties("127.0.0.1:9092");

        assertEquals("127.0.0.1:9092", properties.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class.getName(), properties.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(StringSerializer.class.getName(), properties.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals(3, properties.size());
    }

    @Test
    void sendMessages_comDoisLotesDeDez_enviaVinteRegistrosComChavesEValoresEsperados() {
        MockProducer<String, String> producer = mockProducer(true);

        ProducerDemoWithCallback.sendMessages(producer, "demo_java", 2, 10);

        List<ProducerRecord<String, String>> history = producer.history();
        assertEquals(20, history.size());
        assertRecord(history.get(0), "id_0", "Hello World_0");
        assertRecord(history.get(9), "id_9", "Hello World_9");
        assertRecord(history.get(10), "id_0", "Hello World_0");
        assertRecord(history.get(19), "id_9", "Hello World_9");
        assertTrue(history.stream().allMatch(r -> "demo_java".equals(r.topic())));
    }

    @Test
    void sendMessages_comCadaChaveRepetidaNosLotes_enviaMesmaChaveUmaVezPorLote() {
        MockProducer<String, String> producer = mockProducer(true);

        ProducerDemoWithCallback.sendMessages(producer, "demo_java", 3, 4);

        List<ProducerRecord<String, String>> history = producer.history();
        assertEquals(12, history.size());
        for (int i = 0; i < 4; i++) {
            String key = "id_" + i;
            long count = history.stream().filter(r -> key.equals(r.key())).count();
            assertEquals(3, count, "quantidade de mensagens com a chave " + key);
        }
    }

    @Test
    void sendMessages_comZeroMensagensPorLote_naoEnviaNada() {
        MockProducer<String, String> producer = mockProducer(true);

        ProducerDemoWithCallback.sendMessages(producer, "demo_java", 2, 0);

        assertTrue(producer.history().isEmpty());
    }

    @Test
    void sendMessages_comZeroLotes_naoEnviaNada() {
        MockProducer<String, String> producer = mockProducer(true);

        ProducerDemoWithCallback.sendMessages(producer, "demo_java", 0, 10);

        assertTrue(producer.history().isEmpty());
    }

    @Test
    void sendMessages_quandoEnvioConcluiComSucesso_callbackNaoLancaExcecao() {
        MockProducer<String, String> producer = mockProducer(true);

        // com autoComplete=true o callback roda dentro do send, com recordMetadata preenchido
        assertDoesNotThrow(() -> ProducerDemoWithCallback.sendMessages(producer, "demo_java", 1, 1));

        assertEquals(1, producer.history().size());
    }

    @Test
    void sendMessages_quandoBrokerFalha_callbackApenasLogaENaoRelanca() {
        MockProducer<String, String> producer = mockProducer(false);

        ProducerDemoWithCallback.sendMessages(producer, "demo_java", 1, 1);

        // errorNext completa o envio pendente com erro e dispara o callback
        assertDoesNotThrow(() -> assertTrue(producer.errorNext(new RuntimeException("falha no envio"))));
        assertEquals(1, producer.history().size());
    }
}
