# Kafka-4-noobs

Este repositório serve de ajuda e guia para quem está começando com o Kafka.

Ao final deste tutorial você vai ter:

1. subido um Kafka na sua máquina com Docker;
2. criado um tópico;
3. enviado e recebido mensagens pelo terminal (CLI);
4. enviado mensagens com um **produtor Java** e recebido essas mensagens com um **consumidor Java**.

> 💡 Na raiz existe um `main.go`, que é um produtor de exemplo em Go ainda em andamento. Este tutorial usa **Java**.

# Sobre o Apache Kafka

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

## Como o kafka Funciona (gif by levelupcoding.com):
![kafka-visual-guide.gif](public/kafka-visual-guide.gif)

# Pré-requisitos

- **Java 17** ou superior (`java -version`)
- **Maven 3.9** ou superior (`mvn -version`)
- **Docker** com o **Compose v2** (`docker compose version`)
- Uma IDE (IntelliJ, VS Code, Eclipse...). É opcional, mas ajuda bastante.

Você **não** precisa instalar o Kafka: ele roda dentro de um container, e os comandos de CLI também.

# Estrutura do repositório

```
kafka-4-noobs/
├── Docker/
│   └── zk-single-kafka-single.yml    # sobe 1 ZooKeeper + 1 broker Kafka
├── kafka-basics-java-example/         # projeto Java com os demos
│   └── src/main/java/com/github/brunobarros2093/demos/
│       ├── ProducerDemo.java              # envia 1 mensagem
│       ├── ProducerDemoWithCallback.java  # envia 20 mensagens e mostra a partição de cada uma
│       ├── ConsumerDemo.java              # lê as mensagens do tópico
│       └── README.md                      # explicação do código, linha a linha
├── public/                            # imagens do README
├── main.go, go.mod, go.sum            # produtor em Go (em andamento, fora deste tutorial)
└── pom.xml                            # pom pai do Maven
```

# Passo 1: subir o Kafka

O arquivo `Docker/zk-single-kafka-single.yml` foi criado pela Conduktor e existem mais opções [aqui](https://www.conduktor.io/kafka/complete-kafka-producer-with-java/). Ele sobe **1 broker Kafka** e **1 ZooKeeper**.

```bash
cd Docker
docker compose -f zk-single-kafka-single.yml up -d
```

> Se você tem o Compose antigo (v1), o comando é `docker-compose -f zk-single-kafka-single.yml up -d`.

Na primeira vez o Docker baixa as imagens, o que pode levar alguns minutos. Depois confira se os dois containers estão de pé:

```bash
docker ps
```

Você deve ver `kafka1` (porta `9092`) e `zoo1` (porta `2181`) com status `Up`. Espere uns 10 a 20 segundos depois de subir para o broker terminar de iniciar.

* O ZooKeeper já está meio descontinuado (as versões novas do Kafka usam o modo KRaft, sem ZooKeeper), mas mais para frente a gente fala sobre isso, confia.

**Sobre os endereços:** de dentro do container o broker atende em `kafka1:19092`, que é o endereço usado nos comandos de CLI abaixo. Da sua máquina (os programas Java) o endereço é `127.0.0.1:9092` (ou `localhost:9092`).

# Passo 2: criar o tópico

O compose não cria nenhum tópico, então vamos criar o `demo_java`, que é o tópico usado por todos os demos:

```bash
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:19092 --create --topic demo_java --partitions 3 --replication-factor 1
```

- `--partitions 3`: o tópico é dividido em 3 partições. Assim dá para ver o Kafka distribuindo as mensagens.
- `--replication-factor 1`: cada partição tem 1 cópia. Temos só 1 broker, então não dá para ter mais.

> Pode aparecer um `WARNING` sobre tópicos com `.` ou `_` no nome. É só um aviso e pode ser ignorado.

Para ver os detalhes do tópico:

```bash
docker exec -it kafka1 kafka-topics --bootstrap-server kafka1:19092 --describe --topic demo_java
```

Saída esperada (o `TopicId` muda):

```
Topic: demo_java	TopicId: ...	PartitionCount: 3	ReplicationFactor: 1	Configs:
	Topic: demo_java	Partition: 0	Leader: 1	Replicas: 1	Isr: 1
	Topic: demo_java	Partition: 1	Leader: 1	Replicas: 1	Isr: 1
	Topic: demo_java	Partition: 2	Leader: 1	Replicas: 1	Isr: 1
```

# Passo 3: testar pelo terminal (CLI)

Antes do Java, vamos enviar e receber pelo próprio Kafka. Abra **dois terminais**.

**Terminal 1, consumidor** (fica esperando mensagens):

```bash
docker exec -it kafka1 kafka-console-consumer --bootstrap-server kafka1:19092 --topic demo_java --from-beginning
```

`--from-beginning` faz o consumidor ler o tópico desde o início, e não só o que chegar a partir de agora.

**Terminal 2, produtor** (cada linha que você digitar vira uma mensagem):

```bash
docker exec -it kafka1 kafka-console-producer --bootstrap-server kafka1:19092 --topic demo_java
```

Digite `oi kafka` e aperte Enter. A mensagem aparece no Terminal 1 🎉

Para sair, aperte `Ctrl+C` nos dois terminais.

> Nas imagens da Confluent os comandos não têm `.sh` no final (`kafka-console-consumer`, e não `kafka-console-consumer.sh`). Se você instalou o Kafka "na mão", aí sim os scripts terminam em `.sh` (Linux/Mac) ou `.bat` (Windows).

# Passo 4: compilar o projeto Java

Na **raiz** do repositório:

```bash
mvn -B test
```

Esse comando baixa as dependências, compila e roda os testes unitários. Os testes não precisam do Kafka rodando. No final deve aparecer `BUILD SUCCESS`.

# Passo 5: subir o consumidor Java (`ConsumerDemo`)

Você pode rodar os demos de dois jeitos:

- **pela IDE:** abra a classe e clique em *Run* no método `main`;
- **pelo terminal, com Maven**, sempre a partir da **raiz** do repositório.

Num terminal novo, suba o consumidor.

**bash (Linux/Mac/Git Bash):**

```bash
mvn -pl kafka-basics-java-example compile exec:java -Dexec.mainClass=com.github.brunobarros2093.demos.ConsumerDemo
```

**PowerShell (Windows):** o `-D...` precisa ficar **entre aspas**:

```powershell
mvn -pl kafka-basics-java-example compile exec:java "-Dexec.mainClass=com.github.brunobarros2093.demos.ConsumerDemo"
```

O consumidor assina o tópico `demo_java` no consumer group `my-java-application` e fica esperando mensagens. Deixe esse terminal aberto.

Como ele usa `auto.offset.reset=earliest`, na primeira vez também lê as mensagens que já estavam no tópico, como o `oi kafka` do Passo 3.

> Não use `-q` (modo silencioso do Maven) nesses comandos: ele também esconde os logs dos demos.

# Passo 6: enviar mensagens com o produtor Java

Em **outro terminal** (também na raiz), rode o produtor simples, que envia uma única mensagem `hello world`.

**bash:**

```bash
mvn -pl kafka-basics-java-example compile exec:java -Dexec.mainClass=com.github.brunobarros2093.demos.ProducerDemo
```

**PowerShell:**

```powershell
mvn -pl kafka-basics-java-example compile exec:java "-Dexec.mainClass=com.github.brunobarros2093.demos.ProducerDemo"
```

Agora rode o produtor com callback, que envia 2 lotes de 10 mensagens (`id_0` a `id_9`) e loga em qual partição cada uma caiu.

**bash:**

```bash
mvn -pl kafka-basics-java-example compile exec:java -Dexec.mainClass=com.github.brunobarros2093.demos.ProducerDemoWithCallback
```

**PowerShell:**

```powershell
mvn -pl kafka-basics-java-example compile exec:java "-Dexec.mainClass=com.github.brunobarros2093.demos.ProducerDemoWithCallback"
```

Saída do produtor (as partições podem variar; nas saídas abaixo omitimos a hora e o nome da thread que aparecem no começo de cada linha):

```
INFO ProducerDemoWithCallback - Key: id_1| Partition: 0
INFO ProducerDemoWithCallback - Key: id_0| Partition: 1
INFO ProducerDemoWithCallback - Key: id_2| Partition: 2
...
```

Volte no terminal do **consumidor**. Você deve ver algo assim (partições e offsets podem variar):

```
INFO ConsumerDemo - Key:  | Value: hello world | Partition: 0 | Offset: 0
INFO ConsumerDemo - Key: id_1 | Value: Hello World_1 | Partition: 0 | Offset: 1
INFO ConsumerDemo - Key: id_3 | Value: Hello World_3 | Partition: 0 | Offset: 2
INFO ConsumerDemo - Key: id_0 | Value: Hello World_0 | Partition: 1 | Offset: 0
INFO ConsumerDemo - Key: id_8 | Value: Hello World_8 | Partition: 1 | Offset: 1
INFO ConsumerDemo - Key: id_2 | Value: Hello World_2 | Partition: 2 | Offset: 0
...
```

Pronto: você enviou e recebeu mensagens num tópico do Kafka com Java 🚀

**O que observar:**

- **Mesma chave, mesma partição.** O produtor calcula um hash da chave para escolher a partição. Por isso `id_1` cai sempre na mesma partição, nos dois lotes. É assim que o Kafka garante a **ordem** das mensagens de uma mesma chave.
- **Offset** é a posição da mensagem dentro da partição. Cada partição tem a sua contagem, começando em 0.
- A ordem é garantida **dentro de uma partição**, não no tópico inteiro. Por isso o consumidor pode mostrar as mensagens agrupadas por partição.

# Passo 7: encerrar

No terminal do consumidor, aperte `Ctrl+C`. O `ConsumerDemo` faz um desligamento gracioso:

```
INFO ConsumerDemo - Detectado um shutdown, chamando consumer.wakeup()...
INFO ConsumerDemo - Consumer está desligando...
INFO ConsumerDemo - Consumer foi fechado com sucesso
```

Depois derrube o Kafka (dentro da pasta `Docker`):

```bash
docker compose -f zk-single-kafka-single.yml down
```

O `down` apaga os containers e, junto com eles, o tópico e as mensagens. Na próxima vez, recomece do Passo 1.

# Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| `Connection to node -1 (/127.0.0.1:9092) could not be established` | O Kafka não está rodando ou ainda está subindo. Confira com `docker ps` e espere alguns segundos. Se a porta `9092` estiver ocupada por outro programa, o container `kafka1` não sobe. |
| `the attribute 'version' is obsolete` ao rodar o compose | Só um aviso do Compose v2. Pode ignorar. |
| `LEADER_NOT_AVAILABLE` ou `UNKNOWN_TOPIC_OR_PARTITION` | O tópico ainda não existe. Volte ao Passo 2. |
| Os logs dos demos não aparecem | Rode pelo Maven (sem `-q`) ou pela IDE usando o classpath do projeto, que inclui o `slf4j-simple`. Os logs internos do Kafka ficam em `WARN` de propósito (veja `src/main/resources/simplelogger.properties`). |
| No PowerShell: `Unknown lifecycle phase ".mainClass=..."` | Faltaram as aspas: use `"-Dexec.mainClass=..."`. |
| O consumidor demora para receber depois de ser reiniciado | Se o consumidor anterior foi morto à força (sem `Ctrl+C`), o grupo espera o timeout de sessão (cerca de 45 s) antes de redistribuir as partições. |
| O consumidor não mostra mensagens antigas | O `auto.offset.reset=earliest` só vale quando o grupo ainda não tem offset salvo. Depois que o grupo `my-java-application` já leu, ele continua de onde parou. |

# Próximos passos

- Leia a explicação do código dos demos: [kafka-basics-java-example/.../demos/README.md](kafka-basics-java-example/src/main/java/com/github/brunobarros2093/demos/README.md).
- Mais exemplos de docker-compose (vários brokers, KRaft etc.) estão na [Conduktor](https://www.conduktor.io/kafka/complete-kafka-producer-with-java/).
