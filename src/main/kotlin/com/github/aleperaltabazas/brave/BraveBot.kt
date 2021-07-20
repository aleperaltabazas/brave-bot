package com.github.aleperaltabazas.brave

import com.typesafe.config.ConfigFactory
import dev.kord.core.Kord
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.delay

suspend fun main() {
    val config = ConfigFactory.load()
    val client = Kord(config.getString("discord.bot.token"))
    val pingPong = ReactionEmoji.Unicode("\uD83C\uDFD3")



    client.on<MessageCreateEvent> {
        if (message.content != "!ping") return@on

        val response = message.channel.createMessage("Pong!")
        response.addReaction(pingPong)

        delay(5000)
        message.delete()
        response.delete()
    }

    client.login()
}
