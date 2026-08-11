package com.example.furryfriends.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class IncludedItem(
    val type: String,
    val id: String,
    val attributes: IncludedAttributes
)

@Serializable
data class IncludedAttributes(
    // picture attributes
    val original: ImageSize? = null,
    val large: ImageSize? = null,
    val small: ImageSize? = null,
    val order: Int? = null,
    val created: String? = null,
    val updated: String? = null,

    // org attributes
    val name: String? = null,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalcode: String? = null,
    val country: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val url: String? = null,
    val facebookUrl: String? = null,
    val adoptionUrl: String? = null,
    val donationUrl: String? = null,
    val adoptionProcess: String? = null,
    val about: String? = null,
    val services: String? = null,
    val type: String? = null,
    val citystate: String? = null
)

@Serializable
data class ImageSize(
    val resolutionX: Int? = null,
    val resolutionY: Int? = null,
    val url: String? = null
)
