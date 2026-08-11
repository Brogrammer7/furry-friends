package com.example.furryfriends.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val data: DataNode?
)

@Serializable
data class DataNode(
    val filters: List<Filter>? = null,
    val filterProcessing: String? = null,
    val filterRadius: FilterRadius? = null
)

@Serializable
data class Filter(
    val fieldName: String,
    val operation: String,
    val criteria: String
)

@Serializable
data class FilterRadius(
    val miles: Int,
    @SerialName("postalcode") val postalCode: Int
)
