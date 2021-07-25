package com.github.aleperaltabazas.brave

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.github.aleperaltabazas.brave.model.Unit
import com.typesafe.config.ConfigFactory
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import spark.Spark
import java.net.URL

private val SCHEMA = CsvSchema.emptySchema()
    .withHeader()
    .withColumnSeparator(';')

fun main() = runBlocking {
    val port = ProcessBuilder().environment()
        ?.get("PORT")
        ?.toInt() ?: 9290

    LOGGER.info("Using port $port")

    Spark.port(port)
    Spark.get("/*") { _, _ -> "I feel fantastic and I'm still alive" }

    val config = ConfigFactory.load()
    val client = Kord(config.getString("discord.bot.token"))
    val csv = javaClass.getResource("/units.csv")!!.readText()
    val units = CsvMapper()
        .readerFor(Map::class.java)
        .with(SCHEMA)
        .readValues<Map<String, String>>(csv)
        .asSequence()
        .map { Unit.fromCsvRow(it) }
        .toList()

    client.on<MessageCreateEvent> {
        if (!message.content.startsWith("!summon")) return@on

        val requestedUnit = message.content.drop("!summon".length)
            .trim()
            .takeIf { it.isNotBlank() || it.isNotEmpty() }

        try {
            val unit =
                requestedUnit
                    ?.let { ru -> units.filter { u -> u.name.contains(ru, true) } }
                    ?.randomOrNull()
                    ?: units.random()

            val url = unit.arts.random()

            launch(Dispatchers.IO) {
                val input = URL(url).openStream()
                val fileName = url.reversed().drop(1).takeWhile { it != '/' }.reversed()

                message.channel.createMessage {
                    this.content = "<@${message.author?.id?.asString}> summoned **${unit.name}**!"
                    this.addFile(fileName, input)
                }
            }
        } catch (e: Exception) {
            LOGGER.error("An error occurred", e)
            message.channel.createMessage("Beep boop andá a mirar a los logs")
        }

        delay(5000)
    }

    client.login()
}

private val LOGGER = LoggerFactory.getLogger("BraveBot")
