package ru.geracimov.otus.kafka.hw4kstreams

import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.ListTopicsOptions
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.serialization.Serdes.*
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.*
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess


val log = LoggerFactory.getLogger(Any::class.java)
const val inputTopicName = "events"
const val outputTopicName = "counts"

fun main() {


    val props = mapOf(
        Pair(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:29092,localhost:39092"),
        Pair(StreamsConfig.APPLICATION_ID_CONFIG, "KafkaStreamsApp"),
    )


    Admin.create(props).use { client ->
        val topicsList = client.listTopics(ListTopicsOptions()).listings().get()

        val topicsToDelete =
            topicsList.filter { topic -> topic.name() == inputTopicName || topic.name() == outputTopicName }
                .map { it.name() }
                .toList()
        client.deleteTopics(topicsToDelete).all().get()
        log.info("delete topics")
        client.createTopics(listOf(NewTopic(inputTopicName, 3, 2))).all().get()
        log.info("create topics")
    }


    val builder = StreamsBuilder()
    builder
        .stream(inputTopicName, Consumed.with(StringSerde(), StringSerde()))
        .groupBy({ key, _ -> key }, Grouped.with(StringSerde(), StringSerde()))
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
        .count()
        .toStream()
        .peek { key, value -> log.info("Количество по ключу ${key.key()} = $value") }
        .to(outputTopicName)

    val topology = builder.build()
    log.info(topology.describe().toString())

    val kafkaStreams = KafkaStreams(topology, StreamsConfig(props))
    val latch = CountDownLatch(1)

    Runtime.getRuntime().addShutdownHook(object : Thread("streams-shutdown-hook") {
        override fun run() {
            log.info("Application shutting down... Closing KafkaStreams...")
            kafkaStreams.close()
            latch.countDown()
        }
    })

    try {
        kafkaStreams.start()
        log.info("KafkaStreams started")
        latch.await()
    } catch (e: Throwable) {
        exitProcess(1)
    }
    exitProcess(0)

}




