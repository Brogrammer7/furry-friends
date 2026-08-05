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

        /**
         * Finds a Species by its API type string (e.g. "cats" or "Cat").
         */
        fun fromType(type: String?): Species? {
            if (type == null) return null
            return entries.find { 
                it.type.equals(type, ignoreCase = true) || 
                it.mlLabel.equals(type, ignoreCase = true) ||
                it.type.startsWith(type, ignoreCase = true) ||
                type.startsWith(it.mlLabel, ignoreCase = true)
            }
        }

        /**
         * Finds a matching Species for a given ML label.
         */
        fun fromMlLabel(label: String): Species? {
            val lowerLabel = label.lowercase()
            return entries.find { species ->
                lowerLabel == species.mlLabel.lowercase() ||
                lowerLabel.contains(species.mlLabel.lowercase())
            }
        }
    }
}
