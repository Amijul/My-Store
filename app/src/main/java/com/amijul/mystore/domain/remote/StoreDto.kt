package com.amijul.mystore.domain.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreDto(
    val id: String,
    val name: String,
    val category: String,
    @SerialName("distance_text")
    val distanceText: String,
    @SerialName("image_url")
    val imageUrl: String
)

