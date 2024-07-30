# Kafka-4-noobs [WIP] 
Este repositório tem como objetivo servir de ajuda e guia para quem está começando no kafka.

Para subir o kafka na nossa máquina, estamos utilizando o docker-compose, navegue até a pasta Docker e dentro dela você encontrará o zk-single-kafka-single.yml. 

Esse yaml foi criado pela Conduktor e existem mais opções aqui [link](https://www.conduktor.io/kafka/complete-kafka-producer-with-java/). Nós subiremos apenas 1 kafka broker, 1 zookeper e 1 topico apenas.

> docker-compose -f zk-single-kafka-single up -d

# Consumidor kafka via CLI 

Após executar e subir o Kafka acima, você pode subir um consumer via CLI, ou pelo ConsumerDemo.

> kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo_java 

# Principais Tópicos

O Apache Kafka é uma plataforma de streaming distribuída e de código aberto projetada para construir pipelines de dados em tempo real e aplicativos de streaming.

O Kafka opera como um **sistema de mensagens pub-sub distribuído**, permitindo que aplicações publiquem e assinem feeds de dados em tempo real ou quase em tempo real. **A alta taxa de transferência, escalabilidade, tolerância a falhas, durabilidade e ecossistema que o Kafka oferece o tornaram uma escolha muito popular para casos de uso que exigem feeds de dados em tempo real**.

**Os principais componentes do Kafka incluem**:

🔸 **Produtor**

Produtores publicam (escrevem) mensagens para um tópico do Kafka.

🔸 **Consumidor**

Consumidores/assinantes assinam tópicos e processam (leem) o fluxo de mensagens publicadas.

🔸 **Broker**

Brokers são servidores Kafka que armazenam dados e atendem clientes. Vários brokers formam um cluster.

🔸 **Tópico**

Tópicos são um nome de feed ou categoria de mensagens para as quais os produtores publicam mensagens.

🔸 **Partição**

As mensagens são organizadas em tópicos, que podem ser ainda mais divididos em partições. Isso aumenta o paralelismo e a escalabilidade, permitindo que consumidores leiam diferentes partições ao mesmo tempo.

**O Kafka pode ser usado para muitos casos de uso. Alguns dos casos de uso mais comuns incluem**:

🔹 **Agregação de dados de diferentes fontes**: Seja em pipelines ETL, lakes de dados ou agregação de logs; ingerir, organizar, transformar e distribuir dados são funções principais do Kafka.

🔹 **Processamento de streams**: O Kafka pode ser usado para construir aplicativos de análise em tempo real.

🔹 **Processamento de eventos**: Sistemas ou aplicativos que dependem do processamento de eventos em tempo real, como dispositivos IoT.

🔹 **Monitoramento**: O Kafka é bem adequado para armazenar logs e métricas, permitindo monitoramento e alertas em tempo real.

Projetado para tolerância a falhas, alta taxa de transferência, durabilidade e escalabilidade, o Kafka serve como um serviço intermediário para lidar com grandes volumes de dados. Embora o Kafka ofereça muitas vantagens para construir pipelines de dados em tempo real e aplicativos de streaming, ele também traz algumas desvantagens, sendo a mais notável a complexidade adicional que adiciona à arquitetura do sistema. Dito isso, se um feed de dados em tempo real for necessário, o Kafka é uma escolha popular.


# Como o kafka Funciona (gif by levelupcoding.com):
![kafka-visual-guide.gif](kafka-basics%2Fsrc%2Fmain%2Fjava%2Fcom%2Fgithub%2Fbrunobarros2093%2Fdemos%2Fpublic%2Fkafka-visual-guide.gif)
