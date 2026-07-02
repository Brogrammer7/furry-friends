package com.example.furryfriends.model

data class SearchResponse(
    val meta: Meta,
    val data: List<ResourceItem> = emptyList(),
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

data class ApiError(
    val status: Int? = null,
    val title: String? = null,
    val detail: String? = null
)