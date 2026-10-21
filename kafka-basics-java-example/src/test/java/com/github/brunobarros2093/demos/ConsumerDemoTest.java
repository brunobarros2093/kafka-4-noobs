package com.github.brunobarros2093.demos;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @Timeout protege contra loop infinito caso o wakeup não encerre o consume
@Timeout(10)
class ConsumerDemoTest {

    private static final String TOPIC = "demo_java";

    private final TopicPartition tp = new TopicPartition(TOPIC, 0);

    private MockConsumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        consumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
    }

    /** Simula o broker atribuindo a partição ao consumidor, com o início do log no offset 0. */
    private void assignPartition() {
        consumer.rebalance(List.of(tp));
        consumer.updateBeginningOffsets(Map.of(tp, 0L));
    }

    @Test
    void createProperties_comBootstrapEGrupo_configuraDeserializersGrupoEEarliest() {
        Properties properties = ConsumerDemo.createProperties("b:1", "g");

        assertEquals("b:1", properties.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("g", properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("earliest", properties.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertEquals(StringDeserializer.class.getName(), properties.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(StringDeserializer.class.getName(), properties.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        assertEquals(5, properties.size());
    }

    @Test
    void format_comRegistroCompleto_mostraChaveValorParticaoEOffset() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(TOPIC, 2, 5L, "k", "v");

        assertEquals("Key: k | Value: v | Partition: 2 | Offset: 5", ConsumerDemo.format(record));
    }

    @Test
    void format_comChaveNula_mostraNull() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(TOPIC, 0, 0L, null, "hello world");

        assertEquals("Key: null | Value: hello world | Partition: 0 | Offset: 0", ConsumerDemo.format(record));
    }

    @Test
    void consume_comDuasMensagensEDepoisWakeup_retornaDoisEFechaConsumer() {
        consumer.schedulePollTask(() -> {
            assignPartition();
            consumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 0L, "id_0", "Hello World_0"));
            consumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 1L, "id_1", "Hello World_1"));
        });
        consumer.schedulePollTask(consumer::wakeup);

        int total = ConsumerDemo.consume(consumer, TOPIC);

        assertEquals(2, total);
        assertTrue(consumer.subscription().contains(TOPIC));
        assertTrue(consumer.closed());
    }

    @Test
    void consume_comMensagensEmVariosPolls_somaOTotalDeTodosOsPolls() {
        consumer.schedulePollTask(() -> {
            assignPartition();
            consumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 0L, "id_0", "Hello World_0"));
        });
        consumer.schedulePollTask(() -> {
            consumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 1L, "id_1", "Hello World_1"));
            consumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 2L, "id_2", "Hello World_2"));
        });
        consumer.schedulePollTask(consumer::wakeup);

        int total = ConsumerDemo.consume(consumer, TOPIC);

        assertEquals(3, total);
        assertTrue(consumer.closed());
    }

    @Test
    void consume_semMensagensApenasWakeup_retornaZeroEFechaConsumer() {
        consumer.schedulePollTask(consumer::wakeup);

        int total = ConsumerDemo.consume(consumer, TOPIC);

        assertEquals(0, total);
        assertTrue(consumer.subscription().contains(TOPIC));
        assertTrue(consumer.closed());
    }

    @Test
    void consume_quandoPollLancaKafkaException_naoPropagaEFechaConsumer() {
        consumer.setPollException(new KafkaException("boom"));

        int total = ConsumerDemo.consume(consumer, TOPIC);

        assertEquals(0, total);
        assertTrue(consumer.closed());
    }

    @Test
    void consume_quandoErroInesperadoAposReceberMensagem_retornaOQueJaFoiContadoEFechaConsumer() {
        consumer.schedulePollTask(() -> {
            assignPartition();
            consumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 0L, "id_0", "Hello World_0"));
        });
        consumer.schedulePollTask(() -> {
            throw new IllegalStateException("erro inesperado");
        });

        int total = ConsumerDemo.consume(consumer, TOPIC);

        assertEquals(1, total);
        assertTrue(consumer.closed());
    }
}
