package com.github.aleperaltabazas.brave.cache

import com.github.aleperaltabazas.brave.client.GithubClient
import com.github.aleperaltabazas.brave.model.BraveUnit

class UnitCache(
    private val githubClient: GithubClient,
) {
    private var units: List<BraveUnit> = emptyList()
        @Synchronized get
        @Synchronized set

    fun refresh() {
        val units = githubClient.fetchUnits()
    }
}