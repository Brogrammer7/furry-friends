package com.example.furryfriends.model

data class Pets(
    val data: List<Animals?>
)

data class Animals(
    val attributes: Attributes,
)

data class Attributes(
    val ageString: String,
    val breedPrimary: String,
    val name: String,
    val pictureThumbnailUrl: String
)
