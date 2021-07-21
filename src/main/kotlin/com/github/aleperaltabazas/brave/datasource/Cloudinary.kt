package com.github.aleperaltabazas.brave.datasource

import com.cloudinary.api.ApiResponse
import com.cloudinary.api.exceptions.ApiException
import com.typesafe.config.Config

data class CloudinaryConfig(
    val apiKey: String,
    val secret: String,
    val cloudName: String,
) {
    constructor(moduleConfig: Config) : this(
        apiKey = moduleConfig.getString("api-key"),
        secret = moduleConfig.getString("secret"),
        cloudName = moduleConfig.getString("cloud-name")
    )

    val connectionString: String
        get() = "cloudinary://$apiKey:$secret@$cloudName"
}

fun ApiResponse.matchingUrl(unitFileName: String): String? = this["resources"]
    ?.let { it as? List<Map<String, *>> }
    ?.find { it["filename"]?.let { f -> f as String? }?.startsWith(unitFileName) ?: false }
    ?.let { it["secure_url"]?.let { u -> u as String? } }
