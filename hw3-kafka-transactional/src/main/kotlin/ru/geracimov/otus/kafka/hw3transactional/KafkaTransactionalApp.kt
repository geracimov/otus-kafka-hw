package ru.geracimov.otus.kafka.hw3transactional

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord

private val log = KotlinLogging.logger {}

suspend fun main() {
    val bootstrapServers = "localhost:19092,localhost:29092,localhost:39092"
    val topics = listOf("topic1", "topic2")

    coroutineScope {
        launch {
            ReadCommitedConsumer(bootstrapServers)
                .subscribe(topics)
        }
        launch {
            ReadUncommitedConsumer(bootstrapServers)
                .subscribe(topics)
        }


        log.info("PRODUCING RECORDS")
        delay(5000)

        CommitTransactionalProducer(bootstrapServers).produce(
            listOf(
                ProducerRecord("topic1", "c1", "com_value1"),
                ProducerRecord("topic1", "c2", "com_value2"),
                ProducerRecord("topic1", "c3", "com_value3"),
                ProducerRecord("topic1", "c4", "com_value4"),
                ProducerRecord("topic1", "c5", "com_value5"),

                ProducerRecord("topic2", "c6", "com_value6"),
                ProducerRecord("topic2", "c7", "com_value7"),
                ProducerRecord("topic2", "c8", "com_value8"),
                ProducerRecord("topic2", "c9", "com_value9"),
                ProducerRecord("topic2", "c0", "com_value0"),
            )
        )

        AbortTransactionalProducer(bootstrapServers).produce(
            listOf(
                ProducerRecord("topic1", "u11", "unc_value11"),
                ProducerRecord("topic1", "u12", "unc_value12"),

                ProducerRecord("topic2", "u13", "unc_value13"),
                ProducerRecord("topic2", "u14", "unc_value14"),
            )
        )

    }

}


