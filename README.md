# Kafka-4-noobs [WIP] 
Este repositório tem como objetivo servir de ajuda e guia para quem está começando no kafka.

Para subir o kafka na nossa máquina, estamos utilizando o docker-compose, navegue até a pasta Docker e dentro dela você encontrará o zk-single-kafka-single.yml. 

Esse yaml foi criado pela Conduktor e existem mais opções aqui [link](https://www.conduktor.io/kafka/complete-kafka-producer-with-java/). Nós subiremos apenas 1 kafka broker, 1 zookeper e 1 topico apenas.

> docker-compose -f zk-single-kafka-single up -d

# Consumidor kafka via CLI 

Após executar e subir o Kafka acima, você pode subir um consumer via CLI, ou pelo ConsumerDemo.

> kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo_java 
