package com.example.furryfriends.model

import com.google.gson.annotations.SerializedName

data class SearchRequest(
    val data: DataNode?
)

data class DataNode(
    val filters: List<Filter>? = null,
    val filterProcessing: String? = null,
    val filterRadius: FilterRadius? = null
)

data class Filter(
    val fieldName: String,
    val operation: String,
    val criteria: String
)

data class FilterRadius(
    val miles: Int,
    @SerializedName("postalcode") val postalCode: Int
)
