package com.github.aleperaltabazas.brave

import com.cloudinary.Cloudinary
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.aleperaltabazas.brave.cache.UnitCache
import com.github.aleperaltabazas.brave.client.GithubUserContentClient
import com.github.aleperaltabazas.brave.datasource.CloudinaryConfig
import com.github.aleperaltabazas.brave.datasource.matchingUrl
import com.typesafe.config.ConfigFactory
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

suspend fun main() {
    val config = ConfigFactory.load()
    val client = Kord(config.getString("discord.bot.token"))

    val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule())
        registerModule(AfterburnerModule())
        registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    }

    val github = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com")
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
        .create(GithubUserContentClient::class.java)

    val cache = UnitCache(github)
    val cloudinaryConfig = CloudinaryConfig(config.getConfig("cloudinary"))
    val cloudinary = Cloudinary(cloudinaryConfig.connectionString)

    client.on<MessageCreateEvent> {
        if (!message.content.startsWith("!summon")) return@on

        val requestedUnit = message.content.drop("!summon".length)
            .trim()
            .takeIf { it.isNotBlank() || it.isNotEmpty() }

        try {
            val unit =
                requestedUnit
                    ?.let { u -> cache.units.filter { it.name.contains(u, true) } }
                    ?.randomOrNull()
                    ?: cache.units.random()

            val url = cloudinary.search()
                .expression("${unit.fileName} AND resource_type:image")
                .execute()
                .matchingUrl(unit.fileName)
            val m = listOfNotNull(
                "You summoned **${unit.name}**!",
                url,
            )
            val response = message.channel.createMessage(m.joinToString("\n"))
            response.attachments.forEach { it.isImage }
        } catch (e: Exception) {
            LOGGER.error("An error occurred", e)
            message.channel.createMessage("Beep boop andá a mirar a los logs")
        }

        delay(5000)
    }

    client.login()
}

private val LOGGER = LoggerFactory.getLogger("BraveBot")
