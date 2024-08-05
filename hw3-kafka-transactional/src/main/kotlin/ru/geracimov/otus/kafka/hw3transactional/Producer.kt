package ru.geracimov.otus.kafka.hw3transactional

import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

private val log = KotlinLogging.logger {}

abstract class Producer(bootstrapServers: String) {
    val properties: Properties = Properties()
    lateinit var kafkaProducer: KafkaProducer<String, String>

    init {
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        properties[ProducerConfig.ACKS_CONFIG] = "all"
        properties[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
    }

    abstract suspend fun produce(records: List<ProducerRecord<String, String>>)

}

class AbortTransactionalProducer(bootstrapServers: String) : Producer(bootstrapServers) {
    init {
        properties[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = "kafka_abort_producer_tr_id"
        kafkaProducer = KafkaProducer<String, String>(properties)
    }

    override suspend fun produce(records: List<ProducerRecord<String, String>>) {
        val className = this.javaClass.simpleName

        log.info { "[$className] begin transaction" }
        kafkaProducer.initTransactions()
        kafkaProducer.beginTransaction()

        records.forEach { kafkaProducer.send(it, LoggingCallback()) }
        delay(10000)
        log.warn { "[$className] aborting transaction" }

        kafkaProducer.abortTransaction()
        log.warn { "[$className] transaction aborted" }
    }
}

class CommitTransactionalProducer(bootstrapServers: String) : Producer(bootstrapServers) {
    init {
        properties[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = "kafka_commit_producer_tr_id"
        kafkaProducer = KafkaProducer<String, String>(properties)
    }

    override suspend fun produce(records: List<ProducerRecord<String, String>>) {
        val className = this.javaClass.simpleName

        log.info { "[$className] begin transaction" }
        kafkaProducer.initTransactions()
        kafkaProducer.beginTransaction()

        log.info { "[$className] send records" }
        records.forEach { kafkaProducer.send(it, LoggingCallback()) }

        log.info { "[$className] commiting transaction" }
        kafkaProducer.commitTransaction()
        log.info { "[$className] transaction commited" }
    }
}

class LoggingCallback : Callback {
    private val log = KotlinLogging.logger {}

    override fun onCompletion(metadata: RecordMetadata?, exception: Exception?) {
        log.info { "sent topic=${metadata?.topic()} part=${metadata?.partition()} offset=${metadata?.offset()}" }
    }
}
