package com.example.furryfriends.model

import kotlinx.serialization.Serializable

@Serializable
data class FindResponse(
    val data: List<Animals?>
)

@Serializable
data class Animals(
    val attributes: GetAttributes,
)

@Serializable
data class GetAttributes(
    val ageString: String,
    val breedPrimary: String,
    val name: String,
    val pictureThumbnailUrl: String
)
