package com.github.aleperaltabazas.brave.client

import com.github.aleperaltabazas.brave.dto.UnitInfoDTO
import retrofit2.http.GET

interface GithubClient {
    @GET("/cheahjs/bravefrontier_data/master/info.json")
    fun fetchUnits(): Map<String, UnitInfoDTO>
}