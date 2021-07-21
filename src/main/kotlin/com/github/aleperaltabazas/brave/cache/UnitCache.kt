package com.github.aleperaltabazas.brave.cache

import com.github.aleperaltabazas.brave.client.GithubUserContentClient
import com.github.aleperaltabazas.brave.model.BraveUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class UnitCache(
    private val githubUserContent: GithubUserContentClient,
) {
    var units: List<BraveUnit>
        @Synchronized get
        @Synchronized private set

    init {
        units = emptyList()
        refresh()

        GlobalScope.launch(Dispatchers.IO) {
            // every 1 hour
            delay(3600000)
            refresh()
        }
    }

    private fun refresh() {
        LOGGER.info("Refreshing units...")
        val response = githubUserContent.fetchUnits().execute()
        val units = response
            .takeIf { it.isSuccessful }
            ?.body()
            ?: run {
                throw IllegalStateException("Failed to start unit cache: ${response.code()}: ${response.body()}")
            }

        this.units = units.map {
            val unitName = it.value.name
            val fileName = unitName
                .replace(" ", "-")
                .replace("&", "and")
                .replace("'", "")
                .lowercase()

            BraveUnit(
                name = it.value.name,
                fileName = fileName,
            )
        }

        LOGGER.info("Finished refreshing")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(UnitCache::class.java)
    }
}