package com.example.furryfriends.domain.model

/**
 * Represents the supported animal species in the app.
 * @param type The lowercase string used for API search queries.
 * @param mlLabel The capitalized string returned by the Machine Learning model.
 */
enum class Species(val type: String, val mlLabel: String) {
    CATS("cats", "Cat"),
    DOGS("dogs", "Dog"),
    RABBITS("rabbits", "Rabbit"),
    BIRDS("birds", "Bird"),
    HORSES("horses", "Horse");

    companion object {
        /**
         * Returns a set of all capitalized ML labels for quick lookup.
         */
        val allMlLabels: Set<String> = entries.map { it.mlLabel }.toSet()
    }
}
