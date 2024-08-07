package ru.geracimov.otus.kafka.hw3transactional

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.IsolationLevel
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

private val log = KotlinLogging.logger {}

abstract class Consumer(bootstrapServers: String) {

    val properties: Properties = Properties()

    init {
        properties[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        properties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        properties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    }

    fun subscribe(topics: List<String>) {

        KafkaConsumer<String, String>(properties).use {
            val className = this.javaClass.simpleName
            log.info("[$className] subscribe to topics $topics")
            it.subscribe(topics, LoggingConsumerRebalanceListener())
            while (true) {
                it.poll(Duration.ofMillis(100))
                    .forEach { record ->
                        log.info("[$className] received ${record.string()}")
                    }
            }

        }
    }
}

fun <K : Any, V : Any> ConsumerRecord<K, V>.string(): String {
    return "${topic()}-${partition()} ${key()} ${value()}"
}

class ReadCommitedConsumer(bootstrapServers: String) : Consumer(bootstrapServers) {
    init {
        properties[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = IsolationLevel.READ_COMMITTED.toString()
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "read-committed-consumer-group"
    }
}

class ReadUncommitedConsumer(bootstrapServers: String) : Consumer(bootstrapServers) {
    init {
        properties[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = IsolationLevel.READ_UNCOMMITTED.toString()
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "read-uncommitted-consumer-group"
    }
}

class LoggingConsumerRebalanceListener : ConsumerRebalanceListener {
    override fun onPartitionsRevoked(partitions: MutableCollection<TopicPartition>?) {
        log.warn("Rebalance revoked {}", partitions?.map { it.toString() })
    }

    override fun onPartitionsAssigned(partitions: MutableCollection<TopicPartition>?) {
        log.warn("Rebalance assigned {}", partitions?.map { it.toString() })
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}