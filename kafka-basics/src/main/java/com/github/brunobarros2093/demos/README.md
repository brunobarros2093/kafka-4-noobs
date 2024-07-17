Este código é um exemplo de um produtor Kafka que envia mensagens a um tópico e utiliza um callback para tratar a resposta após o envio. O Kafka é uma plataforma de streaming distribuída, frequentemente usada para construir pipelines de dados em tempo real e aplicativos de streaming.

# Configuração das Propriedades do Produtor:
```java
String bootstrapServers = "127.0.0.1:9092";
Properties properties = new Properties();
properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
```

As propriedades do produtor Kafka são as seguintes:
- bootstrapServers: Endereço do servidor Kafka.
- KEY_SERIALIZER_CLASS_CONFIG
- VALUE_SERIALIZER_CLASS_CONFIG: Definem os serializadores para a chave e o valor da mensagem. 
Aqui, usamos StringSerializer para ambos.

# Criação do Produtor Kafka
```java
Thread.currentThread().setContextClassLoader(null);
KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
```
# Envio das mensagens 
```java
    
    for (int j = 0; j < 2; j++) {
        // send data
        for (int i = 0; i < 10; i++) {
            String topic = "demo_java";
            String key = "id_" + i;
            String value = "Hello World_" + i;
            // create Record = payload/mensagem a ser enviada 
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
``` 
