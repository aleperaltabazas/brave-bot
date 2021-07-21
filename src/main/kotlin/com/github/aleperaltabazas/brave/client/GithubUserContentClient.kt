package com.github.aleperaltabazas.brave.client

import com.github.aleperaltabazas.brave.dto.UnitInfoDTO
import retrofit2.Call
import retrofit2.http.GET

interface GithubUserContentClient {
    @GET("/cheahjs/bravefrontier_data/master/info.json")
    fun fetchUnits(): Call<Map<String, UnitInfoDTO>>
}