package com.github.aleperaltabazas.brave.model

data class Unit(
    val name: String,
    val rarity: String,
    val arts: List<String>,
) {
    companion object {
        fun fromCsvRow(row: Map<String, String>) = Unit(
            name = row["name"]!!,
            rarity = row["rarity"]!!,
            arts = row["arts"]!!.drop(1).dropLast(1).split(","),
        )
    }
}
