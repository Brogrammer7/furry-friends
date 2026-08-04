package com.example.furryfriends.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ResourceItem(
    val type: String,
    val id: String,
    val attributes: AnimalAttributes,
    val relationships: Relationships
)

@Serializable
data class AnimalAttributes(
    val distance: Double? = null,
    val isAdoptionPending: Boolean? = null,
    val ageGroup: String? = null,
    val ageString: String? = null,
    val birthDate: String? = null,
    val isBirthDateExact: Boolean? = null,
    val breedString: String? = null,
    val breedPrimary: String? = null,
    val breedPrimaryId: Int? = null,
    val isBreedMixed: Boolean? = null,
    val coatLength: String? = null,
    val isCourtesyListing: Boolean? = null,
    val descriptionHtml: String? = null,
    val isFound: Boolean? = null,
    val priority: Int? = null,
    val name: String? = null,
    val pictureCount: Int? = null,
    val pictureThumbnailUrl: String? = null,
    val rescueId: String? = null,
    val searchString: String? = null,
    val sex: String? = null,
    val sizeGroup: String? = null,
    val slug: String? = null,
    val isSponsorable: Boolean? = null,
    val trackerimageUrl: String? = null,
    val videoCount: Int? = null,
    val videoUrlCount: Int? = null,
    val createdDate: String? = null,
    val updatedDate: String? = null
)

@Serializable
data class Relationships(
    val pictures: RelationshipDataWrapper? = null,
    val orgs: RelationshipDataWrapper? = null
)

@Serializable
data class RelationshipDataWrapper(
    val data: List<RelationshipData> = emptyList()
)

@Serializable
data class RelationshipData(
    val type: String,
    val id: String
)
