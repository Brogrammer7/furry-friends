package com.example.furryfriends.model

data class FindResponse(
    val data: List<Animals?>
)

data class Animals(
    val attributes: GetAttributes,
)

data class GetAttributes(
    val ageString: String,
    val breedPrimary: String,
    val name: String,
    val pictureThumbnailUrl: String
)
