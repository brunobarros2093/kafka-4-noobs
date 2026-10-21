# Explicando o código dos demos

Aqui explicamos, parte por parte, os três demos usados no [tutorial](../../../../../../../../README.md):

| Demo | O que faz |
|---|---|
| `ProducerDemo` | Envia **1** mensagem (`hello world`) para o tópico `demo_java`. |
| `ProducerDemoWithCallback` | Envia **20** mensagens com chave e usa um *callback* para logar em qual partição cada uma caiu. |
| `ConsumerDemo` | Assina o tópico `demo_java` e loga cada mensagem recebida, até você apertar `Ctrl+C`. |

O "contrato" entre eles é simples: tópico `demo_java`, chave e valor do tipo `String`, broker em `127.0.0.1:9092` (o `ProducerDemo` usa `localhost:9092`, que é a mesma coisa).

Os três seguem o mesmo formato: o `main` cria o cliente Kafka de verdade e chama métodos `static` pequenos (`createProperties`, `send`, `sendMessages`, `consume`). Esses métodos recebem as interfaces `Producer`/`Consumer`, e não as classes concretas. Por isso dá para testá-los com o `MockProducer` e o `MockConsumer` do próprio `kafka-clients`, sem precisar de um Kafka rodando (veja `src/test`).

---

# ProducerDemo

O produtor mais simples possível.

## Configuração das propriedades

```java
static Properties createProperties(String bootstrapServers) {
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers", bootstrapServers);
    properties.setProperty("value.serializer", StringSerializer.class.getName());
    properties.setProperty("key.serializer", StringSerializer.class.getName());
    return properties;
}
```

- `bootstrap.servers`: endereço do Kafka. Basta um broker: ele informa ao cliente onde estão os outros.
- `key.serializer` / `value.serializer`: o Kafka só trafega **bytes**, e o serializer transforma a chave e o valor (aqui, `String`) em bytes.

## Envio

```java
static Future<RecordMetadata> send(Producer<String, String> producer, String topic, String key, String value) {
    // create Record
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
    return producer.send(record);
}
```

O `ProducerRecord` é a mensagem: tópico, chave e valor. O `send` é **assíncrono**: ele coloca a mensagem num buffer e retorna na hora um `Future`, que completa quando o broker confirma.

## O main

```java
Properties properties = createProperties("localhost:9092");
// producer
KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
// send data
send(producer, "demo_java", "", "hello world");
// flush: espera as mensagens pendentes serem enviadas de fato ao broker
producer.flush();
// close: libera conexões e recursos do produtor
producer.close();
```

Como o envio é assíncrono, sem o `flush()`/`close()` o programa poderia terminar antes de a mensagem sair do buffer.

---

# ProducerDemoWithCallback

Este produtor envia mensagens a um tópico e usa um callback para tratar a resposta depois do envio.

## Configuração das propriedades do produtor

```java
static Properties createProperties(String bootstrapServers) {
    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    return properties;
}
```

São as mesmas propriedades do `ProducerDemo`, mas usando as constantes de `ProducerConfig` no lugar das strings, o que evita erros de digitação:
- `BOOTSTRAP_SERVERS_CONFIG`: endereço do servidor Kafka.
- `KEY_SERIALIZER_CLASS_CONFIG` e `VALUE_SERIALIZER_CLASS_CONFIG`: definem os serializadores da chave e do valor da mensagem. Aqui usamos `StringSerializer` para os dois.

## Criação do produtor Kafka

```java
String bootstrapServers = "127.0.0.1:9092";
Properties properties = createProperties(bootstrapServers);
Thread.currentThread().setContextClassLoader(null);
KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
```

O `setContextClassLoader(null)` contorna problemas de classloader em algumas IDEs e formas de execução: com ele, o Kafka passa a usar o próprio classloader para carregar os serializers informados nas propriedades.

## Envio das mensagens

```java
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
```

No `main`, isso é chamado como `sendMessages(producer, "demo_java", 2, 10);`, ou seja, 2 lotes de 10 mensagens.

- O **callback** (a lambda passada ao `send`) roda quando o broker responde. Se `e == null`, deu certo e o `recordMetadata` traz a partição (e o offset) onde a mensagem foi gravada. Se não, logamos o erro.
- A chave define a partição: o Kafka calcula um hash da chave, então `id_1` cai **sempre** na mesma partição, nos dois lotes. Por isso o log mostra as mesmas chaves agrupadas nas mesmas partições.

---

# ConsumerDemo

O consumidor que lê o que os produtores enviaram.

## Configuração das propriedades do consumidor

```java
static Properties createProperties(String bootstrapServers, String groupId) {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return properties;
}
```

- `KEY_DESERIALIZER_CLASS_CONFIG` e `VALUE_DESERIALIZER_CLASS_CONFIG`: o caminho inverso do serializer, de bytes de volta para `String`. Precisam bater com o que o produtor usou.
- `GROUP_ID_CONFIG`: o **consumer group** (`my-java-application` no `main`). O Kafka guarda, por grupo, até onde ele já leu (o *offset*). Se você parar e subir o consumidor de novo, ele continua de onde parou.
- `AUTO_OFFSET_RESET_CONFIG = "earliest"`: quando o grupo ainda **não tem** offset salvo, começa do início do tópico. Assim você também recebe as mensagens enviadas antes de o consumidor subir. Com `latest`, receberia só as novas.

## Formatação de cada mensagem

```java
static String format(ConsumerRecord<String, String> record) {
    return "Key: " + record.key()
            + " | Value: " + record.value()
            + " | Partition: " + record.partition()
            + " | Offset: " + record.offset();
}
```

## O loop de consumo

```java
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
```

- `subscribe`: assina o tópico. O Kafka distribui as partições entre os consumidores do mesmo grupo. Como aqui há um só, ele recebe as 3.
- `poll`: busca um lote de mensagens. Se não houver nenhuma, espera até 1 segundo e devolve um lote vazio, e o loop continua.
- Os offsets lidos são salvos automaticamente (`enable.auto.commit`, ligado por padrão): de tempos em tempos durante o `poll` (a cada 5 segundos, por padrão) e também no `close()`.

## Desligamento gracioso (Ctrl+C)

```java
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
```

O loop é infinito, então como ele termina? Ao apertar `Ctrl+C`, a JVM roda o *shutdown hook* numa outra thread. O `consumer.wakeup()` faz o `poll()` da thread principal lançar uma `WakeupException`. Ela é capturada no `consume`, que sai do loop e cai no `finally`, onde o `consumer.close()` salva os offsets e avisa o grupo que este consumidor saiu. O `mainThread.join()` faz a JVM esperar esse fechamento terminar antes de encerrar.

Sem isso, o processo morreria no meio do caminho, e o grupo levaria alguns segundos (o timeout de sessão) para perceber que o consumidor sumiu.
