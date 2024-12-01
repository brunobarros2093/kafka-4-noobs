package main

import (
	"fmt"
	"log"
	"os"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

func main() {
	log.Println("ProducerDemo")

	// Configuração do produtor
	producer, err := kafka.NewProducer(&kafka.ConfigMap{
		"bootstrap.servers": "localhost:9092",
	})
	if err != nil {
		log.Fatalf("Erro ao criar o produtor: %s", err)
	}
	defer producer.Close()

	// Criação do record
	topic := "demo_java"
	key := ""
	value := "hello world"

	// Envio da mensagem
	deliveryChan := make(chan kafka.Event)

	err = producer.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
		Key:            []byte(key),
		Value:          []byte(value),
	}, deliveryChan)

	if err != nil {
		log.Fatalf("Erro ao produzir mensagem: %s", err)
	}

	// Confirmação da entrega
	e := <-deliveryChan
	msg := e.(*kafka.Message)

	if msg.TopicPartition.Error != nil {
		fmt.Fprintf(os.Stderr, "Falha ao enviar mensagem: %v\n", msg.TopicPartition.Error)
	} else {
		fmt.Printf("Mensagem enviada para %v\n", msg.TopicPartition)
	}

	close(deliveryChan)
}
