package com.example.furryfriends.model

data class SearchResponse(
    val meta: Meta,
    val data: List<ResourceItem>,
    val included: List<IncludedItem> = emptyList(),
    val errors: List<ApiError>? = null
)

data class Meta(
    val count: Int,
    val countReturned: Int,
    val pageReturned: Int,
    val limit: Int,
    val pages: Int,
)

data class ResourceItem(
    val type: String,
    val id: String,
    val attributes: AnimalAttributes,
    val relationships: Relationships
)

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

data class Relationships(
    val pictures: RelationshipDataWrapper? = null,
    val orgs: RelationshipDataWrapper? = null
)

data class RelationshipDataWrapper(
    val data: List<RelationshipData> = emptyList()
)

data class RelationshipData(
    val type: String,
    val id: String
)

data class IncludedItem(
    val type: String,
    val id: String,
    val attributes: IncludedAttributes
)

/*
   IncludedAttributes is a sealed-like structure expressed with optional fields to cover pictures and orgs attribute shapes from the example. You can split into distinct classes (PictureAttributes / OrgAttributes) if you prefer stronger typing.
*/
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

data class ImageSize(
    val resolutionX: Int? = null,
    val resolutionY: Int? = null,
    val url: String? = null
)

data class ApiError(
    val status: Int? = null,
    val title: String? = null,
    val detail: String? = null
)